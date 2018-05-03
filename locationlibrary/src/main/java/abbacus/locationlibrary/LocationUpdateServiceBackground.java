package abbacus.locationlibrary;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationUpdateServiceBackground extends Service {
    private static final String TAG = LocationUpdateServiceBackground.class.getSimpleName();
    private static  long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static  float UPDATE_INTERVAL_IN_DISTANCE = 0.01f; //minimum distance for location update in meter
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static  long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mLocation;
    Context mContext;

    Task<LocationSettingsResponse> task;

    private static final String PACKAGE_NAME =
            "abbacus.locationlibrary";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mContext = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        } catch (Exception unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        if(intent!=null&&!intent.getExtras().isEmpty())
        {
            UPDATE_INTERVAL_IN_MILLISECONDS=intent.getLongExtra("updateTimeInterval",1000);
            UPDATE_INTERVAL_IN_DISTANCE=intent.getFloatExtra("updateDistance",1f);
            FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS=UPDATE_INTERVAL_IN_MILLISECONDS/2;
        }

        return START_REDELIVER_INTENT;
    }


    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setSmallestDisplacement(UPDATE_INTERVAL_IN_DISTANCE);
//        mLocationRequest.setSmallestDisplacement(UPDATE_INTERVAL_IN_DISTANCE);

        // Create LocationSettingsRequest object using location request
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(mLocationRequest);
//
//        LocationSettingsRequest locationSettingsRequest = builder.build();
//
//        // Check whether location settings are satisfied
//        SettingsClient settingsClient = LocationServices.getSettingsClient(LocationUpdateServiceBackground.this);
//
//        task = settingsClient.checkLocationSettings(locationSettingsRequest);
//        task.addOnSuccessListener( new OnSuccessListener<LocationSettingsResponse>() {
//            @Override
//            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//
//                Toast.makeText(LocationUpdateServiceBackground.this,"GPS Started",Toast.LENGTH_LONG).show();
//            }
//        });
//
//        task.addOnFailureListener( new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                if (e instanceof ResolvableApiException) {
//                    // Location settings are not satisfied, but this can be fixed
//                    // by showing the user a dialog.
//                    try {
//                        // Show the dialog by calling startResolutionForResult(),
//                        // and check the result in onActivityResult().
//                        Toast.makeText(LocationUpdateServiceBackground.this,"GPS Stopped",Toast.LENGTH_LONG).show();
//                        ResolvableApiException resolvable = (ResolvableApiException) e;
//                        PendingIntent intent=resolvable.getResolution();
//                        Intent ina=new Intent(mContext,MainActivity.class);
//                        ina.putExtra("resolution", intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(ina);
//
//                        mContext.startActivity(ina);
////
//                    } catch (Exception sendEx) {
//                        // Ignore the error.
//                    }
//                }
//            }
//        });
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        try {

            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());

        } catch (SecurityException unlikely) {
//          Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }

    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
//        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        Log.d("Updated Location:", msg);
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
//            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
//            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }
}
//    @Override
//    public void onGpsStatusChanged(int i) {
//        switch (i) {
//
//
//            case GpsStatus.GPS_EVENT_STOPPED :
//                Toast.makeText(mContext,"GPS Stopped",Toast.LENGTH_LONG).show();
//                break;
//
//            case GpsStatus.GPS_EVENT_STARTED:
//                Toast.makeText(mContext,"GPS Started",Toast.LENGTH_LONG).show();
//                break;
//
//        }
//    }
