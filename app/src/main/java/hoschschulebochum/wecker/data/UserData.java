package hoschschulebochum.wecker.data;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data store class for user information. Apart from collecting user data, it
 * enables some parts of alarms management.
 *
 * @author Mateusz Renes
 */
public class UserData implements Serializable {


    //private int userId;
    // private Notification lastNotification;
    private List<Wecker> alarmList = new ArrayList<>();


    public void addAlarm(Wecker wecker) {
        alarmList.add(wecker);
    }

    public void removeAlarm(Wecker wecker) {
        alarmList.remove(wecker);
    }

    public void removeAll() {
        alarmList.clear();
    }


    /**
     * Returns a String representation of the next active alarm. If there is no
     * active alarm detected, the returned String will show an appropriate
     * message.
     *
     * @return
     */
    public Date getNextAlarmTime() {
        Date nextWakeupTime = null;

        for (Wecker w : alarmList) {
            Date wakupTime = w.getNextWakupTime();
            Log.d("UserData", "Es gibt einen Wecker, der auf " + wakupTime + "eingestellt ist.");
            if (nextWakeupTime == null) {
                nextWakeupTime = wakupTime;
                break;
            }
            if (nextWakeupTime.after(wakupTime)) {
                nextWakeupTime = wakupTime;
            }
        }
        return nextWakeupTime;
    }


}
