package com.illnino.geofencingapi

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceTransitionsIntentService : IntentService("GeofenceTransitionsIntentService") {

    companion object {
        val TAG: String? = GeofenceTransitionsIntentService::class.simpleName
    }
    override fun onHandleIntent(intent: Intent?) {
    //override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "GeofenceTransitionsIntentService Fire !!")
        Toast.makeText(applicationContext, "GeofenceTransitionsIntentService Fire !!", Toast.LENGTH_LONG).show()

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {

            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Toast.makeText(applicationContext, "GeofenceTransitionsIntentService errorMessage $errorMessage !!", Toast.LENGTH_LONG).show()
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
            Toast.makeText(applicationContext, "Sucess: "+status, Toast.LENGTH_LONG).show()
        } else {
            // Log the error.
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