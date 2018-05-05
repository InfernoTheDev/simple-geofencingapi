package com.illnino.geofencingapi

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.illnino.geofencingapi.R.id.toolbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String? = MainActivity::class.simpleName
    }

    lateinit var geofencingClient: GeofencingClient

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

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

}
