package abbacus.locationlibrary.demo;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private boolean mBound = false;

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    TextView tvLocation;
    private Handler handler1;

    MapView mapView;
    GoogleMap googleMap;
    private Marker marker, userPositionMarker;
    boolean cameraAnimated = false;
    private ValueAnimator valueAnimator;
    long animationDuration = 3000;
    int minDistanceForAnimation = 2;
    private float v;
    private double lng;
    private double lat,temp=0.0001;
    private static  int ZOOM_LEVEL = 16;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleMap != null) {
            if (cameraAnimated) {
                handler1 = null;
                mapView.setVisibility(View.VISIBLE);
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

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

        MapsInitializer.initialize(this);


        mapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                if(checkPermissions()) {
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
                registerLocationChangeReceiver();
            }
        });

    }

    private void stopLocationService() {
        Intent ina = new Intent(MainActivity.this, LocationUpdateServiceBackground.class);
        ina.putExtra(LocationUpdateServiceBackground.START_SERVICE_FLAG, false);
        stopService(ina);
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
            ina.putExtra(LocationUpdateServiceBackground.UPDATE_INTERVAL, 2000L);//in milliseconds
            ina.putExtra(LocationUpdateServiceBackground.UPDATE_DISTANCE, 10f);//in meters
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


    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }

    private void registerLocationChangeReceiver() {
        LocationChangeReceiver mReceiver = new LocationChangeReceiver();
        registerReceiver(mReceiver, new IntentFilter(LocationUpdateServiceBackground.ACTION_BROADCAST));
    }

    public class LocationChangeReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdateServiceBackground.EXTRA_LOCATION);
            String message = intent.getStringExtra(LocationUpdateServiceBackground.EXTRA_MESSAGE);
            int status = intent.getIntExtra(LocationUpdateServiceBackground.EXTRA_STATUS, 0);
            if (status == 1) {
                if (location != null)
                {
                    temp+=0.0001;
                    location.setLatitude(location.getLatitude()+temp);
                    location.setLongitude(location.getLongitude()+temp);
                    updateMapMarkerPosition(location);
                }
            } else {
                tvLocation.setText(message);
            }

        }
    }

    private void updateMapMarkerPosition(final Location location)
    {
        try
        {
            temp+=0.0001;
                Debug.trace("Receiver" + location.getLatitude() + "," + location.getLongitude());
                if (location != null) {
                    if (handler1 != null) {
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                final LatLng endPosition = new LatLng(location.getLatitude(), location.getLongitude());
                                if (marker == null) {
                                    marker = googleMap.addMarker(new MarkerOptions().position(endPosition).title("Demo").icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker))));
                                }

                                final LatLng startPosition = marker.getPosition();

                                Location startLocation = new Location("A");
                                startLocation.setLatitude(startPosition.latitude);
                                startLocation.setLongitude(startPosition.longitude);

                                Location endLocation = new Location("B");
                                endLocation.setLatitude(endPosition.latitude);
                                endLocation.setLongitude(endPosition.longitude);
                                double distance = startLocation.distanceTo(endLocation);
                                if (distance > 1) {
                                    valueAnimator = ValueAnimator.ofFloat(0, 1);
                                    valueAnimator.setDuration(animationDuration); //dg
                                    valueAnimator.setInterpolator(new LinearInterpolator());
                                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator valueAnimator) {

                                            v = (float) valueAnimator.getAnimatedValue();
                                            lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                                            lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                                            LatLng newPos = new LatLng(lat, lng);
                                            marker.setPosition(newPos);

                                        }
                                    });
                                    valueAnimator.start();
                                }
                            }
                        }, 16);
                    } else {
                        final LatLng sydney1 = new LatLng(location.getLatitude(), location.getLongitude());
                        if (marker == null) {
                            marker = googleMap.addMarker(new MarkerOptions().position(sydney1).title("Demo").icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker))));
                        } else {
                            marker.setPosition(sydney1);
                        }
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney1, ZOOM_LEVEL), (int) 2000, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                //Here you can take the snapshot or whatever you want
                                cameraAnimated = true;
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                        handler1 = new Handler();
                    }
                }

        }
        catch (Exception ex)
        {
            Debug.trace(ex.toString());
        }

    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_icon, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.marker_image);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }


}
