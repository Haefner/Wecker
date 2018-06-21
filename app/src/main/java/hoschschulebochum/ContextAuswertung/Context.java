package hoschschulebochum.ContextAuswertung;


import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;

public class Context implements IContext {
    @Override
    public boolean hasConflictWithCalender() {
        return false;
    }

    public String getWeatherInformation() {// declaring object of "OWM" class
        OWM owm = new OWM("16bba3091a60d3296e7f3040d78ae11b"); //Lisas Key
        String wetter = "";

        // getting current weather data for the "London" city
        CurrentWeather cwd = null;
        try {
            cwd = owm.currentWeatherByCityName("Bochum,DE");
            //printing city name from the retrieved data
            System.out.println("City: " + cwd.getCityName());

            double kelvinAktuell = cwd.getMainData().getTemp();
            double celsiusAktuell = kelvinAktuell - 273.15;
            double kelvinMax = cwd.getMainData().getTempMax();
            double celsiusMax = kelvinMax - 273.15;
            // printing the max./min. temperature
            System.out.println("Aktuelle Temperature: " + celsiusAktuell
                    + " / Maximale Temperatur" + celsiusMax + "\'C");
            wetter = cwd.getCityName() + " aktuelle Temperatur: " + celsiusAktuell
                    + " maximale Temperatur: " + celsiusMax;
        } catch (APIException e) {
            e.printStackTrace();
        }
        return wetter;

    }


    @Override
    public boolean isBatteryStandHighEnough() {
        return false;
    }

    @Override
    public String getTimeUntilTheNextRing() {
        return "noch nicht implementiert";
    }
}


