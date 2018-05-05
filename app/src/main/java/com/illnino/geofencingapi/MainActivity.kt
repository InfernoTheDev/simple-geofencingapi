package com.illnino.geofencingapi

import android.Manifest
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

    lateinit var geofencingClient: GeofencingClient
    var locationManager: LocationManager? = null

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceTransitionsIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
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
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Create persistent LocationManager reference
        if (locationManager == null)
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0F, locationListeners[0])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
        }


        geofencingClient = LocationServices.getGeofencingClient(this)

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
