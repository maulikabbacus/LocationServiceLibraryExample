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
	        implementation 'com.github.maulikabbacus:LocationServiceLibraryExample:1.0.0'
	}


How to Use this library

1> in your Activity or Service create Broadcast Receiver
    
    private MyReceiver myReceiver;
    
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
    
   - In oncreate()
   
          myReceiver = new MyReceiver();
          
  -  In OnStartCommand or onResume       
  
  		LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
        	new IntentFilter(LocationUpdateServiceBackground.ACTION_BROADCAST));
        
   - In onPause or onDestroy  
   
   		LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);     
   
   - To Start service:
            
	    	Intent ina = new Intent(MainActivity.this, LocationUpdateServiceBackground.class);
            	ina.putExtra("updateTimeInterval",1000);//in milliseconds(long data)
            	ina.putExtra("updateDistance",2f);//in meters(Float Data)
            	ina.putExtra("isDistanceRequired",false);//To enable minimum distance for location check
	        startService(ina);
