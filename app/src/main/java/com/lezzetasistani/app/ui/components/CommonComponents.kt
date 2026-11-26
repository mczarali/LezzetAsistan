package com.lezzetasistani.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Tapas
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lezzetasistani.app.model.RecipeCategory

fun categoryIcon(category: RecipeCategory): ImageVector = when (category) {
    RecipeCategory.YEMEK -> Icons.Rounded.Restaurant
    RecipeCategory.MEZE -> Icons.Rounded.Tapas
    RecipeCategory.TATLI -> Icons.Rounded.Cake
    RecipeCategory.KOKTEYL -> Icons.Rounded.LocalBar
}

@Composable
fun AlcoholWarningFooter(
    modifier: Modifier = Modifier
) {
    Text(
        text = "Aşırı alkol tüketimi sağlığa zararlıdır.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
fun InfoBadge(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}