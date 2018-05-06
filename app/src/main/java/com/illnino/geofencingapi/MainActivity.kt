package com.illnino.geofencingapi

import android.Manifest
import android.annotation.SuppressLint
import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String? = MainActivity::class.simpleName
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        locationRequest = createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    Log.d(TAG, "onLocationResult: ${location?.latitude}, ${location?.longitude}")
                    updateDisplayView("onLocationResult: ${location?.latitude}, ${location?.longitude}\n")
                }
            }
        }

        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Add Geofencing !!", Toast.LENGTH_LONG).show()
            updateDisplayView("Add Geofencing !!")
            Log.d(TAG, "Add Geofencing !!")

            geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent).run {

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


    private val geofencePendingIntent: PendingIntent by lazy {
        Toast.makeText(this, "Add Pending Intent", Toast.LENGTH_LONG).show()
        updateDisplayView("\nAdd Pending Intent !!")
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
                        20F
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

    class GeofenceTransitionsIntentService : IntentService("GeofenceTransitionsIntentService") {

        override fun onHandleIntent(intent: Intent?) {
        //override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "GeofenceTransitionsIntentService Fire !!")
            //updateDisplayView("GeofenceTransitionsIntentService Fire !!")
            //display.append("GeofenceTransitionsIntentService Fire !!")
            Toast.makeText(applicationContext, "GeofenceTransitionsIntentService Fire !!", Toast.LENGTH_LONG).show()

            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent.hasError()) {

                val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                Toast.makeText(applicationContext, "GeofenceTransitionsIntentService errorMessage $errorMessage !!", Toast.LENGTH_LONG).show()
                //display.append("GeofenceTransitionsIntentService errorMessage $errorMessage !!")
                Log.e(TAG, errorMessage)
                return
            }

            // Get the transition type.
            val geofenceTransition = geofencingEvent.geofenceTransition

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                val status: String = handleEnterExit(geofenceTransition)
                Log.d(TAG, status)
                //display.append("Sucess: "+status)
                Toast.makeText(applicationContext, "Sucess: "+status, Toast.LENGTH_LONG).show()
            } else {
                // Log the error.
                //display.append("Error transition code: "+geofenceTransition)
                Toast.makeText(applicationContext, "Error transition code: "+geofenceTransition, Toast.LENGTH_LONG).show()
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

                    else -> {
                        Log.d("geofence", "Unknow")
                        "Unknow"
                    }

                }
    }

}
