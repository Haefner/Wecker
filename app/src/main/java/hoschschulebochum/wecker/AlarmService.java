package hoschschulebochum.wecker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import hoschschulebochum.wecker.data.Dateimanager;
import hoschschulebochum.wecker.data.UserData;

/**
 * This service is used for monitoring alarm activity and setting new alarms for
 * the AlarmManager. The service is run in foreground to ensure that it will be
 * shut down by the OS only is case of extreme lack of resources.
 *
 * @author Mateusz Renes
 *
 */
public class AlarmService extends Service {

    private static final String TAG = "AlarmService";

    final static int RequestCode = 1;
    int snoozeCounter;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This method is called after the start() command of the service. It sets a
     * new alarm based on UserData file and starts the service.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        // get snooze counter
        Bundle extras = intent.getExtras();
        if (extras != null) {
            snoozeCounter = extras.getInt("SNOOZE_COUNTER");
        }
        // get alarm info

        SharedPreferences mPrefs= getSharedPreferences("Wecker",Context.MODE_PRIVATE);
        UserData userData = new Dateimanager().loadUserData(mPrefs);
        Log.d("runnable", "Benutzerdaten geladen");
        if(userData.getNextAlarmTime()!=null) {
            String nextAlarmString = "Nächster Wecker klingelt am " + userData.getNextAlarmTime().toString();
            // set alarm
            setAlarm(userData);

            // build notification
            NotificationCompat.Builder alamNotificationBuilder = new NotificationCompat.Builder(
                    this);
            alamNotificationBuilder.setContentTitle("Alarm");
            alamNotificationBuilder.setContentText(nextAlarmString);
            alamNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            Notification note = alamNotificationBuilder.getNotification();

            // start service
            startForeground(1, note);
        }
        else{
            cancelAlarm();
        }
        return START_NOT_STICKY;
    }

    private void cancelAlarm() {
        // set snooze counter
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("SNOOZE_COUNTER", snoozeCounter);
        // create intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), RequestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // get alarm manager
        AlarmManager alarmManager = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        // cancel any previous alarms if set
        alarmManager.cancel(pendingIntent);
    }


    private void setAlarm(UserData alarmInfo) {
        // set snooze counter
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("SNOOZE_COUNTER", snoozeCounter);
        // create intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), RequestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // get alarm manager
        AlarmManager alarmManager = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        // cancel any previous alarms if set
        alarmManager.cancel(pendingIntent);
        // if the alarm is active, choose time and date of the alarm and
        // set it in the alarm manager
        if (alarmInfo.getNextAlarmTime()!=null) {
            Log.d("AlarmService", "Nächste Alarmzeit " + alarmInfo.getNextAlarmTime().getTime());
            long waketime=alarmInfo.getNextAlarmTime().getTime();
            alarmManager.set(AlarmManager.RTC_WAKEUP, waketime, pendingIntent);

        }
    }

}
