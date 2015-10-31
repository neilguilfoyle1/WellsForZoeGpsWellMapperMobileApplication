package com.dit.neilguilfoyle.wellsforzoegps;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.sql.Date;
import java.util.Calendar;


public class DetailDialog extends ActionBarActivity {

    private boolean addingwell;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_dialog);

        //get add coordinates button click
        Button addButton = (Button)this.findViewById(R.id.add_well_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get parent
                if(addingwell) return;
                addingwell = true;
                View parent = v.getRootView();

                String lat = Common.Lat;
                String lng = Common.Lng;

                String desc = ((EditText)parent.findViewById(R.id.extra_desc)).getText().toString();
                String village = ((EditText)parent.findViewById(R.id.extra_village)).getText().toString();
                String level = ((EditText)parent.findViewById(R.id.extra_water_level)).getText().toString();
                String depth = ((EditText)parent.findViewById(R.id.extra_well_depth)).getText().toString();
                String replacedBy = ((EditText)parent.findViewById(R.id.extra_valve_by)).getText().toString();
                Date replaceDate = getDateFromDatePicker((DatePicker)parent.findViewById(R.id.extra_valve_date));

                Double levelValue = StringToCoordinateConverter.toDouble(level);
                if (levelValue == null)
                {
                    Toast.makeText(v.getContext(), "Water level must be empty or number", Toast.LENGTH_LONG).show();
                    return;
                }
                Double depthValue = StringToCoordinateConverter.toDouble(depth);
                if (depthValue == null)
                {
                    Toast.makeText(v.getContext(), "Depth must be empty or number", Toast.LENGTH_LONG).show();
                    return;
                }

                new DataWellAddTask(lng, lat, desc, village, levelValue, depthValue, replacedBy, replaceDate).execute((Void) null);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_dialog, menu);
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
     * Represents an asynchronous well addition task used to the database
     */
    public class DataWellAddTask extends AsyncTask<Void, Void, String> {

        private final String mLongitude;
        private final String mLatitude;
        private String mDesc;
        private String mVillage;
        private Double mLevel;
        private Double mDepth;
        private String mReplacedBy;
        private Date mReplaceDate;

        DataWellAddTask(String longitude, String latitude, String desc,
                        String village, Double level,
                        Double depth, String replacedBy, Date replaceDate) {
            mLongitude = longitude;
            mLatitude = latitude;
            mDesc = desc;
            mVillage = village;
            mLevel = level;
            mDepth = depth;
            mReplacedBy = replacedBy;
            mReplaceDate = replaceDate;
        }

        @Override
        protected String doInBackground(Void... params) {
            String errorMessage = null;
            String username = "u1300377_wells4z";
            String password = "_Harrington22";
            try {
                DatabaseLoader dl = new DatabaseLoader(Common.getConnectionString(), username, password);
                try {
                    dl.InsertWellData(mLongitude, mLatitude, mDesc,
                            mVillage, mLevel, mDepth, mReplacedBy, mReplaceDate);
                }catch (SQLException e){
                    dl.AddVillage(mVillage);
                    dl.InsertWellData(mLongitude, mLatitude, mDesc,
                            mVillage, mLevel, mDepth, mReplacedBy, mReplaceDate);
                }
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
                Toast.makeText(Common.Context, "New Well has been added", Toast.LENGTH_LONG).show();
                TextView textBox = ((TextView)Common.Context.findViewById(R.id.gpsResultText));
                textBox.setText("Added well at "+textBox.getText());
                finish();
            } else {
                Toast.makeText(Common.Context, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Edited http://stackoverflow.com/questions/8409043/getdate-from-datepicker-android
     * @param datePicker
     * @return a java.sql.Date
     */
    public static java.sql.Date getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return new java.sql.Date(calendar.getTime().getTime());
    }
}
