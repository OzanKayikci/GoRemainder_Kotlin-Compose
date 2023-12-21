package com.laivinieks.georemind.feature_reminder.presentation.add_edit_remainder.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    viewModel: LocationViewModel = hiltViewModel(),
    callback: (
        closeDialog: Boolean,
        locationName: String
    ) -> Unit,
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings().copy(
                zoomGesturesEnabled = true,
                compassEnabled = true,
                indoorLevelPickerEnabled = true,
                mapToolbarEnabled = true,
                myLocationButtonEnabled = true,
                rotationGesturesEnabled = true,
                scrollGesturesEnabled = true,
                scrollGesturesEnabledDuringRotateOrZoom = true,
                tiltGesturesEnabled = true,
                zoomControlsEnabled = false
            )
        )
    }
    var properties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isBuildingEnabled = false,
                isIndoorEnabled = true,
                isMyLocationEnabled = true,
                isTrafficEnabled = false,
                // latLngBoundsForCameraTarget = ,
//            mapStyleOptions = null,
//                maxZoomPreference = 4.5f,
//                minZoomPreference = 6.7f,
            )
        )
    }


    var isNormalMapType by remember {
        mutableStateOf(false)
    }

    var getLocationName by remember {
        mutableStateOf(false)
    }
    var locationName by remember {
        mutableStateOf("")
    }



    DisposableEffect(Unit) {
        viewModel.startLocationUpdates()

        onDispose {
            viewModel.stopLocationUpdates()
        }
    }

    val locationState by viewModel.location

    var latitude by remember{ mutableStateOf(if (locationState != null) locationState!!.latitude else 0.0) }
    var longitude  by remember{ mutableStateOf(if (locationState != null) locationState!!.longitude else 0.0) }
    var markerPosition by remember { mutableStateOf<LatLng?>(null) }


    if (getLocationName) {
        scope.launch {
            getLocationName(context = context, latitude = latitude, longitude = longitude) { isFetched, name ->
                locationName = name
                getLocationName = false
            }

        }

    }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.onBackground,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(4.dp),
        onDismissRequest = {
            callback(false, locationName)
        }
    ) {


        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                properties = properties,
                uiSettings = uiSettings,
                onMapClick = { latLng ->
                    markerPosition = latLng

                },
                cameraPositionState =
                CameraPositionState(CameraPosition(LatLng(latitude, longitude), 18f, 0f, 0f))


            ) {
                markerPosition?.let {
                    Marker(
                        state = rememberMarkerState(position = it),
                        title = "Clicked Location", // Optional title for the marker
                        snippet = "Latitude: ${it.latitude}, Longitude: ${it.longitude}" // Optional snippet
                    )
                    latitude = it.latitude
                    longitude = it.longitude
                }
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = locationName,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(0.7f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = if (isNormalMapType) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Switch(
                        checked = isNormalMapType,
                        onCheckedChange = {
                            isNormalMapType = !isNormalMapType
                            if (it) {
                                properties = properties.copy(mapType = MapType.HYBRID)
                            } else {
                                properties = properties.copy(mapType = MapType.NORMAL)

                            }
                        }
                    )


                    Button(
                        onClick = {
                            getLocationName = true

                        }) {
                        Text(text = "Select Locaiton")
                    }


                    IconButton(
                        onClick = { callback(false, locationName) },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            .size(32.dp)

                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "close map",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                }
            }
        }
    }
}
