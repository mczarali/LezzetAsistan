package com.lezzetasistani.app

import com.lezzetasistani.app.model.RecipeCategory
import com.lezzetasistani.app.viewmodel.RecipeViewModel
import com.lezzetasistani.app.viewmodel.SearchUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecipeViewModelTest {

    private lateinit var viewModel: RecipeViewModel

    @Before
    fun setUp() {
        viewModel = RecipeViewModel()
    }

    @Test
    fun `default category is YEMEK`() {
        val state = viewModel.uiState.value
        assertEquals(RecipeCategory.YEMEK, state.selectedCategory)
        assertTrue(state.ingredients.isEmpty())
        assertEquals(SearchUiState.Idle, state.searchState)
    }

    @Test
    fun `selecting new category resets state`() {
        viewModel.onIngredientInputChange("Patlıcan")
        viewModel.addIngredient()
        assertEquals(1, viewModel.uiState.value.ingredients.size)

        viewModel.selectCategory(RecipeCategory.MEZE)
        val state = viewModel.uiState.value
        assertEquals(RecipeCategory.MEZE, state.selectedCategory)
        assertTrue(state.ingredients.isEmpty())
    }

    @Test
    fun `adding ingredient adds to list and clears input`() {
        viewModel.onIngredientInputChange("Havuç")
        viewModel.addIngredient()

        val state = viewModel.uiState.value
        assertEquals(listOf("Havuç"), state.ingredients)
        assertEquals("", state.ingredientInput)
        assertNull(state.inputWarning)
    }

    @Test
    fun `adding duplicate ingredient sets warning`() {
        viewModel.onIngredientInputChange("Tavuk")
        viewModel.addIngredient()

        viewModel.onIngredientInputChange("tavuk")
        viewModel.addIngredient()

        val state = viewModel.uiState.value
        assertEquals(listOf("Tavuk"), state.ingredients)
        assertEquals("\"tavuk\" zaten eklendi.", state.inputWarning)
    }

    @Test
    fun `removing ingredient updates list`() {
        viewModel.onIngredientInputChange("Yoğurt")
        viewModel.addIngredient()

        viewModel.removeIngredient("Yoğurt")

        val state = viewModel.uiState.value
        assertTrue(state.ingredients.isEmpty())
    }
}
