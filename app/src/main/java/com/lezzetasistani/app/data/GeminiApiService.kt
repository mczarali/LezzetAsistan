package com.lezzetasistani.app.data

import com.lezzetasistani.app.BuildConfig
import com.lezzetasistani.app.model.AlcoholPreference
import com.lezzetasistani.app.model.Recipe
import com.lezzetasistani.app.model.RecipeCategory
import com.lezzetasistani.app.model.RecipeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class MissingApiKeyException : Exception()

class GeminiHttpException(
    val statusCode: Int,
    override val message: String
) : Exception(message)

class GeminiParseException(override val message: String) : Exception(message)

private data class GeminiModelConfig(
    val modelId: String,
    val thinkingLevel: String? = null
)

class GeminiApiService {

    companion object {
        private val MODEL_FALLBACK_CHAIN = listOf(
            GeminiModelConfig(modelId = "gemini-3.5-flash-lite", thinkingLevel = null),
            GeminiModelConfig(modelId = "gemini-3.6-flash", thinkingLevel = "LOW"),
            GeminiModelConfig(modelId = "gemini-3.8-flash", thinkingLevel = "LOW"),
            GeminiModelConfig(modelId = "gemini-3.5-flash", thinkingLevel = null)
        )
        private const val MAX_RECIPES = 20
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchRecipes(
        category: RecipeCategory,
        ingredients: List<String>,
        alcoholPreference: AlcoholPreference = AlcoholPreference.ANY
    ): List<Recipe> = withContext(Dispatchers.IO) {
        val apiKey = KeyObfuscator.getSecureApiKey(BuildConfig.GEMINI_API_KEY)
        if (apiKey.isBlank()) {
            throw MissingApiKeyException()
        }

        var lastError: Exception? = null

        for ((index, modelConfig) in MODEL_FALLBACK_CHAIN.withIndex()) {
            val url =
                "https://generativelanguage.googleapis.com/v1beta/models/${modelConfig.modelId}:generateContent"
            val requestPayload = buildRequestJson(category, ingredients, alcoholPreference, modelConfig).toString()

            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestPayload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            try {
                val recipes = executeAndParse(request, modelConfig.modelId)
                return@withContext recipes
            } catch (e: GeminiHttpException) {
                // Yetki hatalarında (401/403) diğer modelleri denemeye gerek yok
                if (e.statusCode == 401 || e.statusCode == 403) {
                    throw e
                }
                lastError = e
                if (index < MODEL_FALLBACK_CHAIN.lastIndex) {
                    delay(300)
                }
            } catch (e: SocketTimeoutException) {
                lastError = e
                if (index < MODEL_FALLBACK_CHAIN.lastIndex) {
                    delay(300)
                }
            }
        }

        throw lastError ?: GeminiHttpException(503, "Tüm Gemini modellerinde geçici yoğunluk var.")
    }

    private fun executeAndParse(
        request: Request,
        modelId: String
    ): List<Recipe> {
        client.newCall(request).execute().use { response ->
            val responseBody = response.body.string()

            if (!response.isSuccessful) {
                val apiErrorMessage = extractApiErrorMessage(responseBody)
                throw GeminiHttpException(
                    statusCode = response.code,
                    message = "[$modelId] ${apiErrorMessage ?: "HTTP ${response.code}"}"
                )
            }

            if (responseBody.isBlank()) {
                throw GeminiParseException("Sunucudan boş yanıt döndü.")
            }

            val apiResponse = try {
                json.decodeFromString<GeminiGenerateContentResponse>(responseBody)
            } catch (e: Exception) {
                throw GeminiParseException("API yanıtı çözümlenemedi: ${e.message}")
            }

            // Düşünme (thought) parçalarını hariç tutup yalnızca gerçek JSON metnini al
            val rawJsonText = apiResponse.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.filter { it.thought != true }
                ?.mapNotNull { it.text }
                ?.joinToString(separator = "")
                ?.trim()
                ?.let(::stripMarkdownCodeFences)

            if (rawJsonText.isNullOrBlank()) {
                return emptyList()
            }

            val parsed = try {
                json.decodeFromString<RecipeResponse>(rawJsonText)
            } catch (e: Exception) {
                throw GeminiParseException("Tarif verisi ayrıştırılamadı: ${e.message}")
            }

            return parsed.tarifler
                .filter { it.ad.isNotBlank() && it.malzemeler.isNotEmpty() && it.adimlar.isNotEmpty() }
                .take(MAX_RECIPES)
        }
    }

    private fun buildRequestJson(
        category: RecipeCategory,
        ingredients: List<String>,
        alcoholPreference: AlcoholPreference,
        modelConfig: GeminiModelConfig
    ): JsonObject {
        val systemPrompt = buildSystemInstruction(category, alcoholPreference)
        val userPrompt = buildUserPrompt(category, ingredients, alcoholPreference)

        return buildJsonObject {
            putJsonObject("systemInstruction") {
                putJsonArray("parts") {
                    add(buildJsonObject {
                        put("text", systemPrompt)
                    })
                }
            }
            putJsonArray("contents") {
                add(buildJsonObject {
                    put("role", "user")
                    putJsonArray("parts") {
                        add(buildJsonObject {
                            put("text", userPrompt)
                        })
                    }
                })
            }
            putJsonObject("generationConfig") {
                put("responseMimeType", "application/json")
                put("responseSchema", buildResponseSchema(category))
                put("maxOutputTokens", 16384)
                if (modelConfig.thinkingLevel != null) {
                    putJsonObject("thinkingConfig") {
                        put("thinkingLevel", modelConfig.thinkingLevel)
                    }
                }
            }
        }
    }

    private fun buildSystemInstruction(
        category: RecipeCategory,
        alcoholPreference: AlcoholPreference
    ): String {
        val categoryRule = when (category) {
            RecipeCategory.YEMEK ->
                "Yalnızca Türk mutfağından ve dünya mutfağından bilinen, lezzeti kanıtlanmış YEMEK tarifleri öner (ana yemek, çorba, makarna, pilav, börek, salata vb.). Meze, tatlı veya kokteyl önerme."

            RecipeCategory.MEZE ->
                "Yalnızca Türk mutfağından, Akdeniz, Ege, Orta Doğu ve dünya mutfağından bilinen, lezzeti kanıtlanmış MEZE tarifleri öner. Ana yemek, tatlı veya kokteyl önerme."

            RecipeCategory.TATLI ->
                "Yalnızca bilinen, denenmiş ve lezzeti kanıtlanmış TATLI tarifleri öner (şerbetli, sütlü, çikolatalı, meyveli veya fırın tatlıları). Yemek, meze veya kokteyl önerme."

            RecipeCategory.KOKTEYL -> {
                val alcoholRule = when (alcoholPreference) {
                    AlcoholPreference.ALCOHOLIC -> "Kullanıcı ALKOLLÜ kokteyl istedi. Önerdiğin tüm kokteyller mutlaka alkollü olmalıdır (alkollu: true)."
                    AlcoholPreference.NON_ALCOHOLIC -> "Kullanıcı ALKOLSÜZ kokteyl (mocktail) istedi. Önerdiğin tüm kokteyller mutlaka alkolsüz olmalıdır (alkollu: false)."
                    AlcoholPreference.ANY -> "Kullanıcının alkol tercihi fark etmez. Hem alkollü hem alkolsüz kokteyller önerebilirsin."
                }
                "Yalnızca uluslararası ve yerel barmenlik literatüründe bilinen, dengeli ve beğenilen KOKTEYL tarifleri öner. $alcoholRule Her kokteyl için bardak türünü, süslemeyi ve alkollü olup olmadığını doğru belirt."
            }
        }

        return """
            Sen deneyimli bir şef, gastronomi uzmanı ve miksologsun.
            Görevin, kullanıcının verdiği malzemelerle yapılabilecek en iyi tarifleri Türkçe ve kesin JSON şemasına uygun şekilde sunmaktır.

            KESİN KURALLAR:
            1. $categoryRule
            2. Kullanıcının girdiği malzemeleri öncelikle ana malzeme olarak kullanan tarifleri listele. Kullanıcının girdiği malzemeleri görmezden gelme.
            3. Yağ (zeytinyağı, sıvı yağ, tereyağı), tuz, karabiber, temel baharatlar, su ve buz temel kiler/bar malzemesi sayılır; bunlar kullanıcının listesinde yok diye tarifleri eleme.
            4. Yalnızca gerçek, bilinen, lezzeti ve beğenilirliği kanıtlanmış başarılı tarifler ver. Asla uydurma, saçma veya garip malzeme kombinasyonları üretme.
            5. Tarifleri en başarılı, en popüler ve malzemelerle en uyumlu olandan başlayarak sırala. En fazla $MAX_RECIPES tarif döndür.
            6. Eğer $MAX_RECIPES tane gerçekten iyi ve bilinen tarif yoksa daha az tarif döndür; sayıyı doldurmak için kesinlikle uydurma tarif ekleme. Girilen malzemelerle gerçekten bilinen ve başarılı hiçbir tarif yapılamıyorsa "tarifler" listesini tamamen boş ([]) döndür.
            7. Tüm alanlar doğal, akıcı ve doğru Türkçe olsun.
            8. "aciklama" alanı tam olarak bir cümlelik, net ve iştah açıcı bir özet olsun.
            9. "malzemeler" listesindeki her öğe açık ölçü birimiyle yazılsın (örn: "2 yemek kaşığı zeytinyağı", "45 ml votka").
            10. "adimlar" listesi baştan sona net, anlaşılır ve uygulanabilir hazırlama adımları içersin (adım numaralarını metnin başına yazma, sadece adım açıklamasını yaz).
        """.trimIndent()
    }

    private fun buildUserPrompt(
        category: RecipeCategory,
        ingredients: List<String>,
        alcoholPreference: AlcoholPreference
    ): String {
        val formattedIngredients = ingredients.joinToString(separator = ", ")
        val alcoholText = if (category == RecipeCategory.KOKTEYL) " (Tercih: ${alcoholPreference.label})" else ""
        return """
            Bölüm: ${category.title}$alcoholText
            Elimdeki malzemeler: $formattedIngredients

            Yukarıdaki malzemeleri ana malzeme olarak kullanan, bilinen ve en başarılı ${category.title.lowercase()} tariflerini JSON formatında listele.
        """.trimIndent()
    }

    private fun buildResponseSchema(category: RecipeCategory): JsonObject {
        val isCocktail = category == RecipeCategory.KOKTEYL

        return buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("tarifler") {
                    put("type", "ARRAY")
                    putJsonObject("items") {
                        put("type", "OBJECT")
                        putJsonObject("properties") {
                            putJsonObject("ad") {
                                put("type", "STRING")
                                put("description", "Tarifin Türkçe adı")
                            }
                            putJsonObject("aciklama") {
                                put("type", "STRING")
                                put("description", "Tarifi özetleyen tek cümlelik açıklama")
                            }
                            putJsonObject("mutfak") {
                                put("type", "STRING")
                                put("description", "Mutfak veya köken türü (örn: Türk, İtalyan, Meksika, Klasik)")
                            }
                            putJsonObject("sureDakika") {
                                put("type", "INTEGER")
                                put("description", "Tahmini toplam hazırlık ve pişirme süresi (dakika)")
                            }
                            putJsonObject("kisiSayisi") {
                                put("type", "INTEGER")
                                put("description", "Tarifin kaç kişilik olduğu")
                            }
                            putJsonObject("malzemeler") {
                                put("type", "ARRAY")
                                put("description", "Ölçüleriyle birlikte tüm malzemeler")
                                putJsonObject("items") {
                                    put("type", "STRING")
                                }
                            }
                            putJsonObject("adimlar") {
                                put("type", "ARRAY")
                                put("description", "Sıralı ve detaylı hazırlanış adımları")
                                putJsonObject("items") {
                                    put("type", "STRING")
                                }
                            }
                            if (isCocktail) {
                                putJsonObject("bardak") {
                                    put("type", "STRING")
                                    put("description", "Servis edilecek bardak türü (örn: Highball bardağı, Martini kadehi)")
                                }
                                putJsonObject("susleme") {
                                    put("type", "STRING")
                                    put("description", "Kokteyl süslemesi (örn: Taze nane dalı ve limon dilimi)")
                                }
                                putJsonObject("alkollu") {
                                    put("type", "BOOLEAN")
                                    put("description", "Kokteyl alkollü ise true, alkolsüz ise false")
                                }
                            }
                        }
                        putJsonArray("required") {
                            add("ad")
                            add("aciklama")
                            add("mutfak")
                            add("sureDakika")
                            add("kisiSayisi")
                            add("malzemeler")
                            add("adimlar")
                            if (isCocktail) {
                                add("bardak")
                                add("susleme")
                                add("alkollu")
                            }
                        }
                    }
                }
            }
            putJsonArray("required") {
                add("tarifler")
            }
        }
    }

    private fun extractApiErrorMessage(responseBody: String): String? {
        return try {
            json.decodeFromString<GeminiErrorEnvelope>(responseBody).error?.message
        } catch (_: Exception) {
            null
        }
    }

    private fun stripMarkdownCodeFences(raw: String): String {
        val fence = "`".repeat(3)
        val trimmed = raw.trim()
        if (!trimmed.startsWith(fence)) return trimmed
        return trimmed
            .removePrefix("${fence}json")
            .removePrefix("${fence}JSON")
            .removePrefix(fence)
            .removeSuffix(fence)
            .trim()
    }
}

@Serializable
private data class GeminiGenerateContentResponse(
    @SerialName("candidates")
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
private data class GeminiCandidate(
    @SerialName("content")
    val content: GeminiContent? = null
)

@Serializable
private data class GeminiContent(
    @SerialName("parts")
    val parts: List<GeminiPart> = emptyList()
)

@Serializable
private data class GeminiPart(
    @SerialName("text")
    val text: String? = null,
    @SerialName("thought")
    val thought: Boolean? = null
)

@Serializable
private data class GeminiErrorEnvelope(
    @SerialName("error")
    val error: GeminiErrorDetail? = null
)

@Serializable
private data class GeminiErrorDetail(
    @SerialName("message")
    val message: String? = null
)