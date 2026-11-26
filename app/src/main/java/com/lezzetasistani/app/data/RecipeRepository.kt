package com.lezzetasistani.app.data

import com.lezzetasistani.app.model.AlcoholPreference
import com.lezzetasistani.app.model.Recipe
import com.lezzetasistani.app.model.RecipeCategory
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed interface RecipeFetchResult {
    data class Success(val recipes: List<Recipe>) : RecipeFetchResult
    data object Empty : RecipeFetchResult
    data class Error(
        val message: String,
        val isNetworkError: Boolean = false
    ) : RecipeFetchResult
}

class RecipeRepository(
    private val apiService: GeminiApiService = GeminiApiService()
) {

    suspend fun searchRecipes(
        category: RecipeCategory,
        ingredients: List<String>,
        alcoholPreference: AlcoholPreference = AlcoholPreference.ANY
    ): RecipeFetchResult {
        return try {
            val recipes = apiService.fetchRecipes(category, ingredients, alcoholPreference)
            if (recipes.isEmpty()) {
                RecipeFetchResult.Empty
            } else {
                RecipeFetchResult.Success(recipes)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: MissingApiKeyException) {
            RecipeFetchResult.Error(
                message = "Gemini API anahtarı bulunamadı. Lütfen proje kökündeki local.properties dosyasına GEMINI_API_KEY ekleyip uygulamayı yeniden derleyin."
            )
        } catch (_: UnknownHostException) {
            RecipeFetchResult.Error(
                message = "İnternet bağlantısı bulunamadı. Lütfen bağlantınızı kontrol edip tekrar deneyin.",
                isNetworkError = true
            )
        } catch (_: ConnectException) {
            RecipeFetchResult.Error(
                message = "Sunucuya bağlanılamadı. Lütfen internet bağlantınızı kontrol edip tekrar deneyin.",
                isNetworkError = true
            )
        } catch (_: NoRouteToHostException) {
            RecipeFetchResult.Error(
                message = "Ağ bağlantısı kurulamadı. Lütfen internet bağlantınızı kontrol edin.",
                isNetworkError = true
            )
        } catch (_: SocketTimeoutException) {
            RecipeFetchResult.Error(
                message = "İstek zaman aşımına uğradı. Bağlantınız yavaş olabilir, lütfen tekrar deneyin.",
                isNetworkError = true
            )
        } catch (e: GeminiHttpException) {
            val userMessage = when (e.statusCode) {
                400 -> "İstek işlenemedi (400)."
                401, 403 -> "API anahtarı yetkisiz veya kısıtlı (${e.statusCode})."
                404 -> "Model veya uç nokta bulunamadı (404)."
                429 -> "İstek limiti veya API kotası doldu (429)."
                in 500..599 -> "Gemini sunucusunda geçici yoğunluk var (${e.statusCode})."
                else -> "Sunucu hatası oluştu (Kod: ${e.statusCode})."
            }
            RecipeFetchResult.Error(
                message = "$userMessage\n\nTeknik detay: ${e.message}"
            )
        } catch (e: GeminiParseException) {
            RecipeFetchResult.Error(
                message = "Tarif yanıtı okunurken sorun oluştu.\n\nTeknik detay: ${e.message}"
            )
        } catch (e: IOException) {
            RecipeFetchResult.Error(
                message = "Ağ hatası oluştu.\n\nTeknik detay: ${e.message ?: e::class.simpleName}",
                isNetworkError = true
            )
        } catch (e: Exception) {
            RecipeFetchResult.Error(
                message = "Beklenmeyen bir hata oluştu.\n\nTeknik detay: ${e::class.simpleName}: ${e.message}"
            )
        }
    }
}
