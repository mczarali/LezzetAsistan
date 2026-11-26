package com.lezzetasistani.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lezzetasistani.app.ui.components.UpdateDialog
import com.lezzetasistani.app.ui.screens.HomeScreen
import com.lezzetasistani.app.ui.screens.IngredientsScreen
import com.lezzetasistani.app.ui.screens.RecipeDetailScreen
import com.lezzetasistani.app.ui.screens.ResultsScreen
import com.lezzetasistani.app.update.UpdateManager
import com.lezzetasistani.app.update.UpdateState
import com.lezzetasistani.app.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

private object Routes {
    const val HOME = "home"
    const val INGREDIENTS = "ingredients"
    const val RESULTS = "results"
    const val DETAIL = "detail"
}

@Composable
fun AppNavigation(
    viewModel: RecipeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val updateManager = remember { UpdateManager() }
    val updateState by updateManager.updateState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        updateManager.checkForUpdates()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onCategorySelected = { category ->
                        viewModel.selectCategory(category)
                        navController.navigate(Routes.INGREDIENTS) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.INGREDIENTS) {
                IngredientsScreen(
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onInputChanged = viewModel::onIngredientInputChange,
                    onAddIngredient = viewModel::addIngredient,
                    onRemoveIngredient = viewModel::removeIngredient,
                    onAlcoholPreferenceSelected = viewModel::selectAlcoholPreference,
                    onSearchClick = {
                        viewModel.searchRecipes()
                        navController.navigate(Routes.RESULTS) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.RESULTS) {
                ResultsScreen(
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onRetryClick = viewModel::searchRecipes,
                    onRecipeClick = { recipe ->
                        viewModel.selectRecipe(recipe)
                        navController.navigate(Routes.DETAIL) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.DETAIL) {
                RecipeDetailScreen(
                    category = uiState.selectedCategory,
                    recipe = uiState.selectedRecipe,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        UpdateDialog(
            updateState = updateState,
            onUpdateClick = { downloadUrl ->
                coroutineScope.launch {
                    updateManager.startDownloadAndInstall(context, downloadUrl)
                }
            },
            onInstallClick = {
                val currentState = updateState
                if (currentState is UpdateState.ReadyToInstall) {
                    updateManager.installApk(context, currentState.apkFile)
                }
            },
            onDismissClick = updateManager::dismissUpdate
        )
    }
}

