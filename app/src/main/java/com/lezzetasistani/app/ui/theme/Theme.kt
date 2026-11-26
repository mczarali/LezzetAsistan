package com.lezzetasistani.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.lezzetasistani.app.model.RecipeCategory

@Immutable
data class CategoryColorSet(
    val accent: Color,
    val container: Color,
    val onContainer: Color
)

@Immutable
data class CategoryPalette(
    val yemek: CategoryColorSet,
    val meze: CategoryColorSet,
    val tatli: CategoryColorSet,
    val kokteyl: CategoryColorSet
) {
    fun forCategory(category: RecipeCategory): CategoryColorSet = when (category) {
        RecipeCategory.YEMEK -> yemek
        RecipeCategory.MEZE -> meze
        RecipeCategory.TATLI -> tatli
        RecipeCategory.KOKTEYL -> kokteyl
    }
}

private val LightCategoryPalette = CategoryPalette(
    yemek = CategoryColorSet(YemekAccentLight, YemekContainerLight, YemekOnContainerLight),
    meze = CategoryColorSet(MezeAccentLight, MezeContainerLight, MezeOnContainerLight),
    tatli = CategoryColorSet(TatliAccentLight, TatliContainerLight, TatliOnContainerLight),
    kokteyl = CategoryColorSet(KokteylAccentLight, KokteylContainerLight, KokteylOnContainerLight)
)

private val DarkCategoryPalette = CategoryPalette(
    yemek = CategoryColorSet(YemekAccentDark, YemekContainerDark, YemekOnContainerDark),
    meze = CategoryColorSet(MezeAccentDark, MezeContainerDark, MezeOnContainerDark),
    tatli = CategoryColorSet(TatliAccentDark, TatliContainerDark, TatliOnContainerDark),
    kokteyl = CategoryColorSet(KokteylAccentDark, KokteylContainerDark, KokteylOnContainerDark)
)

val LocalCategoryPalette = staticCompositionLocalOf { LightCategoryPalette }

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = TerracottaOnPrimary,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = TerracottaOnContainer,
    secondary = MezeAccentLight,
    onSecondary = Color.White,
    tertiary = KokteylAccentLight,
    onTertiary = Color.White,
    background = WarmCreamBackground,
    onBackground = WarmOnBackground,
    surface = WarmSurface,
    onSurface = WarmOnBackground,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmOnSurfaceVariant,
    outline = WarmOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaDarkPrimary,
    onPrimary = TerracottaDarkOnPrimary,
    primaryContainer = TerracottaDarkContainer,
    onPrimaryContainer = TerracottaDarkOnContainer,
    secondary = MezeAccentDark,
    onSecondary = Color(0xFF0A2E18),
    tertiary = KokteylAccentDark,
    onTertiary = Color(0xFF341247),
    background = DarkEspressoBackground,
    onBackground = DarkWarmOnBackground,
    surface = DarkEspressoSurface,
    onSurface = DarkWarmOnBackground,
    surfaceVariant = DarkEspressoSurfaceVariant,
    onSurfaceVariant = DarkWarmOnSurfaceVariant,
    outline = DarkWarmOutline
)

@Composable
fun LezzetAsistaniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val categoryPalette = if (darkTheme) DarkCategoryPalette else LightCategoryPalette

    CompositionLocalProvider(LocalCategoryPalette provides categoryPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}