package com.test.application.mapp.ui.compose_files

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.test.application.mapp.GeoJsonObjectDto
import com.test.application.mapp.presenters.MapPresenter

@Composable
fun MapScreen(presenter: MapPresenter) {

    val isLoading by presenter.isLoading
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = true) {
        presenter.loadGeoJsonData()
    }

    Mapbox.getInstance(context)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Map1(presenter)

        NavRail(presenter, selectedItem) { selectedItem = it }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        }
    }


}

@Composable
fun NavRail (presenter: MapPresenter, selectedItem: Int, onItemSelected: (Int) -> Unit){
    Box(
        modifier = Modifier
            .width(120.dp),
         contentAlignment = Alignment.Center
    ){
        NavigationRail(
            modifier = Modifier
                .width(120.dp)
                .align(Alignment.TopStart),
            containerColor = Color.LightGray,
            contentColor = Color.Black

        ) {
            Text(
                text = "Подложки",
                style = TextStyle(fontSize = 18.sp),
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 40.dp, bottom = 10.dp)
            )
            NavRailItem(
                text = "Streets",
                isSelected = selectedItem == 0,

            ) {
                presenter.setStyle("https://api.maptiler.com/maps/streets/style.json?key=onNivG1D5OeMY7l7WTYQ")
                onItemSelected(0)
            }
            NavRailItem(
                text = "Satellite",
                isSelected = selectedItem == 1
            ) {
                presenter.setStyle("https://api.maptiler.com/maps/satellite/style.json?key=onNivG1D5OeMY7l7WTYQ")
                onItemSelected(1)
            }
            NavRailItem(
                text = "Backdrop",
                isSelected = selectedItem == 2
            ) {
                presenter.setStyle("https://api.maptiler.com/maps/backdrop/style.json?key=onNivG1D5OeMY7l7WTYQ")
                onItemSelected(2)
            }
            Text(
                text = "Слои",
                style = TextStyle(fontSize = 18.sp),
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
            )
            FilterChipExample("Polygons", presenter)
        }
    }
}

@Composable
fun NavRailItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background = if (isSelected) Color.Gray else Color.Transparent
    val contentColor = if (isSelected) Color.White else Color.Black

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = 15.dp, vertical = 15.dp)
            .clickable { onClick() }
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = contentColor)
    }
}


@Composable
fun Map1(presenter: MapPresenter){
    val mapView =  rememberMapViewWithLifecycle()
    val mapPoints by presenter.mapPoints
    val mapCircle by presenter.mapCircle
    val featureCollection by presenter.featureCollection
    val currentStyle by rememberUpdatedState(newValue = presenter.getCurrentStyleMy())

    AndroidView(
        factory = {
            mapView
        },
        modifier = Modifier.fillMaxSize()
    )


    LaunchedEffect(currentStyle, mapCircle, featureCollection) {

        mapView.getMapAsync { mapboxMap ->
            featureCollection.features()?.filter { it.geometry()?.type() == "Point" }?.mapNotNull { feature ->
                val geometry = feature.geometry()
                val properties = feature.properties()

                val coordinates = when (geometry) {
                    is Point -> geometry.coordinates()
                    else -> null
                }
                val latitude = coordinates?.get(0)
                val longitude = coordinates?.get(1)

                val systemObject = properties?.getAsJsonObject("system")
                val oPropertyObject = systemObject?.getAsJsonObject("oProperty")

                val name = oPropertyObject?.get("name")?.asString
                val desc = oPropertyObject?.get("desc")?.asString

                if (latitude != null && longitude != null && name != null && desc != null) {
                    mapPoints.add(GeoJsonObjectDto(latitude, longitude, name, desc))
                }

            }

            mapboxMap.setStyle(currentStyle) { style->


                if (mapCircle){
                    Log.d("debug", "mapCircle:$mapCircle")
                    Log.d("debug", "featureCollection:$featureCollection")
                    val geoJsonSourceFill = GeoJsonSource("custom-source-fill", featureCollection)
                    style.addSource(geoJsonSourceFill)

                    val fillLayer = FillLayer("fill-layer", "custom-source-fill")
                    fillLayer.setProperties(
                        PropertyFactory.fillColor("rgba(255, 0, 0, 0.3)")
                    )
                    style.addLayer(fillLayer)
                }else{
                    style.removeLayer("fill-layer")
                }

                mapPoints.forEach { point ->
                    val markerOptions = MarkerOptions()
                        .position(LatLng(point.latitude, point.longitude))
                        .setTitle(point.name)
                        .setSnippet(point.description)

                    mapboxMap.addMarker(markerOptions)
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipExample(name:String, preseter: MapPresenter) {
    var selected by remember { mutableStateOf(true) }

    FilterChip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        onClick = {
            selected = !selected
            if(selected){
                preseter.toggleLayerVisibility(true)
            }else{
                preseter.toggleLayerVisibility(false)
            }
                  },
        label = {
            Text(name)
        },
        selected = selected,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )

            }
        } else {
            null
        }
    )
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        onDispose {
            mapView.onStop()
        }
    }

    return mapView
}

