package com.lezzetasistani.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lezzetasistani.app.update.UpdateState

@Composable
fun UpdateDialog(
    updateState: UpdateState,
    onUpdateClick: (downloadUrl: String) -> Unit,
    onInstallClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    if (updateState is UpdateState.Idle || updateState is UpdateState.Checking) {
        return
    }

    AlertDialog(
        onDismissRequest = {
            if (updateState !is UpdateState.Downloading) {
                onDismissClick()
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Rounded.SystemUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = when (updateState) {
                    is UpdateState.UpdateAvailable -> "Yeni Güncelleme Mevcut! 🎉"
                    is UpdateState.Downloading -> "Güncelleme İndiriliyor"
                    is UpdateState.ReadyToInstall -> "İndirme Tamamlandı"
                    is UpdateState.Error -> "Güncelleme Hatası"
                    else -> "Güncelleme"
                },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (updateState) {
                    is UpdateState.UpdateAvailable -> {
                        Text(
                            text = "Lezzet Asistanı v${updateState.newVersion} sürümü yayınlandı.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (updateState.releaseNotes.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = updateState.releaseNotes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    is UpdateState.Downloading -> {
                        val percentage = (updateState.progress * 100).toInt()
                        Text(
                            text = "Yeni güncelleme arka planda indiriliyor (%$percentage)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { updateState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    is UpdateState.ReadyToInstall -> {
                        Text(
                            text = "Güncelleme dosyası başarıyla indirildi. Paket yükleyici açılıyor...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    is UpdateState.Error -> {
                        Text(
                            text = updateState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> {}
                }
            }
        },
        confirmButton = {
            when (updateState) {
                is UpdateState.UpdateAvailable -> {
                    Button(
                        onClick = { onUpdateClick(updateState.downloadUrl) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(text = "Güncelle")
                    }
                }

                is UpdateState.ReadyToInstall -> {
                    Button(
                        onClick = onInstallClick,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Yüklemeyi Başlat")
                    }
                }

                is UpdateState.Error -> {
                    Button(
                        onClick = onDismissClick,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Tamam")
                    }
                }

                else -> {}
            }
        },
        dismissButton = {
            if (updateState is UpdateState.UpdateAvailable) {
                TextButton(onClick = onDismissClick) {
                    Text(text = "Kapat")
                }
            }
        }
    )
}
