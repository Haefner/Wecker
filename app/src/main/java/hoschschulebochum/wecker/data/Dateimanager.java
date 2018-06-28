package hoschschulebochum.wecker.data;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

public class Dateimanager {

        public void saveUserData(SharedPreferences mPrefs, UserData userData) {
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(userData);
            prefsEditor.putString("UserData", json);
            prefsEditor.commit();
            Log.d("runnable", "Benutzerdaten gespeichert");
        }

        public UserData loadUserData(SharedPreferences mPrefs) {
            Gson gson = new Gson();
            String json = mPrefs.getString("UserData", "");
            UserData obj = gson.fromJson(json, UserData.class);
            Log.d("runnable", "Benutzerdaten geladen");
            return obj;
        }
}
