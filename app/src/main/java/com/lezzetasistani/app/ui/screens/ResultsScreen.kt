package com.lezzetasistani.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lezzetasistani.app.model.Recipe
import com.lezzetasistani.app.model.RecipeCategory
import com.lezzetasistani.app.ui.components.AlcoholWarningFooter
import com.lezzetasistani.app.ui.components.InfoBadge
import com.lezzetasistani.app.ui.theme.LocalCategoryPalette
import com.lezzetasistani.app.viewmodel.RecipeUiState
import com.lezzetasistani.app.viewmodel.SearchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    uiState: RecipeUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val category = uiState.selectedCategory
    val categoryColors = LocalCategoryPalette.current.forCategory(category)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${category.title} Tarifleri",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            if (category == RecipeCategory.KOKTEYL) {
                AlcoholWarningFooter(modifier = Modifier.navigationBarsPadding())
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState.searchState) {
                SearchUiState.Idle,
                SearchUiState.Loading -> {
                    LoadingView(
                        message = category.loadingMessage,
                        accentColor = categoryColors.accent
                    )
                }

                is SearchUiState.Error -> {
                    StatusMessageView(
                        icon = if (state.isNetworkError) Icons.Rounded.WifiOff else Icons.Rounded.ErrorOutline,
                        title = if (state.isNetworkError) "Bağlantı Sorunu" else "Bir Hata Oluştu",
                        description = state.message,
                        accentColor = categoryColors.accent,
                        onRetryClick = onRetryClick,
                        onEditIngredientsClick = onBackClick
                    )
                }

                SearchUiState.Empty -> {
                    StatusMessageView(
                        icon = Icons.Rounded.SearchOff,
                        title = "Uygun Tarif Bulunamadı",
                        description = "Seçtiğin tüm malzemeleri birlikte kullanan, bilinen ve kanıtlanmış bir ${category.title.lowercase()} tarifi bulunamadı. Malzemeleri değiştirerek tekrar deneyebilirsin.",
                        accentColor = categoryColors.accent,
                        onRetryClick = onRetryClick,
                        onEditIngredientsClick = onBackClick
                    )
                }

                is SearchUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "${state.recipes.size} tarif bulundu",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = categoryColors.accent
                                )
                                Text(
                                    text = "Malzemeler: ${uiState.ingredients.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        itemsIndexed(
                            items = state.recipes,
                            key = { index, recipe -> "${index}_${recipe.ad}" }
                        ) { _, recipe ->
                            RecipeSummaryCard(
                                recipe = recipe,
                                containerColor = categoryColors.container,
                                onContainerColor = categoryColors.onContainer,
                                onClick = { onRecipeClick(recipe) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeSummaryCard(
    recipe: Recipe,
    containerColor: Color,
    onContainerColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = recipe.ad,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = recipe.aciklama,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (recipe.mutfak.isNotBlank()) {
                    InfoBadge(
                        icon = Icons.Rounded.Public,
                        text = recipe.mutfak,
                        containerColor = containerColor,
                        contentColor = onContainerColor
                    )
                }

                if (recipe.sureDakika > 0) {
                    InfoBadge(
                        icon = Icons.Rounded.Timer,
                        text = "${recipe.sureDakika} dk",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView(
    message: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(52.dp),
            color = accentColor,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Bu işlem birkaç saniye sürebilir.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatusMessageView(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    onRetryClick: () -> Unit,
    onEditIngredientsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetryClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Tekrar dene",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onEditIngredientsClick,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Malzemeleri düzenle",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
