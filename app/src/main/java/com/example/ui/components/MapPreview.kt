package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.MapPlace
import com.example.data.MapTilerGeocoding
import com.example.ui.theme.FameGoBorder
import com.example.ui.theme.FameGoCard
import com.example.ui.theme.FameGoGold
import com.example.ui.theme.FameGoTextMuted
import com.example.ui.theme.FameGoWhite
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/** One-tap popular shoot localities — works with zero network. */
private val PopularLocalities = listOf(
  "Bandra West", "Andheri West", "Juhu", "Lower Parel",
  "Powai", "Colaba", "Versova", "Goregaon"
)

/**
 * Real movable map (osmdroid + free CARTO dark tiles, no API key):
 * drag to pan, pinch to zoom, tap to drop a pin, "Use this spot" fills
 * the address via free reverse-geocoding. Recenters on search picks.
 */
@Composable
fun MapPreviewCard(
  centerLng: Double,
  centerLat: Double,
  zoom: Int,
  pinLabel: String?,
  onPick: (MapPlace) -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  var mapRef by remember { mutableStateOf<MapView?>(null) }
  // Last center WE applied — user pans never trigger a recenter fight.
  val lastApplied = remember { doubleArrayOf(centerLat, centerLng, zoom.toDouble()) }
  val markerHolder = remember { arrayOfNulls<Marker>(1) }
  var pending by remember { mutableStateOf<Triple<Double, Double, String>?>(null) }
  var pendingFull by remember { mutableStateOf<MapPlace?>(null) }
  var resolving by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(240.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(FameGoCard)
      .border(1.dp, FameGoBorder, RoundedCornerShape(16.dp))
      .testTag("map_preview")
  ) {
    AndroidView(
      factory = { ctx ->
        MapView(ctx).apply {
          setTileSource(
            XYTileSource(
              "CartoDark", 0, 19, 256, ".png",
              arrayOf(
                "https://a.basemaps.cartocdn.com/dark_all/",
                "https://b.basemaps.cartocdn.com/dark_all/",
                "https://c.basemaps.cartocdn.com/dark_all/"
              )
            )
          )
          setMultiTouchControls(true)
          zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
          controller.setZoom(zoom.toDouble())
          controller.setCenter(GeoPoint(centerLat, centerLng))
          overlays.add(
            MapEventsOverlay(object : MapEventsReceiver {
              override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                resolving = true
                pending = null
                scope.launch {
                  val place = MapTilerGeocoding.reverse(p.latitude, p.longitude).getOrNull()
                  pending = Triple(
                    p.latitude, p.longitude,
                    place?.short ?: "Selected spot"
                  )
                  // Stash full place for the confirm tap.
                  pendingFull = place ?: MapPlace(
                    full = "${p.latitude}, ${p.longitude}",
                    short = "Selected spot",
                    latitude = p.latitude,
                    longitude = p.longitude
                  )
                  resolving = false
                }
                return true
              }

              override fun longPressHelper(p: GeoPoint): Boolean = false
            })
          )
          val marker = Marker(this).apply {
            position = GeoPoint(centerLat, centerLng)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
          }
          overlays.add(marker)
          markerHolder[0] = marker
          mapRef = this
        }
      },
      update = { map ->
        markerHolder[0]?.let {
          it.position = GeoPoint(centerLat, centerLng)
          it.title = pinLabel
        }
        if (lastApplied[0] != centerLat || lastApplied[1] != centerLng || lastApplied[2] != zoom.toDouble()) {
          lastApplied[0] = centerLat
          lastApplied[1] = centerLng
          lastApplied[2] = zoom.toDouble()
          map.controller.animateTo(GeoPoint(centerLat, centerLng))
          map.controller.setZoom(zoom.toDouble())
        }
        map.invalidate()
      },
      modifier = Modifier.matchParentSize(),
      onRelease = { it.onDetach() }
    )

    // Zoom controls (minimal, gold).
    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf(Icons.Default.Add to { mapRef?.controller?.zoomIn() },
        Icons.Default.Remove to { mapRef?.controller?.zoomOut() }).forEach { (icon, action) ->
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.65f))
            .border(1.dp, FameGoGold.copy(alpha = 0.5f), CircleShape)
            .clickable { action() },
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = FameGoGold, modifier = Modifier.size(16.dp))
        }
      }
    }

    // Resolving spinner / confirm chip.
    if (resolving) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 26.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color.Black.copy(alpha = 0.7f))
          .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(color = FameGoGold, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
      }
    } else {
      pendingFull?.let { full ->
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 26.dp, start = 16.dp, end = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(FameGoGold)
            .clickable {
              onPick(full)
              pending = null
            }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("map_use_spot"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = Color(0xFF1A1408),
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Use this spot${if (full.short != "Selected spot") " • ${full.short}" else ""}",
              color = Color(0xFF1A1408),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    Text(
      text = "© OpenStreetMap © CARTO",
      color = Color.White.copy(alpha = 0.65f),
      fontSize = 9.sp,
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 8.dp, bottom = 6.dp)
    )
  }
}

@Composable
fun LocalityChips(
  onPick: (MapPlace) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Text(
      text = "Popular shoot areas — tap to fill",
      color = FameGoTextMuted,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .testTag("locality_chips"),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      PopularLocalities.forEach { loc ->
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FameGoCard)
            .border(1.dp, FameGoBorder, RoundedCornerShape(16.dp))
            .clickable {
              onPick(MapPlace(full = "$loc, Mumbai, Maharashtra, India", short = loc))
            }
            .padding(horizontal = 14.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(FameGoGold)
          )
          Spacer(modifier = Modifier.width(7.dp))
          Text(
            text = loc,
            color = FameGoWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}
