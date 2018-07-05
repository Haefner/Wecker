package hoschschulebochum.wecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This is a BroadcastReceiver used to detect new alarms. After receiving an
 * appropriate message from the system it starts a new AlarmActivty. A snooze
 * counter is stored and sent together with the new intent.
 *
 * @author Mateusz Renes
 *
 */
public class AlarmReceiver extends BroadcastReceiver {


    Context context;
    int snoozeCounter;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("runnable", "Alarm received!");
        this.context = context;
        //set bundle with snooze counter
        Bundle extras = intent.getExtras();
        if (extras != null) {
            snoozeCounter = extras.getInt("SNOOZE_COUNTER");
        }
        // and start new activity
        Intent newIntent = new Intent(context, WeckerMainActivity.class);
        newIntent.putExtra("START_ALARM", true);
        newIntent.putExtra("SNOOZE_COUNTER", snoozeCounter);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        //Löse aus, dass der Alarm ertönt
        context.startActivity(newIntent);

    }

}
