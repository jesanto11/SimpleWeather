package com.example.simpleweather.data.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

const val REQUEST_LOCATION_PERMISSION = 1
const val TAG = "LocationUtils"

fun checkAndRequestLocationPermissions(
    context: Context,
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>
): Boolean {
    val fineLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarseLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

    val permissionNeeded = mutableListOf<String>()

    if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
        permissionNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
        permissionNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    return if (permissionNeeded.isNotEmpty()) {
        requestPermissionLauncher.launch(permissionNeeded.toTypedArray())
        false
    } else {
        true
    }
}

suspend fun getCurrentLocation(activity: Activity): Result<Location> {
    return if (ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return Result.failure(Exception("Location permission denied"))
    } else {
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(activity)
        try {
            val location = fusedLocationClient.awaitLastLocation()
            if (location != null) {
                Log.d(TAG, "Last known location: $location")
                Result.success(location)
            } else {
                Log.d(TAG, "Last known location is null, requesting location update")
                val locationUpdate = fusedLocationClient.awaitLocationUpdate()
                Result.success(locationUpdate)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Permission denied", e)
            Result.failure(Exception("SecurityException: Permission denied"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting location", e)
            Result.failure(e)
        }
    }
}

suspend fun FusedLocationProviderClient.awaitLastLocation(): Location? {
    return suspendCancellableCoroutine { continuation ->
        try {
            lastLocation.addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    continuation.resumeWithException(task.exception ?: Exception("Unknown error"))
                }
            }
        } catch (e: SecurityException) {
            continuation.resumeWithException(Exception("SecurityException: Permission denied"))
        }
    }
}

suspend fun FusedLocationProviderClient.awaitLocationUpdate(): Location {
    return suspendCancellableCoroutine { continuation ->
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(5000)
            .build()

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                removeLocationUpdates(this)
                val location = locationResult.lastLocation
                if (location != null) {
                    Log.d(TAG, "Location update received: $location")
                    continuation.resume(location)
                } else {
                    Log.d(TAG, "Location update received but location is null")
                    continuation.resumeWithException(Exception("Unable to get location"))
                }
            }

            override fun onLocationAvailability(locationAvailability: com.google.android.gms.location.LocationAvailability) {
                Log.d(TAG, "Location availability: ${locationAvailability.isLocationAvailable}")
            }
        }

        try {
            requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            continuation.resumeWithException(Exception("SecurityException: Permission denied"))
        }
    }
}