package hoschschulebochum.wecker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
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
import java.util.concurrent.ExecutionException;

import hoschschulebochum.ContextAuswertung.WetterContext;
import hoschschulebochum.souldRing.MyLocationListener;
import hoschschulebochum.wecker.data.Dateimanager;
import hoschschulebochum.wecker.data.MyLocation;
import hoschschulebochum.wecker.data.UserData;

public class WeckerMainActivity extends AppCompatActivity {

    private static java.util.concurrent.locks.ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();


    private TextView mTextMessage;

    //Wecker
    private PendingIntent pendingIntent;
    private TimePicker alarmTimePicker;
    private static WeckerMainActivity inst;
    private ToggleButton alarmToggle;
    Button buttonOff;
    Button buttonSnooze;

    private TextView alarmText;
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
    SensorManager sensorManager;
    SensorEventListener sensorEventListener;
    LocationManager locationManager;
    MyLocationListener locationListener;
    private double aktuelleLatitude;
    private double aktuelleLongitude;

    /**
     * Gibt wieder ob das Telefon Momentan klingeln soll
     */
    private boolean doesRingAtMoment=false;
    private float anfangsLichtwert=-100;
    private float aktuellerLichtwert=-100;
    private float[] anfangsBewegung= new float[]{-100,-100,-100};
    private float[] aktuelleBewegung= new float[]{-100,-100,-100};


    private UserData userData;
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setContentView(R.layout.activity_wecker_main);

        mPrefs= getSharedPreferences("Wecker",Context.MODE_PRIVATE);
        userData = new Dateimanager().loadUserData(mPrefs);
        //Bei der erst Benutzung der App können die UserData null sein
        if(userData==null)
        {
            userData= new UserData();
        }

