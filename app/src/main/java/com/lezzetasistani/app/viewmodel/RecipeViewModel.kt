package com.lezzetasistani.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lezzetasistani.app.data.RecipeFetchResult
import com.lezzetasistani.app.data.RecipeRepository
import com.lezzetasistani.app.model.AlcoholPreference
import com.lezzetasistani.app.model.Recipe
import com.lezzetasistani.app.model.RecipeCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val recipes: List<Recipe>) : SearchUiState
    data object Empty : SearchUiState
    data class Error(
        val message: String,
        val isNetworkError: Boolean = false
    ) : SearchUiState
}

data class RecipeUiState(
    val selectedCategory: RecipeCategory = RecipeCategory.YEMEK,
    val alcoholPreference: AlcoholPreference = AlcoholPreference.ANY,
    val ingredientInput: String = "",
    val ingredients: List<String> = emptyList(),
    val inputWarning: String? = null,
    val searchState: SearchUiState = SearchUiState.Idle,
    val selectedRecipe: Recipe? = null
)

class RecipeViewModel(
    private val repository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val turkishLocale = Locale.forLanguageTag("tr-TR")

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun selectCategory(category: RecipeCategory) {
        _uiState.update { current ->
            if (current.selectedCategory == category) {
                current.copy(
                    ingredientInput = "",
                    inputWarning = null
                )
            } else {
                searchJob?.cancel()
                RecipeUiState(selectedCategory = category)
            }
        }
    }

    fun selectAlcoholPreference(preference: AlcoholPreference) {
        _uiState.update { current ->
            if (current.alcoholPreference == preference) {
                current
            } else {
                current.copy(
                    alcoholPreference = preference,
                    searchState = SearchUiState.Idle
                )
            }
        }
    }

    fun onIngredientInputChange(newValue: String) {
        _uiState.update { current ->
            current.copy(
                ingredientInput = newValue,
                inputWarning = null
            )
        }
    }

    fun addIngredient() {
        val trimmed = _uiState.value.ingredientInput.trim()
        if (trimmed.isEmpty()) {
            return
        }

        val normalizedNew = trimmed.lowercase(turkishLocale)
        val alreadyExists = _uiState.value.ingredients.any { existing ->
            existing.lowercase(turkishLocale) == normalizedNew
        }

        if (alreadyExists) {
            _uiState.update { current ->
                current.copy(
                    ingredientInput = "",
                    inputWarning = "\"$trimmed\" zaten eklendi."
                )
            }
            return
        }

        _uiState.update { current ->
            current.copy(
                ingredients = current.ingredients + trimmed,
                ingredientInput = "",
                inputWarning = null,
                searchState = SearchUiState.Idle
            )
        }
    }

    fun removeIngredient(ingredient: String) {
        _uiState.update { current ->
            current.copy(
                ingredients = current.ingredients.filterNot { it == ingredient },
                inputWarning = null,
                searchState = SearchUiState.Idle
            )
        }
    }

    fun searchRecipes() {
        val currentState = _uiState.value
        if (currentState.ingredients.isEmpty()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    searchState = SearchUiState.Loading,
                    selectedRecipe = null
                )
            }

            when (
                val result = repository.searchRecipes(
                    category = currentState.selectedCategory,
                    ingredients = currentState.ingredients,
                    alcoholPreference = currentState.alcoholPreference
                )
            ) {
                is RecipeFetchResult.Success -> {
                    _uiState.update {
                        it.copy(searchState = SearchUiState.Success(result.recipes))
                    }
                }

                RecipeFetchResult.Empty -> {
                    _uiState.update {
                        it.copy(searchState = SearchUiState.Empty)
                    }
                }

                is RecipeFetchResult.Error -> {
                    _uiState.update {
                        it.copy(
                            searchState = SearchUiState.Error(
                                message = result.message,
                                isNetworkError = result.isNetworkError
                            )
                        )
                    }
                }
            }
        }
    }

    fun selectRecipe(recipe: Recipe) {
        _uiState.update { current ->
            current.copy(selectedRecipe = recipe)
        }
    }
}