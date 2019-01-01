package abbacus.locationlibrary.demo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.IntentFilter;


import abbacus.locationlibrary.LocationUpdateServiceBackground;


public class MyApplication extends Application {

    public static final String TAG = MyApplication.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    public static MyApplication appInstance;


    public static synchronized MyApplication getInstance() {
        return appInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;

            registerLocationChangeReceiver();


    }

    private void registerLocationChangeReceiver()
    {
            MainActivity.LocationChangeReceiver mReceiver=new MainActivity.LocationChangeReceiver();
            registerReceiver(mReceiver, new IntentFilter(LocationUpdateServiceBackground.ACTION_BROADCAST));
    }


}