package com.lezzetasistani.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class RecipeCategory(
    val title: String,
    val subtitle: String,
    val ingredientsScreenTitle: String,
    val inputHint: String,
    val loadingMessage: String
) {
    YEMEK(
        title = "Yemek",
        subtitle = "Ana yemekler, çorbalar, hamur işleri ve tatlılar",
        ingredientsScreenTitle = "Yemek Malzemeleri",
        inputHint = "Dolabındaki malzemeleri ekle (örn: havuç, yoğurt, tavuk)",
        loadingMessage = "En lezzetli yemek tarifleri hazırlanıyor..."
    ),
    MEZE(
        title = "Meze",
        subtitle = "Sofraları zenginleştiren soğuk ve sıcak mezeler",
        ingredientsScreenTitle = "Meze Malzemeleri",
        inputHint = "Dolabındaki malzemeleri ekle (örn: süzme yoğurt, patlıcan, sarımsak, ceviz)",
        loadingMessage = "En seçkin meze tarifleri hazırlanıyor..."
    ),
    TATLI(
        title = "Tatlı",
        subtitle = "Şerbetli, sütlü, çikolatalı ve meyveli nefis tatlılar",
        ingredientsScreenTitle = "Tatlı Malzemeleri",
        inputHint = "Dolabındaki malzemeleri ekle (örn: süt, un, kakao, vanilya)",
        loadingMessage = "En tatlı nefis tarifler hazırlanıyor..."
    ),
    KOKTEYL(
        title = "Kokteyl",
        subtitle = "Klasik ve modern alkollü & alkolsüz kokteyller",
        ingredientsScreenTitle = "Kokteyl Malzemeleri",
        inputHint = "Elindeki içecekleri ve malzemeleri ekle (örn: votka, limon, nane)",
        loadingMessage = "En uyumlu kokteyl tarifleri hazırlanıyor..."
    )
}

enum class AlcoholPreference(val label: String) {
    ANY("Fark Etmez"),
    ALCOHOLIC("Alkollü"),
    NON_ALCOHOLIC("Alkolsüz")
}

@Serializable
data class RecipeResponse(
    @SerialName("tarifler")
    val tarifler: List<Recipe> = emptyList()
)

@Serializable
data class Recipe(
    @SerialName("ad")
    val ad: String = "",
    @SerialName("aciklama")
    val aciklama: String = "",
    @SerialName("mutfak")
    val mutfak: String = "",
    @SerialName("sureDakika")
    val sureDakika: Int = 0,
    @SerialName("kisiSayisi")
    val kisiSayisi: Int = 1,
    @SerialName("malzemeler")
    val malzemeler: List<String> = emptyList(),
    @SerialName("adimlar")
    val adimlar: List<String> = emptyList(),
    @SerialName("bardak")
    val bardak: String? = null,
    @SerialName("susleme")
    val susleme: String? = null,
    @SerialName("alkollu")
    val alkollu: Boolean? = null
)