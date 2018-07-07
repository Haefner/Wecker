package hoschschulebochum.ContextAuswertung;


import android.os.AsyncTask;

import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;

public  class WetterContext extends AsyncTask<Object,Void,String> {
    public boolean hasConflictWithCalender() {
        return false;
    }

    public String doInBackground(Object... objcts) {// declaring object of "OWM" class

        OWM owm = new OWM("16bba3091a60d3296e7f3040d78ae11b"); //Lisas Key
        String wetter = "";

        // getting current weather data for the "London" city
        CurrentWeather cwd = null;
        try {
            cwd = owm.currentWeatherByCityName("Bochum,DE");
            //printing city name from the retrieved data
            System.out.println("City: " + cwd.getCityName());

            double kelvinAktuell = cwd.getMainData().getTemp();
            int celsiusAktuell = (int) (kelvinAktuell - 273.15);
            double kelvinMax = cwd.getMainData().getTempMax();
            int celsiusMax = (int) (kelvinMax - 273.15);
            // printing the max./min. temperature
            System.out.println("Aktuelle: " + celsiusAktuell
                    + " / Maximale Temperatur" + celsiusMax + "\'C");
            wetter = "aktuelle " + celsiusAktuell
                    + " °C maximale " + celsiusMax+" °C";
        } catch (APIException e) {
            e.printStackTrace();
        }
        return wetter;

    }

}


