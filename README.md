# LocationServiceLibraryExample
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.maulikabbacus:LocationServiceLibraryExample:1.4
	}


How to Use this library

1> in your Activity or Service create Broadcast Receiver
    
    private LocationChangeReceiver myReceiver;
    
   public  class LocationChangeReceiver extends BroadcastReceiver {
          public void onReceive(Context context, Intent intent) {
              Location location = intent.getParcelableExtra(LocationUpdateServiceBackground.EXTRA_LOCATION);
              String message = intent.getStringExtra(LocationUpdateServiceBackground.EXTRA_MESSAGE);
              int status = intent.getIntExtra(LocationUpdateServiceBackground.EXTRA_STATUS, 0);
              if (status == 1) {
                  if (location != null) {
                      Toast.makeText(context,"Changed",Toast.LENGTH_SHORT).show();
                  }
              } else {

              }

          }


      }
    
   - In oncreate()
   
          myReceiver = new LocationChangeReceiver();
          
  -  In OnStartCommand or onResume       
  
  		LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
        	new IntentFilter(LocationUpdateServiceBackground.ACTION_BROADCAST));
        
   - In onPause or onDestroy  
   
   		LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);     
   
   - To Start service:
            
	    	Intent ina = new Intent(MainActivity.this, LocationUpdateServiceBackground.class);
                        ina.putExtra(LocationUpdateServiceBackground.UPDATE_INTERVAL, 3000);//in milliseconds
                        ina.putExtra(LocationUpdateServiceBackground.UPDATE_DISTANCE, 0.01f);//in meters
                        ina.putExtra(LocationUpdateServiceBackground.IS_DISTANCE_REQUIRED_FLAG, false);//To enable minimum distance for location check
                        ina.putExtra(LocationUpdateServiceBackground.START_SERVICE_FLAG, true);//To start or stop service
                        ina.putExtra(LocationUpdateServiceBackground.NOTIFICATION_TITLE, "Demo");//Title of foreground notification
                        ina.putExtra(LocationUpdateServiceBackground.NOTIFICATION_MESSAGE, "Location");//Message for foreground notification
                        ina.putExtra(LocationUpdateServiceBackground.NOTIFICATION_ICON, R.mipmap.ic_launcher_round);//Icon for foreground notification
                        ina.putExtra(LocationUpdateServiceBackground.NOTIFICATION_SMALLICON, R.mipmap.ic_launcher_round);//Small icon for foreground notification
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(ina);
                        } else {
                            startService(ina);
                        }
		
		
- in manifest file Add
 			
			<service
           		 android:name="abbacus.locationlibrary.LocationUpdateServiceBackground"
           		 android:exported="true"
           		 android:enabled="true"
            		android:stopWithTask="false"
            		/>
			
			
			
			 Add in module level build.gradle dependancy
			 implementation 'com.google.android.gms:play-services-location:11.0.0'
