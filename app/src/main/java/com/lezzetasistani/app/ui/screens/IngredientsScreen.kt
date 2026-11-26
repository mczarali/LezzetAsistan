package com.lezzetasistani.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.lezzetasistani.app.model.AlcoholPreference
import com.lezzetasistani.app.model.RecipeCategory
import com.lezzetasistani.app.ui.components.AlcoholWarningFooter
import com.lezzetasistani.app.ui.theme.LocalCategoryPalette
import com.lezzetasistani.app.viewmodel.RecipeUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IngredientsScreen(
    uiState: RecipeUiState,
    onBackClick: () -> Unit,
    onInputChanged: (String) -> Unit,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onAlcoholPreferenceSelected: (AlcoholPreference) -> Unit,
    onSearchClick: () -> Unit
) {
    val category = uiState.selectedCategory
    val categoryColors = LocalCategoryPalette.current.forCategory(category)
    val canSearch = uiState.ingredients.isNotEmpty()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category.ingredientsScreenTitle,
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
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onSearchClick()
                        },
                        enabled = canSearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = categoryColors.accent,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Ara",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (category == RecipeCategory.KOKTEYL) {
                        AlcoholWarningFooter(modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (category == RecipeCategory.KOKTEYL) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Alkol Tercihi",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AlcoholPreference.entries.forEach { pref ->
                            val isSelected = uiState.alcoholPreference == pref
                            FilterChip(
                                selected = isSelected,
                                onClick = { onAlcoholPreferenceSelected(pref) },
                                label = {
                                    Text(
                                        text = pref.label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = categoryColors.accent,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Text(
                text = category.inputHint,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = uiState.ingredientInput,
                    onValueChange = onInputChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Malzeme yaz...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onAddIngredient() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = categoryColors.accent,
                        cursorColor = categoryColors.accent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                FilledIconButton(
                    onClick = onAddIngredient,
                    enabled = uiState.ingredientInput.trim().isNotEmpty(),
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = categoryColors.accent,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Malzeme ekle",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (uiState.inputWarning != null) {
                Text(
                    text = uiState.inputWarning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (uiState.ingredients.isNotEmpty()) {
                Text(
                    text = "Eklenen Malzemeler (${uiState.ingredients.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.ingredients.forEach { ingredient ->
                        InputChip(
                            selected = true,
                            onClick = { onRemoveIngredient(ingredient) },
                            label = {
                                Text(
                                    text = ingredient,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "$ingredient kaldır",
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = categoryColors.container,
                                selectedLabelColor = categoryColors.onContainer,
                                selectedTrailingIconColor = categoryColors.onContainer
                            ),
                            border = null
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Text(
                        text = "Henüz malzeme eklenmedi. En az 1 malzeme eklediğinde \"Ara\" butonu aktif olacak.\n\nİpucu: Yağ, tuz, baharat, su ve buz gibi temel malzemeleri eklemene gerek yok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        }
    }
}