        Log.d(TAG, "onCreate() - snoozeCounter = " + snoozeCounter);
        getIds();
        loadAlarmSettings_SetToGui();
        setUpWecker();
        setUpButtonListener();
        setUpSensorManager();
        setUpLocation();
        registerListener();


    }

    /**
     * lädt die persistierten Daten und fügt setzt diese auf der GUI
     */
    private void loadAlarmSettings_SetToGui()
    {
        alarmTimePicker.setHour(userData.getFirstAlarm().getHour());
        alarmTimePicker.setMinute(userData.getFirstAlarm().getMinutes());
        WetterContext wetterContext = new WetterContext();
        AsyncTask<Object, Void, String> execute = wetterContext.execute();
        String info="";
        try {
            info= execute.get();
            alarmText.setText(info);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    private void setUpWecker()
    {
        // get the snoozeCounter value from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            snoozeCounter = extras.getInt("SNOOZE_COUNTER");
        }

        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        alarmTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                userData.getFirstAlarm().setHour(hourOfDay);
                userData.getFirstAlarm().setMinutes(minute);
                alarmToogleAusschalten();
                saveAlarm();
            }
        });

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
    }

    private void setUpLocation() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        //TODO initial wird immer bei Neuanlage des Weckers als Lokation die Hochschule Bochum gesetzt
        //TODO Statdessen sollte, sofern der Benuter nichts gesetzt hat, die GPS koordinaten genommen werden

        //Gespeicherte lokation Laden und die Lokation in der Map Laden
        MyLocation home = userData.getFirstAlarm().getHome();
        GeoPoint startPoint = new GeoPoint(home.getLatitude(), home.getLongitude());
        IMapController mapController = map.getController();
        mapController.setZoom(16);
        mapController.setCenter(startPoint);

        //Fügt den Marker, den Markierten Punkt hinzu
        final Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Home");
        map.getOverlays().add(startMarker);
        //refresh the map
        map.invalidate();

        //Fügt ein Event hinzu, dass auf ein langes klicken auf der Karte reagiert um den Marker zu verschieben
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Toast.makeText(getBaseContext(),"Ort Zuhause geändert: "+p.getLatitude() + " - "+p.getLongitude(),Toast.LENGTH_LONG).show();
                startMarker.setPosition(p);
                map.invalidate();
                userData.getFirstAlarm().setHome(new MyLocation("Home", p.getLatitude(),p.getLongitude()));
                alarmToogleAusschalten();
                saveAlarm();
                return false;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEvents);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(this,getApplicationContext(), getAktuelleLatitude(), getAktuelleLongitude());

    }
    private void setUpSensorManager() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()) {
                    //Beschleunigungssensor Drehmoment Winkelgeschwindigkeit
                    case Sensor.TYPE_GYROSCOPE:

                        break;
                    //Bewegungssensor Liniar
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        lock.writeLock().lock();
                        aktuelleBewegung[0] = event.values[0];
                        aktuelleBewegung[1] = event.values[1];
                        aktuelleBewegung[2] = event.values[2];
                        lock.writeLock().unlock();
                        break;
                    case Sensor.TYPE_LIGHT:
                        lock.writeLock().lock();
                        aktuellerLichtwert=event.values[0];
                        lock.writeLock().unlock();
                        break;
                }

            }
        };

    }

    private void registerListener() {
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 1000);
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 1000);
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), 1000);
            //Prüfe ob Berechtigung für GPS vorliegt
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(WeckerMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            //Pruefe ob GPS aktiviert ist
            if (!locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }
            //minDistance Angabe in Meter
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 30, locationListener);

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
        alarmText =(TextView) findViewById(R.id.alarmText);

        //Wecker
        alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);
        alarmToggle = (ToggleButton) findViewById(R.id.alarmToggle);

         buttonOff = (Button) findViewById(R.id.button_alarm_off);
         buttonSnooze = (Button) findViewById(R.id.button_alarm_snooze);


    }

    /**
     * Aktivieren deaktivieren des Weckers speichern.
     * @param view
     */
    public void onToggleClicked(View view) {
        if (alarmToggle.isChecked()) {
            Log.d("MyActivity", "Alarm On");
            saveAlarmAktive(true);

        } else {
            saveAlarmAktive(false);
            Log.d("MyActivity", "Alarm Off");
        }
    }

    /**
     * Finden Änderungen an der Gui statt, soll der Wecker ausgeschaltet werden, damit im Hintergrund
     * nicht zu oft der Alarmmanager neu aufgesetzt werden muss.
     */
    public void alarmToogleAusschalten()
    {
        alarmToggle.setChecked(false);
        saveAlarmAktive(false);
    }

    private void saveAlarmAktive(Boolean angeschaltet) {
        userData.getFirstAlarm().setAngeschaltet(angeschaltet);
        Log.d("WeckerMainActivity", "saveAlarmAktive with " + alarmTimePicker.getHour() + ":" + alarmTimePicker.getMinute());
        saveAlarm();
        startService(new Intent(this, AlarmService.class));

    }

    /**
     * Speichert die Alarmeinstellung
     */
    public void saveAlarm() {
        new Dateimanager().saveUserData(mPrefs, userData);
    }




    /**
     * Method used to deactivate the alarm after the user used the "Wake up"
     * button.
     */
    private void alarmOff() {
        lock.writeLock().lock();
        doesRingAtMoment=false;
        lock.writeLock().unlock();
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
        lock.writeLock().lock();
        doesRingAtMoment=false;
        lock.writeLock().unlock();
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

    public double getAktuelleLatitude() {
        return aktuelleLatitude;
    }

    public double getAktuelleLongitude() {
        return aktuelleLongitude;
    }


    public void setAktuelleLongitude(double aktuelleLongitude) {
        lock.writeLock().lock();
        this.aktuelleLongitude = aktuelleLongitude;
        lock.writeLock().unlock();

    }

    public void setAktuelleLatitude(double aktuelleLatitude) {
        lock.writeLock().lock();
        this.aktuelleLatitude = aktuelleLatitude;
        lock.writeLock().unlock();
    }


    /**
     * AsynTask used for starting the alarm sound and vibration.
     *
     * @author Mateusz Renes
     */
    private class SoundAlarm extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {

            Log.d(SoundAlarm.class.getSimpleName(), "Alarm received!");
            if (shouldRing()) {
                anfangsLichtwert = aktuellerLichtwert;
                lock.writeLock().lock();
                doesRingAtMoment = true;
                anfangsBewegung = aktuelleBewegung.clone();
                lock.writeLock().unlock();
                ringtone.play();
            }

            Log.d(SoundAlarm.class.getSimpleName(), "Alarm received!");

            if (shouldVibrate()) {
                long[] pattern = {1000, 1000};
                vibrator.vibrate(pattern, 0);
            }

            //klingel so lange, bis Licht sich stark ändert. Dann gehe in den Snooze
            while(doesRingAtMoment)
            {
                float aktuellerLichtwertKopie=aktuellerLichtwert;
                float[] aktuelleBewegungKopie = aktuelleBewegung.clone();
             // Entweder eine deutliche Differenz im Lichtbereich, oder der Lichtwert ist von ganz Dunkel ganz hell geworden (Licht angegangen, oder Handy aus tasche geholt, oder umgekehrt
                if(lichtChanged(anfangsLichtwert,aktuellerLichtwertKopie)  || schuettelSmartphone(anfangsBewegung, aktuelleBewegungKopie))
                {
                    lock.writeLock().lock();
                    Log.d(SoundAlarm.class.getSimpleName(), "Setze doesKlingeln auf false");
                    doesRingAtMoment=false;
                    lock.writeLock().unlock();
                    alarmSnooze();
                }

            }


            return null;
        }

        private boolean schuettelSmartphone(float[] anfangsBewegung, float[] aktuelleBewegung) {
            // Log.d(SoundAlarm.class.getSimpleName(), "Anfangs- und AktuelleBewegung "+anfangsBewegung[0] +" " +anfangsBewegung[1]+" "+anfangsBewegung[2]+ "; "+aktuelleBewegung[0]+" "+ aktuelleBewegung[1]+" "+aktuelleBewegung[2]);
            if (anfangsBewegung[0] < -99 || aktuelleBewegung[0] < -99) {
                throw new RuntimeException("Bewegungssensoren wurden nicht korrekt ausgelesen");
            }

            //Handy lag ruhig auf dem Tisch
            if (Math.abs(anfangsBewegung[0]) < 0.5 && Math.abs(anfangsBewegung[1]) < 0.5 && Math.abs(anfangsBewegung[2]) < 0.5) {
                //Benutzer fängt an sich zu Bewegen
                if (Math.abs(aktuelleBewegung[0]) > 30 || Math.abs(aktuelleBewegung[1]) > 30 || Math.abs(aktuelleBewegung[2]) > 30) {
                    Log.d(SoundAlarm.class.getSimpleName(), "Klingeln durch Schütteln deaktiviert");
                    return true;
                }
            }

            return false;

        }

        private boolean lichtChanged(float startwert, float aktuellerLichtwert)
        {
            //Licht ansschalten, aus der Tasche gehohlt
            if(startwert<20 && aktuellerLichtwert>40)
            {
                Log.d(SoundAlarm.class.getSimpleName(), "Licht angeschaltet. Startwert=  " + startwert +" aktuellerWert= "+ aktuellerLichtwert);
                return true;
            }
            //Sensor mit der Hand abdecken
            if(startwert>40 && aktuellerLichtwert<10)
            {
                Log.d(SoundAlarm.class.getSimpleName(), "Sensor mit der Hand angedeckt. Startwert=  " + startwert +" aktuellerWert= "+ aktuellerLichtwert);
                return true;
            }

            return false;

        }

        private boolean shouldRing() {
            Log.d(SoundAlarm.class.getSimpleName(), "Prüfe ob nutzer Zuhause ist");
            //Prüft ob der Benutzer zuhause ist
            if(userData==null)
            {
                Log.e(SoundAlarm.class.getSimpleName(), "Shared Preferenz Userdata ist null");
            }

            //Warte kurz, dass der Locationlistener Zeit hat die Location auszulesen
            try {
                synchronized (this){
                wait(10000);}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(SoundAlarm.class.getSimpleName(), "aktuelleLatitude " + getAktuelleLatitude() + getAktuelleLongitude());
            boolean benutzerZuhause= userData.getFirstAlarm().getHome().doesLocationFit(getAktuelleLatitude(), getAktuelleLongitude());

            Log.d(SoundAlarm.class.getSimpleName(), "Benutzer ist Zuhause: " + benutzerZuhause );
            //Klingel nur, wenn Benutzer zu Hause ist
            return benutzerZuhause;
        }
        private boolean shouldVibrate() {
            return true;
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
