package abbacus.locationlibrary.demo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import abbacus.locationlibrary.LocationUpdateServiceBackground;

import static android.content.ContentValues.TAG;

public class locationReceiverService extends Service
{

    Context mContext;
    private MyReceiver myReceiver;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdateServiceBackground.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(mContext,"From my new Service"+ location.getLatitude(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreate() {
        mContext = this;
        myReceiver = new MyReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        } catch (Exception unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdateServiceBackground.ACTION_BROADCAST));
        return START_REDELIVER_INTENT;
    }

}
