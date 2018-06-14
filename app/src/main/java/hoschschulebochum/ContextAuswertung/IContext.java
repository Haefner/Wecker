package hoschschulebochum.ContextAuswertung;

interface IContext {

    /**
     * Gibt an, ob es zum Zeitpunkt an dem der Wecker klingeln soll einen Termin gibt.
     * Termine können je nach Einstellung das Weckerklingeln verhindern.
     *
     * @return true = wenn der Wecker nicht klingel soll, weil Zeitlich ein Termin eingetragen ist.
     */
    public boolean hasConflictWithCalender();


    /**
     * Gibt die aktuelle Temperatur als Gradzahl wieder.
     *
     * @return Die aktuelle Temperatur
     */
    public String getWeatherInformation();

    /**
     * Gibt wieder, ob der Akku des Smartphone noch genug Power hat um am nächsten Tag zu klingeln.
     *
     * @return true= Akku ist voll genug
     */
    public boolean isBatteryStandHighEnough();

    /**
     * Gibt die Zeit bis zum nächsten Weckerklingeln wieder
     *
     * @return String der die Stunden und Minuten bis zum nächsten klingeln enthält
     */
    public String getTimeUntilTheNextRing();
}
