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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

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


    }

    @Override
    public void onStart() {
        Log.d("runnable", "Weckermain on start");
        super.onStart();
        inst = this;
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
