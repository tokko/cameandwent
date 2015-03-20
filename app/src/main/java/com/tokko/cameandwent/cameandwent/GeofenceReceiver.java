package com.tokko.cameandwent.cameandwent;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;


public class GeofenceReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    public static final String ACTION = "GEOFENCE_ACTION";
    public static final String ACTIVATE_GEOFENCE = "ACTIVATE_GEOFENCE";
    public static final String DEACTIVATE_GEOFENCE = "DEACTIVATE_GEOFENCE";

    private PendingIntent pendingIntent;
    private GoogleApiClient googleApiClient;
    private GeofencingRequest request;
    private boolean enabled;
    private Context context;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, GeofenceReceiver.class).setAction(ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
        if(intent.getAction().equals(ACTIVATE_GEOFENCE)){
            registerGeofence();
        }
        else if(intent.getAction().equals(DEACTIVATE_GEOFENCE)){
            registerGeofence();
        }
        else if(intent.getAction().equals(ACTION)) {
            if(!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enabled", true)) return;
            GeofencingEvent event = GeofencingEvent.fromIntent(intent);
            Log.d("recvr", "Intent fired");
            int transition = event.getGeofenceTransition();
            ClockManager cm = new ClockManager(context);
            if(transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d("recvr", "entered");
                List<Geofence> triggerList = event.getTriggeringGeofences();
                for (Geofence fence : triggerList){
                    cm.clockIn(Integer.valueOf(fence.getRequestId().split("/")[1]));
                }
            }
            else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d("recvr", "exited");
                cm.clockOut();
            }
        }
        else
            throw new IllegalStateException("Unknown action for service: " + intent.getAction());
    }

    public void registerGeofence() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        enabled = sp.getBoolean("enabled", false);
        String radiuS = sp.getString("radius", null);
     	//String[] location = sp.getString("origin", "").split(";");
        if (!enabled || radiuS == null) return;
        Cursor c  = context.getContentResolver().query(CameAndWentProvider.URI_TAGS, null, null, null, null);
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
           long id = c.getLong(c.getColumnIndex(CameAndWentProvider.ID));
           double longitude = c.getDouble(c.getColumnIndex(CameAndWentProvider.LONGITUDE));
           double latitude = c.getDouble(c.getColumnIndex(CameAndWentProvider.LATITUDE));
           if(longitude != -1 && latitude != -1) {
                float radii = Float.parseFloat(radiuS);
                Geofence.Builder builder = new Geofence.Builder();
                builder.setCircularRegion(latitude, longitude, radii);
                builder.setRequestId("com.tokko.cameandwent/"+id);
                builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
                builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
                Geofence fence = builder.build();
                GeofencingRequest.Builder requestBuilder = new GeofencingRequest.Builder();
                requestBuilder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
                requestBuilder.addGeofence(fence);
                request = requestBuilder.build();
                googleApiClient = new GoogleApiClient.Builder(context)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                googleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, pendingIntent);
        if(enabled) {
            LocationServices.GeofencingApi.addGeofences(googleApiClient, request, pendingIntent);
        }
        googleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(Status status) {
        if(status.isSuccess())
            Toast.makeText(context, "Geofence added", Toast.LENGTH_SHORT).show();
        if(!status.isSuccess())
            Toast.makeText(context, "Geofence add failed", Toast.LENGTH_SHORT).show();
        if(status.isCanceled())
            Toast.makeText(context, "Geofence add canceled", Toast.LENGTH_SHORT).show();
        if(status.isInterrupted())
            Toast.makeText(context, "Geofence add interrupted", Toast.LENGTH_SHORT).show();

    }

}
