package com.dit.neilguilfoyle.wellsforzoegps;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

public class GpsLoadActivity extends ActionBarActivity {

    public LocationManager LocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_load);
        LocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        Common.Context = this;
        if (savedInstanceState == null) {
            PlaceholderFragment p = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, p)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gps_load, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        // Acquire a reference to the system Location Manager
        private LocationManager locationManager;
        private Location lastLocation;
        private LocationListener locationListener;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //get the real view
            View rootView = inflater.inflate(R.layout.fragment_gps_load, container, false);
            locationManager = Common.Context.LocationManager;

            //get the image button to add click listener
            ImageButton ib = (ImageButton)rootView.findViewById(R.id.imageButton);
            ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //select best criteria to get coordinates
                    Criteria c = new Criteria();
                    c.setAccuracy(Criteria.ACCURACY_FINE);
                    c.setAltitudeRequired(false);
                    c.setBearingRequired(false);
                    c.setSpeedRequired(false);
                    c.setCostAllowed(true);
                    c.setPowerRequirement(Criteria.POWER_HIGH);

                    //get parent to get textbox to display cooridinates
                    View parent = v.getRootView();
                    final TextView textBox = ((TextView)parent.findViewById(R.id.gpsResultText));

                    //get the best provider based on criteria above
                    String providerName = locationManager.getBestProvider(c, false);

                    //notify user the coordinates are actually being obtained
                    textBox.setText("Getting location, please wait");

                    //make a listener for the location updates
                    locationListener = new LocationListener() {
                        int counter = 0;

                        //whenever new location is found this method is called
                        public void onLocationChanged(Location location) {
                            lastLocation = location;
                            //convert to string
                            String coords = lastLocation != null ? lastLocation.getLatitude()+", "+lastLocation.getLongitude() : "No location found yet";

                            //if updated 10 times, settle on final result
                            if(counter++ > 10) {
                                locationManager.removeUpdates(this);
                                textBox.setText("Final: "+coords);

                            }
                            //if we didn't get 10 updates yet, update text but keep waiting
                            else {
                                textBox.setText("Refining Please Wait ...\n"+coords);
                            }
                        }

                        public void onStatusChanged(String provider, int status, Bundle extras) {}

                        public void onProviderEnabled(String provider) {}

                        public void onProviderDisabled(String provider) {}
                    };

                    //if we have a provider selected and it is enabled then start listening for location
                    //updates with listener above
                    if (providerName != null && locationManager.isProviderEnabled(providerName)) {
                        // Provider is enabled
                        locationManager.requestLocationUpdates(providerName, 0,0,locationListener);
                    } else {
                        // Provider not enabled, prompt user to enable it
                        Toast.makeText(v.getContext(), R.string.please_turn_on_gps, Toast.LENGTH_LONG).show();
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                }
            });

                //get add coordinates button click
                Button addButton = (Button)rootView.findViewById(R.id.addbutton);
                addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get parent to get textbox to get cooridinates
                    View parent = v.getRootView();
                    final TextView textBox = ((TextView)parent.findViewById(R.id.gpsResultText));
                    String resultText = textBox.getText().toString();
                    if(resultText.startsWith("Final:")){
                        String coordinatePair = resultText.substring("Final: ".length());
                        String[] pair = coordinatePair.split(",");

                        Common.Lat = StringToCoordinateConverter.DDToCoord(Double.parseDouble(pair[0].trim()), "lat");
                        Common.Lng = StringToCoordinateConverter.DDToCoord(Double.parseDouble(pair[1].trim()), "lng");

                        Intent intent = new Intent(Common.Context, DetailDialog.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(v.getContext(), "Please get location or wait if loading", Toast.LENGTH_LONG).show();
                    }
                }
            });

            Button mapButton = (Button)rootView.findViewById(R.id.mapbutton);
            mapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try{
                        Intent intent = new Intent(Common.Context, MapsActivity.class);
                        startActivity(intent);
                    }catch (Exception e) {
                        Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("Map error", e.getMessage());
                    }
                }
            });

            return rootView;
        }
    }
}
