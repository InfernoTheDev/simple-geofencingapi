package com.illnino.geofencingapi

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import com.illnino.geofencingapi.R.id.toolbar
import kotlinx.android.synthetic.main.activity_main.*
import java.security.Provider

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String? = MainActivity::class.simpleName
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var locationRequest: LocationRequest
    var locationManager: LocationManager? = null

    private val geofencePendingIntent: PendingIntent by lazy {
        Toast.makeText(this, "Add Pending Intent", Toast.LENGTH_LONG).show()
        //val intent = Intent("com.illnino.geofencingapi.ACTION_RECEIVE_GEOFENCE")
        val intent = Intent(this, GeofenceTransitionsIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        //PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        val geofenceList: ArrayList<Geofence> = ArrayList<Geofence>();

        val firstStop = Geofence.Builder()
                .setRequestId("first")
                .setCircularRegion(
                        12.897571,
                        100.903045,
                        10F
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                //.setLoiteringDelay(2000)
                .build()

        geofenceList.add(firstStop)

        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    val locationListeners = arrayOf(
            LTRLocationListener(LocationManager.GPS_PROVIDER),
            LTRLocationListener(LocationManager.NETWORK_PROVIDER)
    )

    class LTRLocationListener(provider: String) : android.location.LocationListener {

        val lastLocation = Location(provider)

        override fun onLocationChanged(location: Location?) {
            lastLocation.set(location)

            Log.d(TAG, "onLocationChanged: ${location?.latitude}, ${location?.longitude}")
            // TODO: Do something here
        }

        override fun onProviderDisabled(provider: String?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Log.d(TAG, "onStatusChanged $provider, status: $status")

            if (extras != null) {
                for (i in extras.keySet()) {
                    Log.d(TAG, "extras: ${extras[i]}")
                }
            }
        }

    }

    fun createLocationRequest(): LocationRequest {
        return LocationRequest().apply {
            interval = 2000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient
                .requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Create persistent LocationManager reference
        /*if (locationManager == null)
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0F, locationListeners[0])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
        }*/

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        locationRequest = createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    Log.d(TAG, "onLocationResult: ${location?.latitude}, ${location?.longitude}")
                }
            }
        }

        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Add Geofencing !!", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Add Geofencing !!")

            geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent).run {

                addOnSuccessListener {
                    //Toast.makeText(applicationContext, "geofencingClient add success", Toast.LENGTH_LONG).show()
                    Log.d(TAG, "geofencingClient add success")
                }
                addOnFailureListener {
                    //Toast.makeText(applicationContext, "geofencingClient add fail ${it.message}", Toast.LENGTH_LONG).show()
                    Log.d(TAG, "geofencingClient add fail ${it.message}")
                }
            }

        } else {
            Toast.makeText(this, "Permission Denied !!", Toast.LENGTH_LONG).show()
        }
    }

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            //thetext.setText("" + location.longitude + ":" + location.latitude);
        }
    }

}
