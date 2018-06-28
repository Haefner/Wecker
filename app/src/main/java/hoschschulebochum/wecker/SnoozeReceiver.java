package hoschschulebochum.wecker;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This is a BroadcastReceiver used to detect snooze alarms. After receiving an
 * appropriate message from the system it starts a new AlarmActivty. A snooze
 * counter is stored and sent together with the new intent.
 *
 * @author Mateusz Renes
 *
 */
public class SnoozeReceiver extends BroadcastReceiver {

    private static final String TAG = "SnoozeReceiver";

    Context context;
    int snoozeCounter;

    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;

        Bundle extras = intent.getExtras();
        if (extras != null) {
            snoozeCounter = extras.getInt("SNOOZE_COUNTER");
        }

        Intent newIntent = new Intent(context, WeckerMainActivity.class);
        newIntent.putExtra("SNOOZE_COUNTER", snoozeCounter);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);

        Log.e(TAG, "ALARM RECEIVED!!!");
    }

}

