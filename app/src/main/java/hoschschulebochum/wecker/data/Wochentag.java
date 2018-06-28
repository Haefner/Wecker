package hoschschulebochum.wecker.data;

public enum Wochentag {

    Sonntag, Montag, Dienstag, Mittwoch, Donnerstag, Freitag, Samstag;


    public static Wochentag getWochentagToCalenderDayOfWeek(int calenderWeekDay) {
        switch (calenderWeekDay) {
            case 1:
                return Sonntag;
            case 7:
                return Samstag;
            case 6:
                return Freitag;
            case 5:
                return Donnerstag;
            case 4:
                return Mittwoch;
            case 3:
                return Dienstag;
            default:
                return Montag;

        }
    }

    public static Wochentag getNaechstenWochentag(Wochentag wochentag) {
        switch (wochentag) {
            case Samstag:
                return Sonntag;
            case Freitag:
                return Samstag;
            case Donnerstag:
                return Freitag;
            case Mittwoch:
                return Donnerstag;
            case Dienstag:
                return Mittwoch;
            case Montag:
                return Dienstag;
            default:
                return Montag;
        }
    }

    public static int getAnzahlTageBetweenAktuellerWochentagAndWeekday(Wochentag aktuellerWochentag, Wochentag bisWochentag) {
        //Montag ist 1 und Sonntag 7
        //Aktuelle Wochentag (z:b. Dienstag 2) ist kleiner als der uebergebeneWochentag (z.B. Sonntag 7)
        if (aktuellerWochentag.ordinal() < bisWochentag.ordinal()) {
            return bisWochentag.ordinal() - aktuellerWochentag.ordinal();
        }
        //Aktuelle Wochentag (z:b. Sonntag 7) ist größer als der uebergebeneWochentag (z.B. Dienstag 2)
        else if (aktuellerWochentag.ordinal() > bisWochentag.ordinal()) {

            int differenzZurVollenWoche= 7-aktuellerWochentag.ordinal();
            return differenzZurVollenWoche +bisWochentag.ordinal();
        }
        return 0;
    }


}
