package abbacus.locationlibrary;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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
    private long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private float UPDATE_INTERVAL_IN_DISTANCE = 0f; //minimum distance for location update in meter
    private boolean IS_DISTANCE_REQUIRED = true; //minimum distance for location update in meter
    private boolean startServiceFlag = true;
    private String title = "";
    private String message = "";
    int smallicon, icon;
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    Context mContext;

    private static final String PACKAGE_NAME =
            "abbacus.locationlibrary";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcastLocation";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    NotificationManager mNotificationManager;
    Notification mNotification = null;
    public final static int NOTIFICATION_ID = 101516;
    String NOTIFICATION_CHANNEL_ID = "com.locaationdemo";
    String channelName = "Location_Service";
    public static final String UPDATE_INTERVAL = "updateTimeInterval";
    public static final String UPDATE_DISTANCE = "updateDistance";
    public static final String IS_DISTANCE_REQUIRED_FLAG = "isDistanceRequired";
    public static final String START_SERVICE_FLAG = "startServiceFlag";
    public static final String NOTIFICATION_TITLE = "title";
    public static final String NOTIFICATION_MESSAGE = "message";
    public static final String NOTIFICATION_SMALLICON = "smallIcon";
    public static final String NOTIFICATION_ICON = "icon";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_STATUS = "status";

    @Override
    public void onCreate() {
        mContext = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mFusedLocationClient = null;
        } catch (Exception unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && !intent.getExtras().isEmpty()) {
            startServiceFlag = intent.getBooleanExtra(START_SERVICE_FLAG, true);
            if(startServiceFlag) {
                UPDATE_INTERVAL_IN_MILLISECONDS = intent.getLongExtra(UPDATE_INTERVAL, 1000);
                UPDATE_INTERVAL_IN_DISTANCE = intent.getFloatExtra(UPDATE_DISTANCE, 1f);
                IS_DISTANCE_REQUIRED = intent.getBooleanExtra(IS_DISTANCE_REQUIRED_FLAG, true);
                startServiceFlag = intent.getBooleanExtra(START_SERVICE_FLAG, true);
                title = intent.getStringExtra(NOTIFICATION_TITLE);
                message = intent.getStringExtra(NOTIFICATION_MESSAGE);
                icon = intent.getIntExtra(NOTIFICATION_ICON, 0);
                smallicon = intent.getIntExtra(NOTIFICATION_SMALLICON, 0);
                FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS;
            }
        }
        if (startServiceFlag) {
            setasForeGroundService();
            startLocationUpdates();
        } else {
            stopForeground(true);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void setasForeGroundService() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (mNotification == null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                    mNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                // The user-visible name of the channel.
                CharSequence name = channelName;
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
                mChannel.enableVibration(false);
                mChannel.setSound(null, null);
                mNotificationManager.createNotificationChannel(mChannel);
            }

            mNotification = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(smallicon).setOngoing(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title)
                    .setContentText(message)
                    .build();
            mNotification.defaults = 0;
        }

        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            startForeground(NOTIFICATION_ID, mNotification);


    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        if (mFusedLocationClient == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            if (IS_DISTANCE_REQUIRED) {
                mLocationRequest.setSmallestDisplacement(UPDATE_INTERVAL_IN_DISTANCE);
            }


            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    onLocationChanged(locationResult.getLastLocation());
                }
            };

            try
            {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback, Looper.myLooper());

            } catch (SecurityException unlikely) {
                mFusedLocationClient=null;
                Intent broadCastIntent = new Intent();
                broadCastIntent.setAction(ACTION_BROADCAST);
                broadCastIntent.putExtra(EXTRA_LOCATION, unlikely.getMessage());
                broadCastIntent.putExtra(EXTRA_MESSAGE, "");
                broadCastIntent.putExtra(EXTRA_STATUS, 1);
                Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
            }
        }
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
//        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        Log.d("Updated Location:", msg);
        /*Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        intent.putExtra(EXTRA_MESSAGE, "");
        intent.putExtra(EXTRA_STATUS, 1);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
*/
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(ACTION_BROADCAST);
        broadCastIntent.putExtra(EXTRA_LOCATION, location);
        broadCastIntent.putExtra(EXTRA_MESSAGE, "");
        broadCastIntent.putExtra(EXTRA_STATUS, 1);
        sendBroadcast(broadCastIntent);

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
            stopSelf();
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }
}
