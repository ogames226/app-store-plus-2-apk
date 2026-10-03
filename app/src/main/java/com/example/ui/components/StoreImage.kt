package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale

/**
 * Coil wrapper that constrains decode resolution to the on-screen size.
 *
 * Every image in this app previously loaded its full-resolution bitmap and then
 * scaled it down into an 68dp icon box — wasted decode, wasted heap, and the
 * single biggest cause of scroll jank. Passing an explicit `size` lets Coil
 * downsample at decode time instead.
 *
 * The [crossfade] also costs a frame-and-a-half per image; it's off by default
 * here because grid scroll is where it's felt most, and it isn't enabled on
 * hero art either.
 */
@Composable
fun StoreImage(
  data: Any?,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop,
  placeholderPainter: Painter? = null,
  crossfade: Boolean = false
) {
  val context = LocalContext.current
  val density = LocalDensity.current

  val request = remember(data, crossfade) {
    ImageRequest.Builder(context)
      .data(data)
      .crossfade(crossfade)
      .scale(Scale.FILL)
      .build()
  }

  Box(modifier = modifier) {
    AsyncImage(
      model = request,
      contentDescription = contentDescription,
      contentScale = contentScale,
      placeholder = placeholderPainter,
      error = placeholderPainter,
      modifier = Modifier.fillMaxSize()
    )
  }
}

/**
 * Size-aware variant. [width]/[height] in dp define the decode target; without
 * this Coil has to measure-then-load, which costs a frame.
 */
@Composable
fun StoreImage(
  data: Any?,
  contentDescription: String?,
  width: Dp,
  height: Dp,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop,
  placeholderPainter: Painter? = null,
  crossfade: Boolean = false
) {
  val context = LocalContext.current
  val density = LocalDensity.current

  val request = remember(data, width, height, crossfade) {
    val w = with(density) { width.roundToPx() }
    val h = with(density) { height.roundToPx() }
    ImageRequest.Builder(context)
      .data(data)
      .size(w, h)
      .scale(Scale.FILL)
      .crossfade(crossfade)
      .build()
  }

  Box(modifier = modifier) {
    AsyncImage(
      model = request,
      contentDescription = contentDescription,
      contentScale = contentScale,
      placeholder = placeholderPainter,
      error = placeholderPainter,
      modifier = Modifier.fillMaxSize()
    )
  }
}

/** The standard scrim laid over banner art so white text stays readable. */
fun bannerScrim(): Brush = Brush.verticalGradient(
  colors = listOf(Color.Transparent, Color(0x99000000), Color(0xEE070A12))
)
