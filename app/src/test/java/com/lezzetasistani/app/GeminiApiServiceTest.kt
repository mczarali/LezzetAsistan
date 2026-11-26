package com.lezzetasistani.app

import com.lezzetasistani.app.data.GeminiApiService
import com.lezzetasistani.app.model.AlcoholPreference
import com.lezzetasistani.app.model.RecipeCategory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiApiServiceTest {

    @Test
    fun testFetchYemekRecipesIntegration() = runBlocking {
        val apiService = GeminiApiService()
        val recipes = apiService.fetchRecipes(
            category = RecipeCategory.YEMEK,
            ingredients = listOf("tavuk", "yoğurt", "sarımsak")
        )
        assertTrue("Yemek tarif listesi boş olmamalı", recipes.isNotEmpty())
    }

    @Test
    fun testFetchTatliRecipesIntegration() = runBlocking {
        val apiService = GeminiApiService()
        val recipes = apiService.fetchRecipes(
            category = RecipeCategory.TATLI,
            ingredients = listOf("süt", "un", "şeker", "kakao")
        )
        assertTrue("Tatlı tarif listesi boş olmamalı", recipes.isNotEmpty())
    }

    @Test
    fun testFetchCocktailNonAlcoholicIntegration() = runBlocking {
        val apiService = GeminiApiService()
        val recipes = apiService.fetchRecipes(
            category = RecipeCategory.KOKTEYL,
            ingredients = listOf("limon", "nane", "soda"),
            alcoholPreference = AlcoholPreference.NON_ALCOHOLIC
        )
        assertTrue("Alkolsüz kokteyl listesi boş olmamalı", recipes.isNotEmpty())
        assertTrue("Tüm kokteyller alkolsüz olmalı", recipes.all { it.alkollu == false })
    }
}
