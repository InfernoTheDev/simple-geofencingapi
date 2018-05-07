package com.illnino.geofencingapi

import android.Manifest
import android.annotation.SuppressLint
import android.app.IntentService
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String? = MainActivity::class.simpleName
        lateinit var getInstance: MainActivity
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var locationRequest: LocationRequest
    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        getInstance = this

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        locationRequest = createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    Log.d(TAG, "onLocationResult: ${location?.latitude}, ${location?.longitude}")
                    updateDisplayView("onLocationResult: ${location?.latitude}, ${location?.longitude}")
                }
            }
        }

        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Add Geofencing !!", Toast.LENGTH_LONG).show()
            updateDisplayView("Add Geofencing !!")
            Log.d(TAG, "Add Geofencing !!")

            geofencingClient.addGeofences(
                    getGeofencingRequest(),
                    geofencePendingIntent).run {

                addOnSuccessListener {
                    //Toast.makeText(applicationContext, "geofencingClient add success", Toast.LENGTH_LONG).show()
                    Log.d(TAG, "geofencingClient add success")
                    updateDisplayView("geofencingClient add success")
                }
                addOnFailureListener {
                    //Toast.makeText(applicationContext, "geofencingClient add fail ${it.message}", Toast.LENGTH_LONG).show()
                    Log.d(TAG, "geofencingClient add fail ${it.message}")
                    updateDisplayView("geofencingClient add fail ${it.message}")
                }
            }

        } else {
            Toast.makeText(this, "Permission Denied !!", Toast.LENGTH_LONG).show()
            updateDisplayView("Permission Denied !!")
        }

    }

    fun updateDisplayView(txt: String){
        tv_display_status.append(txt + "\n")
        //sv_display.fullScroll(View.FOCUS_DOWN)
    }


    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private val geofencePendingIntent: PendingIntent by lazy {
        Toast.makeText(this, "Add Pending Intent", Toast.LENGTH_LONG).show()
        updateDisplayView("\nAdd Pending Intent !!")
        //val intent = Intent("com.illnino.geofencingapi.ACTION_RECEIVE_GEOFENCE")
        val intent = Intent(this, GeofenceTransitionsIntentService::class.java).apply {
            action = "com.illnino.geofencingapi.ACTION_RECEIVE_GEOFENCE"
        }
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        //PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        val geofenceList: ArrayList<Geofence> = ArrayList<Geofence>();

        val firstStop = Geofence.Builder().apply {
            setRequestId("Voova")
            setCircularRegion(
                    12.880310,
                    100.895110,
                    20F)
            setExpirationDuration(Geofence.NEVER_EXPIRE)
            //setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            setLoiteringDelay(5000)
        }.build()


        val secondStop = Geofence.Builder().apply {
            setRequestId("7-11 Seven-Eleven")
            setCircularRegion(
                    12.880023,
                    100.894688,
                    20F)
            setExpirationDuration(Geofence.NEVER_EXPIRE)
            //setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            setLoiteringDelay(5000)
        }.build()

        val thirdStop = Geofence.Builder().apply {
            setRequestId("Whale Marina Condo showroom")
            setCircularRegion(
                    12.880408,
                    100.895835,
                    20F
            )
            setExpirationDuration(Geofence.NEVER_EXPIRE)
            //setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            setLoiteringDelay(5000)
        }.build()

        geofenceList.add(firstStop)
        geofenceList.add(secondStop)
        geofenceList.add(thirdStop)

        return GeofencingRequest.Builder().apply {
            setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
            addGeofences(geofenceList)
        }.build()
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

    class GeofenceTransitionsIntentService : BroadcastReceiver() {
        //override fun onHandleIntent(intent: Intent?) {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "GeofenceTransitionsIntentService Fire !!")

            MainActivity.getInstance.updateDisplayView("GeofenceTransitionsIntentService Fire !!")
            Toast.makeText(context, "GeofenceTransitionsIntentService Fire !!", Toast.LENGTH_LONG).show()

            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            Log.d(TAG, "GeofenceTransitionsIntentService triggeringLocation !! ${geofencingEvent.triggeringLocation}")

            val name: String = geofencingEvent.triggeringGeofences[0].requestId
            Log.d(TAG, "GeofenceTransitionsIntentService triggeringLocation !! $name")


            if (geofencingEvent.hasError()) {

                val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                Toast.makeText(context, "GeofenceTransitionsIntentService errorMessage $errorMessage !!", Toast.LENGTH_LONG).show()
                MainActivity.getInstance.updateDisplayView("GeofenceTransitionsIntentService errorMessage $errorMessage !!")
                Log.e(TAG, errorMessage)
                return
            }

            // Get the transition type.
            val geofenceTransition = geofencingEvent.geofenceTransition

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                val status: String = handleEnterExit(geofenceTransition)
                Log.d(TAG, status)
                MainActivity
                        .getInstance
                        .updateDisplayView("Status: $status, $name")
                Toast.makeText(context, "Status: $status, $name", Toast.LENGTH_LONG).show()
            } else {
                // Log the error.
                MainActivity
                        .getInstance
                        .updateDisplayView("Error transition code: $geofenceTransition, ${GeofenceStatusCodes.getStatusCodeString(geofenceTransition)}")
                Toast.makeText(context, "Error transition code: $geofenceTransition", Toast.LENGTH_LONG).show()
                Log.d(TAG, GeofenceStatusCodes.getStatusCodeString(geofenceTransition))
            }
        }

        private fun handleEnterExit(geofenceTransition: Int): String =
                when(geofenceTransition){

                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        Log.d("geofence", "Entered")
                        "Entered"
                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT ->{
                        Log.d("geofence", "Exited")
                        "Exited"
                    }

                    Geofence.GEOFENCE_TRANSITION_DWELL ->{
                        Log.d("geofence", "Dwell")
                        "Dwell"
                    }

                    else -> {
                        Log.d("geofence", "Unknow")
                        "Unknow"
                    }

                }
    }

}
