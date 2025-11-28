package com.example.palcharity.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat

class LocationHelper(private val context: Context) {

    private var selectedLocation: Pair<Double, Double>? = null

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): Pair<Double, Double>? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                null
            } else {
                val location: Location? =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                location?.let { Pair(it.latitude, it.longitude) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun setSelectedLocation(lat: Double, lon: Double) {
        selectedLocation = Pair(lat, lon)
    }

    fun getSelectedLocation(): Pair<Double, Double>? = selectedLocation
}
