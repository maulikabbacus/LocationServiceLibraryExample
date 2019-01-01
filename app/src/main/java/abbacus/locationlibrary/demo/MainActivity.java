package abbacus.locationlibrary.demo;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.Manifest;

import android.content.pm.PackageManager;

import android.net.Uri;

import android.provider.Settings;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import abbacus.locationlibrary.LocationUpdateServiceBackground;


/**
 * The only activity in this sample.
 * <p>
 * Note: for apps running in the background on "O" devices (regardless of the targetSdkVersion),
 * location may be computed less frequently than requested when the app is not in the foreground.
 * Apps that use a foreground service -  which involves displaying a non-dismissable
 * notification -  can bypass the background location limits and request location updates as before.
 */
public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
//    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
//    private LocationUpdatesService mService = null;
//    private LocationUpdateServiceBackground mServiceBG = null;
    // Tracks the bound state of the service.
    private boolean mBound = false;

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    static TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.request_location_updates_button);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.remove_location_updates_button);
        tvLocation = (TextView) findViewById(R.id.tv_location);

        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    startServiceIntent();

                }

            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//              mService.removeLocationUpdates();
                mRequestLocationUpdatesButton.setEnabled(true);
                mRemoveLocationUpdatesButton.setEnabled(false);
                stopLocationService();
            }
        });

    }

    private void stopLocationService() {
        Intent ina = new Intent(MainActivity.this, LocationUpdateServiceBackground.class);
        ina.putExtra(LocationUpdateServiceBackground.START_SERVICE_FLAG, false);
        stopService(ina);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
//                new IntentFilter(LocationUpdateServiceBackground.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {

            mBound = false;
        }

        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                startServiceIntent();

            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    private void startServiceIntent() {
        if (CheckGpsStatus()) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
            Intent ina = new Intent(MainActivity.this, LocationUpdateServiceBackground.class);
            ina.putExtra(LocationUpdateServiceBackground.UPDATE_INTERVAL, 3000);//in milliseconds
            ina.putExtra(LocationUpdateServiceBackground.UPDATE_DISTANCE, 0.01f);//in meters
            ina.putExtra(LocationUpdateServiceBackground.IS_DISTANCE_REQUIRED_FLAG, false);//To enable minimum distance for location check
            ina.putExtra(LocationUpdateServiceBackground.START_SERVICE_FLAG, true);//To start or stop service
            ina.putExtra(LocationUpdateServiceBackground.NOTIFICATION_TITLE, "Demo");//Title of foreground notification
            ina.putExtra(LocationUpdateServiceBackground.NOTIFICATION_MESSAGE, "Location");//Message for foreground notification
            ina.putExtra(LocationUpdateServiceBackground.NOTIFICATION_ICON, R.mipmap.ic_launcher_round);//Message for foreground notification
            ina.putExtra(LocationUpdateServiceBackground.NOTIFICATION_SMALLICON, R.mipmap.ic_launcher_round);//Message for foreground notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(ina);
            } else {
                startService(ina);
            }

        } else {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                startServiceIntent();
                break;
        }
    }

    public boolean CheckGpsStatus() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     */
//    private class MyReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Location location = intent.getParcelableExtra(LocationUpdateServiceBackground.EXTRA_LOCATION);
//            if (location != null) {
//                Toast.makeText(MainActivity.this, (int) location.getLatitude(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
//        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
//            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
//                    false));
//        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }

    public static class LocationChangeReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdateServiceBackground.EXTRA_LOCATION);
            String message = intent.getStringExtra(LocationUpdateServiceBackground.EXTRA_MESSAGE);
            int status = intent.getIntExtra(LocationUpdateServiceBackground.EXTRA_STATUS, 0);
            if (status == 1) {
                if (location != null) {
                    tvLocation.setText(location.getLatitude() + "\n" + location.getLongitude());
                }
            } else {
                tvLocation.setText(message);
            }

        }


    }

}
