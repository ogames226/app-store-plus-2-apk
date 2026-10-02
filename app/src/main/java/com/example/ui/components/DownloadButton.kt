package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadItem
import com.example.model.DownloadStatus
import com.example.ui.theme.ButtonGradient

@Composable
fun DownloadButton(
    downloadItem: DownloadItem?,
    defaultText: String = "تثبيت",
    onInstallClick: () -> Unit,
    onCancelClick: () -> Unit,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 34.dp,
    fontSize: Int = 13
) {
    val status = downloadItem?.status ?: DownloadStatus.IDLE

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .background(ButtonGradient)
            .clickable {
                when (status) {
                    DownloadStatus.IDLE, DownloadStatus.FAILED -> onInstallClick()
                    DownloadStatus.DOWNLOADING -> onCancelClick()
                    DownloadStatus.COMPLETED -> onOpenClick()
                    DownloadStatus.PAUSED -> onInstallClick()
                }
            }
            .animateContentSize()
            .testTag("download_button"),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            DownloadStatus.DOWNLOADING -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { downloadItem?.progress ?: 0.1f },
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "${((downloadItem?.progress ?: 0f) * 100).toInt()}%",
                        color = Color.White,
                        fontSize = (fontSize - 1).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
            DownloadStatus.COMPLETED -> {
                Text(
                    text = "فتح",
                    color = Color.White,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            DownloadStatus.FAILED -> {
                Text(
                    text = "إعادة",
                    color = Color.White,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            else -> {
                Text(
                    text = defaultText,
                    color = Color.White,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }
    }
}
