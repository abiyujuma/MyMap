package com.rs.mymap.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import com.rs.mymap.data.api.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // UI State for Text Fields
    var originText by remember { mutableStateOf("-7.9666204,112.6326321") }
    var destinationText by remember { mutableStateOf("-0.0263303,109.3425039") }
    
    // UI State for Markers and Route
    var originLatLng by remember { mutableStateOf<LatLng?>(LatLng(-7.9666204, 112.6326321)) }
    var destinationLatLng by remember { mutableStateOf<LatLng?>(LatLng(-0.0263303, 109.3425039)) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    
    // UI State for Route Details
    var distance by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var transportMode by remember { mutableStateOf("Roda 4") }
    var selectedMode by remember { mutableStateOf("driving") } // driving or two_wheeler
    var isLoading by remember { mutableStateOf(false) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-7.9666204, 112.6326321), 5f)
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val apiKey = "AIzaSyAMxzfxQjAg9Jr-WE5EtBpAE7xCXwz2B1Q"
    
    val mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = false)) }
    val mapUiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = true)) }

    // Helper function to fetch route
    fun fetchRoute() {
        if (originText.isBlank() || destinationText.isBlank()) return
        
        scope.launch {
            isLoading = true
            try {
                // Parse coordinates from text
                val originParts = originText.split(",")
                val destParts = destinationText.split(",")
                
                if (originParts.size >= 2 && destParts.size >= 2) {
                    val oLatLng = LatLng(originParts[0].trim().toDouble(), originParts[1].trim().toDouble())
                    val dLatLng = LatLng(destParts[0].trim().toDouble(), destParts[1].trim().toDouble())
                    
                    originLatLng = oLatLng
                    destinationLatLng = dLatLng
                    
                    val response = RetrofitClient.getDirectionsApiService(context).getDirections(
                        origin = "${oLatLng.latitude},${oLatLng.longitude}",
                        destination = "${dLatLng.latitude},${dLatLng.longitude}",
                        mode = selectedMode,
                        apiKey = apiKey
                    )
                    
                    if (response.routes.isNotEmpty()) {
                        val route = response.routes[0]
                        val encodedPolyline = route.overviewPolyline.points
                        routePoints = PolyUtil.decode(encodedPolyline)
                        
                        // Extract Distance and Duration
                        if (route.legs.isNotEmpty()) {
                            distance = route.legs[0].distance.text
                            duration = route.legs[0].duration.text
                        }

                        // Animate camera to fit both points
                        val bounds = LatLngBounds.builder()
                            .include(oLatLng)
                            .include(dLatLng)
                            .build()
                        
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngBounds(bounds, 150),
                            durationMs = 1000
                        )
                        scaffoldState.bottomSheetState.partialExpand()
                    } else {
                        Log.e("MapScreen", "No routes found or API error")
                        Toast.makeText(context, "Rute tidak ditemukan. Cek API Key/Koneksi.", Toast.LENGTH_LONG).show()
                        // If no route found, at least move to origin
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(oLatLng, 15f),
                            durationMs = 1000
                        )
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e("MapScreen", "Error fetching directions", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Reset function
    fun resetFields() {
        originText = ""
        destinationText = ""
        routePoints = emptyList()
        distance = ""
        duration = ""
        transportMode = "Roda 4"
        selectedMode = "driving"

        // Tetap tampilkan marker awal
        originLatLng = LatLng(-7.9666204, 112.6326321)
        destinationLatLng = LatLng(-0.0263303, 109.3425039)
    }

    // Auto refresh when mode changes
    LaunchedEffect(selectedMode) {
        if (originText.isNotEmpty() && destinationText.isNotEmpty()) {
            fetchRoute()
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        fetchRoute()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 140.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Compact Summary (Always visible in Peek Height)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (distance.isNotEmpty() || duration.isNotEmpty()) {
                            Text(
                                text = "$duration ($distance)",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF00796B)
                            )
                            Text(
                                text = "Moda: $transportMode",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = "Cari Rute Perjalanan",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Button(
                        onClick = { fetchRoute() },
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(if (isLoading) "Loading..." else "Cari Rute")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Expanded Controls
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = originText,
                        onValueChange = { originText = it },
                        label = { Text("Origin (Lat,Lng)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color.Blue) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = destinationText,
                        onValueChange = { destinationText = it },
                        label = { Text("Destination (Lat,Lng)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color.Red) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Pilih Moda Transportasi:", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedMode == "driving",
                            onClick = { 
                                selectedMode = "driving"
                                transportMode = "Roda 4"
                            },
                            label = { Text("Mobil") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        FilterChip(
                            selected = selectedMode == "two_wheeler",
                            onClick = { 
                                selectedMode = "two_wheeler"
                                transportMode = "Roda 2"
                            },
                            label = { Text("Motor") },
                            leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { 
                                resetFields()
                                scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                        ) {
                            Text("Reset / Clear")
                        }
                    }
                    
                    // Detailed Info Card (Visible when expanded)
                    if (distance.isNotEmpty() || duration.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = Color.Blue)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = "Jarak: $distance", style = MaterialTheme.typography.bodyLarge)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Red)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = "Waktu Tempuh: $duration", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp)) // Extra padding for the bottom
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings
            ) {
                originLatLng?.let {
                    Marker(
                        state = remember(it) { MarkerState(position = it) },
                        title = "Malang",
                        snippet = "Titik Keberangkatan",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
                
                destinationLatLng?.let {
                    Marker(
                        state = remember(it) { MarkerState(position = it) },
                        title = "Pontianak",
                        snippet = "Titik Tujuan",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }

                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = Color.Blue,
                        width = 15f
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    MapScreen()
}
