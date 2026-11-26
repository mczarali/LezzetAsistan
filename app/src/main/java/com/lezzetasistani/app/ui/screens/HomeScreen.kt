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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lezzetasistani.app.model.RecipeCategory
import com.lezzetasistani.app.ui.components.categoryIcon
import com.lezzetasistani.app.ui.theme.LocalCategoryPalette

@Composable
fun HomeScreen(
    onCategorySelected: (RecipeCategory) -> Unit
) {
    val categoryPalette = LocalCategoryPalette.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Lezzet Asistanı",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Her Dilekte Bir Lezzet ✨",
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Elindeki malzemelerle ne hazırlamak istersin?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(RecipeCategory.entries) { category ->
                val colors = categoryPalette.forCategory(category)
                CategoryCard(
                    category = category,
                    accentColor = colors.accent,
                    containerColor = colors.container,
                    onContainerColor = colors.onContainer,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: RecipeCategory,
    accentColor: Color,
    containerColor: Color,
    onContainerColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(20.dp),
                color = containerColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = categoryIcon(category),
                        contentDescription = category.title,
                        tint = accentColor,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(12.dp),
                color = containerColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "İleri",
                        tint = onContainerColor
                    )
                }
            }
        }
    }
}
