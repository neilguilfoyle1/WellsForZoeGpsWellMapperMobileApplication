package com.dit.neilguilfoyle.wellsforzoegps;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        new DataWellLoadTask().execute((Void) null);
    }

    /**
     * Represents an asynchronous well loading from the database
     */
    public class DataWellLoadTask extends AsyncTask<Void, Void, String> {

        ArrayList<LatLng> wellsLoaded;

        public DataWellLoadTask(){

        }

        @Override
        protected String doInBackground(Void... params) {
            String errorMessage = null;
            String username = "u1300377_wells4z";
            String password = "_Harrington22";
            try {
                DatabaseLoader dl = new DatabaseLoader(Common.getConnectionString(), username, password);
                wellsLoaded = dl.ReadAllWells();

            } catch (ClassNotFoundException e) {
                errorMessage = "Application Error";
                Log.e("DB Driver error", e.getMessage());
            } catch (SQLException e) {
                errorMessage = getString(R.string.check_internet);
                Log.e("DB error", e.getMessage());
            }
            return errorMessage;
        }

        @Override
        protected void onPostExecute(final String errorMessage) {
            if (errorMessage == null) {
                ArrayList<Marker> markers = new ArrayList<>();
                for(LatLng well : wellsLoaded){
                    markers.add(mMap.addMarker(new MarkerOptions().position(well)));
                }
                Toast.makeText(Common.Context, "Map Loaded", Toast.LENGTH_LONG).show();

                //center on markers
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker m : markers) {
                    builder.include(m.getPosition());
                }

                //in case we don't have any wells added center on Malawi
                if(markers.size() == 0) builder.include(new LatLng(-12.811, 34.013));

                LatLngBounds bounds = builder.build();
                int padding = 0; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
            } else {
                Toast.makeText(Common.Context, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}
