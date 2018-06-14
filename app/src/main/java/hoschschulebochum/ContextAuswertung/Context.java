package hoschschulebochum.ContextAuswertung;

import hoschschulebochum.ContextAuswertung.IContext;

public class Context implements IContext {
    @Override
    public boolean hasConflictWithCalender() {
        return false;
    }

    @Override
    public String getWeatherInformation() {
        return "noch nicht implementiert";
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
