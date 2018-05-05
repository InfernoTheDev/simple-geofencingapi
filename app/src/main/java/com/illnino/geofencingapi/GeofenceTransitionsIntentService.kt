package com.illnino.geofencingapi

import android.app.IntentService
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

        Toast.makeText(this, "GeofenceTransitionsIntentService Fire !!", Toast.LENGTH_LONG).show()

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {

            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Toast.makeText(this, "GeofenceTransitionsIntentService errorMessage $errorMessage !!", Toast.LENGTH_LONG).show()
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            val geofenceTransitionDetails = GeofenceStatusCodes.getStatusCodeString(geofenceTransition)

            // Send notification and log the transition details.
            Log.d(TAG, geofenceTransitionDetails)
            Toast.makeText(this, "Sucess: "+geofenceTransitionDetails, Toast.LENGTH_LONG).show()
        } else {
            // Log the error.
            Toast.makeText(this, "Error: "+GeofenceStatusCodes.getStatusCodeString(geofenceTransition), Toast.LENGTH_LONG).show()
            Log.d(TAG, GeofenceStatusCodes.getStatusCodeString(geofenceTransition))
        }
    }
}