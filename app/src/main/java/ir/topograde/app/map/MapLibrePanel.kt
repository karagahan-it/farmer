package ir.topograde.app.map

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

data class MapLatLon(val latitude: Double, val longitude: Double)
data class MapHeatPoint(val latitude: Double, val longitude: Double, val category: String)

@Composable
fun MapLibrePanel(
    latitude: Double? = null,
    longitude: Double? = null,
    track: List<MapLatLon> = emptyList(),
    boundary: List<MapLatLon> = emptyList(),
    heatmap: List<MapHeatPoint> = emptyList()
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                map.setStyle(OfflineMapManager.STYLE_URL) { style ->
                    ensureLine(style, "track-source", "track-layer", lineJson(track, false), Color.rgb(126,87,194))
                    ensureLine(style, "boundary-source", "boundary-layer", lineJson(boundary, true), Color.rgb(230,81,0))
                    ensureHeat(style, heatmap)
                }
                map.cameraPosition = CameraPosition.Builder().target(LatLng(32.0, 53.0)).zoom(4.0).build()
            }
        }
    }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            mapView.getMapAsync { map ->
                map.cameraPosition = CameraPosition.Builder().target(LatLng(latitude,longitude)).zoom(16.0).build()
            }
        }
    }
    LaunchedEffect(track, boundary, heatmap) {
        mapView.getMapAsync { map ->
            map.getStyle { style ->
                val ts = style.getSourceAs<GeoJsonSource>("track-source")
                if (ts == null) ensureLine(style,"track-source","track-layer",lineJson(track,false),Color.rgb(126,87,194)) else ts.setGeoJson(lineJson(track,false))
                val bs = style.getSourceAs<GeoJsonSource>("boundary-source")
                if (bs == null) ensureLine(style,"boundary-source","boundary-layer",lineJson(boundary,true),Color.rgb(230,81,0)) else bs.setGeoJson(lineJson(boundary,true))
                updateHeat(style, heatmap)
            }
        }
    }
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxWidth().height(380.dp))
}

private data class HeatStyle(val key:String,val color:Int)
private val heatStyles=listOf(
    HeatStyle("DEEP_CUT",Color.rgb(183,28,28)),
    HeatStyle("CUT",Color.rgb(255,138,128)),
    HeatStyle("NEUTRAL",Color.rgb(189,189,189)),
    HeatStyle("FILL",Color.rgb(129,212,250)),
    HeatStyle("DEEP_FILL",Color.rgb(13,71,161))
)

private fun ensureHeat(style: org.maplibre.android.maps.Style, heatmap:List<MapHeatPoint>) {
    heatStyles.forEach { hs ->
        val sourceId="heat-${hs.key}-source"; val layerId="heat-${hs.key}-layer"
        if(style.getSource(sourceId)==null) style.addSource(GeoJsonSource(sourceId, pointJson(heatmap.filter{it.category==hs.key})))
        if(style.getLayer(layerId)==null) style.addLayer(CircleLayer(layerId,sourceId).withProperties(PropertyFactory.circleColor(hs.color),PropertyFactory.circleRadius(6f),PropertyFactory.circleOpacity(0.85f)))
    }
}
private fun updateHeat(style: org.maplibre.android.maps.Style, heatmap:List<MapHeatPoint>) {
    heatStyles.forEach { hs ->
        val sourceId="heat-${hs.key}-source"
        val src=style.getSourceAs<GeoJsonSource>(sourceId)
        if(src==null) ensureHeat(style,heatmap) else src.setGeoJson(pointJson(heatmap.filter{it.category==hs.key}))
    }
}

private fun ensureLine(style: org.maplibre.android.maps.Style, sourceId:String, layerId:String, json:String, color:Int) {
    if (style.getSource(sourceId) == null) style.addSource(GeoJsonSource(sourceId,json))
    if (style.getLayer(layerId) == null) style.addLayer(LineLayer(layerId,sourceId).withProperties(PropertyFactory.lineColor(color),PropertyFactory.lineWidth(4f)))
}
private fun lineJson(points:List<MapLatLon>, close:Boolean):String {
    val list = if (close && points.size > 2) points + points.first() else points
    val coords = list.joinToString(",") { "[" + it.longitude + "," + it.latitude + "]" }
    return "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"LineString\",\"coordinates\":[" + coords + "]}}]}"
}
private fun pointJson(points:List<MapHeatPoint>):String {
    val features=points.joinToString(",") { "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[" + it.longitude + "," + it.latitude + "]}}" }
    return "{\"type\":\"FeatureCollection\",\"features\":[" + features + "]}"
}