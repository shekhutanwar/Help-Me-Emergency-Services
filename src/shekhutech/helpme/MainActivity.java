package shekhutech.helpme;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {

    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private String locationProvider = null;
    private Location lastKnownLocation = null;
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private ImageButton imgPanicBtn = null;
    private View relativeView = null;
    private ObjectAnimator objectAnimator = null;
    private TextView txtLocation = null;
    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter
            if (mAccel > 12) {
                Toast toast = Toast.makeText(getApplicationContext(), "Device has shaken.", Toast.LENGTH_SHORT);
                toast.show();
                startLocationListener(locationManager,locationListener);
            }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializations
        // Get reference of Layout of MainActivity
        relativeView = findViewById(R.id.mainLayout);
        txtLocation = (TextView) findViewById(R.id.txtLocation);
        objectAnimator = ObjectAnimator.ofObject(relativeView,"backgroundColor",new ArgbEvaluator(), getResources().getColor(R.color.white), getResources().getColor(R.color.red));
        objectAnimator.setDuration(1000);
        // Get Reference of ImageButton to imgPanicBtn variable
        imgPanicBtn = (ImageButton) findViewById(R.id.imgPanic);
        // Get location Provider
        locationProvider = LocationManager.NETWORK_PROVIDER;
        // get location service to location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // get last known location from cache to show initial location while app is fetching location
        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        //detect shake
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        // add a location listener to show updated location coordinates
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // display the latitude and longitude in logcat
                Log.i("MainActivity","Latitude = " + location.getLatitude());
                Log.i("MainActivity","Longitude = " + location.getLongitude());
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    Address address = addressList.get(0);
                    //StringBuilder strAddress = new StringBuilder();
                    txtLocation.setText("You are in " + address.getSubLocality() + ", " + address.getLocality() + ", " + address.getCountryName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    System.out.println("Stopping Location Service");
                    stopLocationListener(locationManager, locationListener);
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        // display last known location
        Log.i("MainActivity","Last Known Location is " + "Latitude = " + lastKnownLocation.getLatitude() + "Longitude = " + lastKnownLocation.getLongitude());
        Toast.makeText(MainActivity.this, "Getting Location, Check logcat for location coordinates", Toast.LENGTH_SHORT).show();
        // Set onClick listener to imgPanicButton
        imgPanicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start location listener to receive location coordinates on button click
                startLocationListener(locationManager,locationListener);
                objectAnimator.start();

                //relativeView.setBackgroundColor(getResources().getColor(R.color.red));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    private void startLocationListener(LocationManager locationManager,LocationListener locationListener) {
        Log.i("MainActivity","Starting Location Receiver");
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
    }

    private void stopLocationListener(LocationManager locationManager,LocationListener locationListener){
        Log.i("MainActivity", "Stopping Location Receiver");
        locationManager.removeUpdates(locationListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this,"Settings will be soon available",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
