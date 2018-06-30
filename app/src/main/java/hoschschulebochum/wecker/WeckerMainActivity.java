package hoschschulebochum.wecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.Calendar;

import hoschschulebochum.wecker.data.Dateimanager;
import hoschschulebochum.wecker.data.UserData;
import hoschschulebochum.wecker.data.Wecker;

public class WeckerMainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    //Wecker
    private PendingIntent pendingIntent;
    private TimePicker alarmTimePicker;
    private static WeckerMainActivity inst;
    private ToggleButton alarmToggle;
    Button buttonOff;
    Button buttonSnooze;
    // alarm settings
    public static final int snoozeTimeInMinutes = 1;

    private final static int RequestCode = 1;
    private static final String TAG = "WeckerMainActivity";


    private Vibrator vibrator;
    private int snoozeCounter;
    private Ringtone ringtone;
    //Location
    Marker startMarker;
    MapView map = null;

//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
//                    return true;
//                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
//                    return true;
//                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
//                    return true;
//            }
//            return false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setContentView(R.layout.activity_wecker_main);

        //   Intent myIntent = new Intent(this, AlarmReceiver.class);
        // pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);

        // get the snoozeCounter value from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            snoozeCounter = extras.getInt("SNOOZE_COUNTER");
        }
        Log.d(TAG, "onCreate() - snoozeCounter = " + snoozeCounter);
        getIds();
        setUpButtonListener();
        //-----
        //SetUpWecker
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        Log.d("runnable", "pruefeAlarm");
        if (extras != null) {
            boolean start_alarm = extras.getBoolean("START_ALARM");
            if(start_alarm)
            {
                // execute alarm sound and vibration async task
                new SoundAlarm().execute();
                extras.putBoolean("START_ALARM" , false);
            }
        }
        //-----
        setUpLocation();

    }

    private void setUpLocation() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        //TODO GPS auslesen und übergeben
        GeoPoint startPoint = new GeoPoint(51.446905, 7.271703);
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        mapController.setCenter(startPoint);

        //https://github.com/osmdroid/osmdroid/wiki/Markers,-Lines-and-Polygons
       // https://github.com/MKergall/osmbonuspack/wiki/Tutorial_0
        final Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Home");
        map.getOverlays().add(startMarker);
        //refresh the map
        map.invalidate();


        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
              // Toast.makeText(getBaseContext(),p.getLatitude() + " - "+p.getLongitude(),Toast.LENGTH_LONG).show();

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Toast.makeText(getBaseContext(),"Ort Zuhause geändert: "+p.getLatitude() + " - "+p.getLongitude(),Toast.LENGTH_LONG).show();
                startMarker.setPosition(p);
                map.invalidate();
                return false;
            }
        };



        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEvents);
        //https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library


    }



    @Override
    public void onStart() {
        Log.d("runnable", "Weckermain on start");
        super.onStart();
        inst = this;
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void setUpButtonListener() {
        buttonOff.setBackgroundColor(Color.rgb(0, 150, 150));
        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmOff();
            }
        });
        buttonSnooze.setBackgroundColor(Color.rgb(0, 150, 150));
        buttonSnooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmSnooze();
            }
        });
    }

    private void getIds()
    {
        mTextMessage = (TextView) findViewById(R.id.message);

//        //NavigationBar -- delete
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Wecker
        alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);
        alarmToggle = (ToggleButton) findViewById(R.id.alarmToggle);

         buttonOff = (Button) findViewById(R.id.button_alarm_off);
         buttonSnooze = (Button) findViewById(R.id.button_alarm_snooze);


    }

    public void onToggleClicked(View view) {
        if (alarmToggle.isChecked()) {
            Log.d("MyActivity", "Alarm On");
            saveAlarmDate(true);

        } else {
            saveAlarmDate(false);
            Log.d("MyActivity", "Alarm Off");
        }
    }

    private void saveAlarmDate(Boolean angeschaltet) {
        UserData userData = new UserData();
        userData.removeAll();
        Log.d("WeckerMainActivity", "saveAlarmDate with " + alarmTimePicker.getHour() + ":" + alarmTimePicker.getMinute());
        Wecker wecker = new Wecker(alarmTimePicker.getHour(), alarmTimePicker.getMinute(), angeschaltet );
        userData.addAlarm(wecker);
        SharedPreferences mPrefs= getSharedPreferences("Wecker",Context.MODE_PRIVATE);
        new Dateimanager().saveUserData(mPrefs,userData);
        startService(new Intent(this, AlarmService.class));
    }




    /**
     * Method used to deactivate the alarm after the user used the "Wake up"
     * button.
     */
    private void alarmOff() {
        new StopAlarm().execute();
        Log.d(TAG, "alarmOff() - snoozeCounter = " + snoozeCounter);
        // new PostEventTask().execute(); FIXME
        snoozeCounter = 0;
        resetAlarmService();
        finish();
    }

    /**
     * Method used to deactivate and reschedule the alarm after the user used
     * the "Snooze" button.
     */
    private void alarmSnooze() {
        new StopAlarm().execute();
        snoozeCounter++;
        Log.d(TAG, "alarmSnooze() - snoozeCounter = " + snoozeCounter);
        // setup next snooze alarm time
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, snoozeTimeInMinutes);
        long nextSnoozeTime = c.getTimeInMillis();
        // set new snooze alarm
        Intent intent = new Intent(getApplicationContext(),
                AlarmReceiver.class);
        intent.putExtra("SNOOZE_COUNTER", snoozeCounter);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), RequestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        Log.d(TAG, "Snooze set to: " + c.getTime().toString());
        alarmManager
                .set(AlarmManager.RTC_WAKEUP, nextSnoozeTime, pendingIntent);
        finish();
    }

    /**
     * Cancel any AlarmService tasks and run then again with renewed parameters
     * (new alarm date and time).
     */
    private void resetAlarmService() {
        Log.d(TAG, "restart AlarmService");
        Intent intent = new Intent(getApplicationContext(), AlarmService.class);
        stopService(intent);
        startService(intent);
    }

    /**
     * AsynTask used for starting the alarm sound and vibration.
     *
     * @author Mateusz Renes
     */
    private class SoundAlarm extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {


            ringtone.play();

            Log.d(TAG, "Alarm received!");

            long[] pattern = { 1000, 1000 };
            vibrator.vibrate(pattern, 0);
            return null;
        }
    }

    /**
     * AsynTask used for stopping the alarm sound and vibration.
     *
     * @author Mateusz Renes
     */
    private class StopAlarm extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... params) {
           ringtone.stop();
           vibrator.cancel();
            return null;
        }
    }
}
