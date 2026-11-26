package com.lezzetasistani.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.NoDrinks
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WineBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.rounded.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lezzetasistani.app.model.Recipe
import com.lezzetasistani.app.model.RecipeCategory
import com.lezzetasistani.app.ui.components.AlcoholWarningFooter
import com.lezzetasistani.app.ui.components.InfoBadge
import com.lezzetasistani.app.ui.theme.LocalCategoryPalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    category: RecipeCategory,
    recipe: Recipe?,
    onBackClick: () -> Unit
) {
    val categoryColors = LocalCategoryPalette.current.forCategory(category)
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${category.title} Detayı",
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
                actions = {
                    if (recipe != null) {
                        IconButton(onClick = { shareRecipe(context, recipe) }) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Tarifi Paylaş"
                            )
                        }
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
        if (recipe == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tarif bilgisi bulunamadı.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Başlık ve Temel Bilgiler Kartı
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = recipe.ad,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (recipe.aciklama.isNotBlank()) {
                            Text(
                                text = recipe.aciklama,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (recipe.mutfak.isNotBlank()) {
                                InfoBadge(
                                    icon = Icons.Rounded.Public,
                                    text = recipe.mutfak,
                                    containerColor = categoryColors.container,
                                    contentColor = categoryColors.onContainer
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
                            if (recipe.kisiSayisi > 0) {
                                InfoBadge(
                                    icon = Icons.Rounded.People,
                                    text = "${recipe.kisiSayisi} kişilik",
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Kokteyl Ek Bilgileri Kartı
            if (category == RecipeCategory.KOKTEYL) {
                item {
                    CocktailExtraCard(
                        recipe = recipe,
                        accentColor = categoryColors.accent,
                        containerColor = categoryColors.container,
                        onContainerColor = categoryColors.onContainer
                    )
                }
            }

            // Malzemeler Kartı
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionHeader(
                            icon = Icons.Rounded.Checklist,
                            title = "Malzemeler",
                            accentColor = categoryColors.accent
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                        recipe.malzemeler.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(categoryColors.accent)
                                )
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Hazırlanış Adımları Kartı
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(
                            icon = Icons.Rounded.FormatListNumbered,
                            title = "Hazırlanışı",
                            accentColor = categoryColors.accent
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                        recipe.adimlar.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(30.dp),
                                    shape = CircleShape,
                                    color = categoryColors.container
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = categoryColors.onContainer
                                        )
                                    }
                                }
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CocktailExtraCard(
    recipe: Recipe,
    accentColor: Color,
    containerColor: Color,
    onContainerColor: Color
) {
    val isAlcoholic = recipe.alkollu == true
    val alcoholText = if (isAlcoholic) "Alkollü" else "Alkolsüz"
    val alcoholIcon = if (isAlcoholic) Icons.Rounded.LocalBar else Icons.Rounded.NoDrinks

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CocktailDetailRow(
                icon = alcoholIcon,
                label = "İçerik",
                value = alcoholText,
                iconColor = accentColor,
                textColor = onContainerColor
            )
            if (!recipe.bardak.isNullOrBlank()) {
                CocktailDetailRow(
                    icon = Icons.Rounded.WineBar,
                    label = "Bardak Türü",
                    value = recipe.bardak,
                    iconColor = accentColor,
                    textColor = onContainerColor
                )
            }
            if (!recipe.susleme.isNullOrBlank()) {
                CocktailDetailRow(
                    icon = Icons.Rounded.Spa,
                    label = "Süsleme",
                    value = recipe.susleme,
                    iconColor = accentColor,
                    textColor = onContainerColor
                )
            }
        }
    }
}

@Composable
private fun CocktailDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun shareRecipe(context: Context, recipe: Recipe) {
    val text = buildString {
        appendLine("🍽️ ${recipe.ad}")
        if (recipe.aciklama.isNotBlank()) {
            appendLine(recipe.aciklama)
        }
        appendLine()
        if (recipe.mutfak.isNotBlank()) appendLine("📍 Mutfak: ${recipe.mutfak}")
        if (recipe.sureDakika > 0) appendLine("⏱️ Süre: ${recipe.sureDakika} dk")
        if (recipe.kisiSayisi > 0) appendLine("👥 Kişi Sayısı: ${recipe.kisiSayisi}")
        appendLine()
        appendLine("🛒 MALZEMELER:")
        recipe.malzemeler.forEach { appendLine("• $it") }
        appendLine()
        appendLine("👩‍🍳 HAZIRLANIŞI:")
        recipe.adimlar.forEachIndexed { index, step -> appendLine("${index + 1}. $step") }
        appendLine()
        append("Lezzet Asistanı ile hazırlandı ✨")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Tarifi Paylaş")
    context.startActivity(shareIntent)
}