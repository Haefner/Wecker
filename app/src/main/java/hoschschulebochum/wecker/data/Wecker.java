package hoschschulebochum.wecker.data;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Wecker implements Serializable {

    //Allgemeine Einstellungen zu einem Wecker
    private int hour=6;
    private int minutes=0;
    private final int SekondsToSnooze = 10;
    private boolean settingsShouldRingAtHome = true;
    private boolean settingsShouldRingOnTheWay = true;
    private boolean settingsSouldSnoozeWhenLigthChange = false;
    private boolean settingsShouldSnoozeWhenStartToMove = false;
    private boolean settingsShouldNotRingByConflictsInCalender = false;
    private MyLocation home = new MyLocation("Home" , 51.447709, 7.270900);
    private List<Wochentag> tage = new ArrayList<>();

    private boolean angeschaltet = false;

    public Wecker(int hour, int minutes, boolean angeschaltet) {
        this.setHour(hour);
        this.setMinutes(minutes);
        this.setAngeschaltet(angeschaltet);
        tage.addAll(Arrays.asList(Wochentag.values()));
    }

    public Wecker(List<Wochentag> wochentage, int hour, int minutes, boolean angeschaltet) {
        this.setHour(hour);
        this.setMinutes(minutes);
        this.setAngeschaltet(angeschaltet);
        tage.addAll(Arrays.asList(Wochentag.values()));
        tage.addAll(wochentage);
    }


    public Date getNextWakupTime() {
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int h=calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        int m=calendar.get(Calendar.MINUTE);

        //Bei Calender gehen die Tage mit SUNDAY = 1; Montag=2 los. Bei DayOfWeek mit Monday=1 Sonntag=7
        //Daher wird eine eigene Klasse verwendet
        Wochentag tag= Wochentag.getWochentagToCalenderDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
        Log.d("Wecker", "Der Wochentag heute ist " + tag);

        if(tage.isEmpty())
        {
            Log.d("Wecker", "Kein Wochentag zum klingeln definiert " + tag);
            return null;
        }
        //Finde den nächsten Tag an dem der Wecker klingeln soll.
        //___________
        //Wenn am aktuellen Tag schon die Weckzeit vorbei ist, suche den nächsten Tag, an dem der Wecker klingeln soll
        Log.d("Wecker", "Vergleiche aktuelle Uhrzeit mit der des Weckers. Die aktuelle Uhrzeit lautet " + h+":"+m
                + ". Die des Weckers ist " + getHour() + ":" + getMinutes());
        if(h>= getHour() &&m>= getMinutes())
        {
            //diesen Tag hat der Wecker schon geklingelt- Suche für den nächsten Tag
            Log.d("Wecker", "Diesen Tag hat der Wecker schon geklingelt - Prüfe den nächsten Tag. Dieser Wochentag lautet " + tag);
            tag=Wochentag.getNaechstenWochentag(tag);
        }

        //schreibe in tag, den nächsten Wochentag an den der Wecker klingeln Soll
        while(true)
        {
            if(tage.contains(tag))
            {
                break;
            }
            else {
                tag=Wochentag.getNaechstenWochentag(tag);
            }
        }

        Log.d("Wecker", "Der Wecker soll am "+tag+ "  klingeln");
        //______
        // Ermittel jetzt die nächste genaue Zeit.
        int tageBisZumKlingeln = Wochentag.getAnzahlTageBetweenAktuellerWochentagAndWeekday(Wochentag.getWochentagToCalenderDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),tag);
        if(tageBisZumKlingeln==0)
        {
            //Wenn am aktuellen Tag schon die Weckzeit vorbei ist, klingel erst in 7 Tagen wieder
            if(h>= getHour() &&m>= getMinutes())
            {tageBisZumKlingeln=7;}
        }
        Log.d("Wecker", "Das sind "+tageBisZumKlingeln+ " Tage bis der Wecker erneut klingeln");



        Calendar calendar2 = GregorianCalendar.getInstance(); // creates a new calendar instance
        //Weckzeit setzen
        calendar2.set(Calendar.HOUR_OF_DAY, getHour());
        calendar2.set(Calendar.MINUTE, getMinutes());
        calendar2.add(Calendar.DAY_OF_MONTH,tageBisZumKlingeln);

        Date time = calendar2.getTime();
        Log.d("Wecker","Das Datum hierzu lautet " + time.toString());
        return time;

    }

    public void setTime(int hour, int minutes) {
        this.setHour(hour);
        this.setMinutes(minutes);
    }

    public int getSekondsToSnooze() {
        return SekondsToSnooze;
    }

    public Boolean getSettingsShouldRingAtHome() {
        return isSettingsShouldRingAtHome();
    }

    public void setSettingsShouldRingAtHome(Boolean settingsShouldRingAtHome) {
        this.settingsShouldRingAtHome = settingsShouldRingAtHome;
    }

    public Boolean getSettingsShouldRingOnTheWay() {
        return isSettingsShouldRingOnTheWay();
    }

    public void setSettingsShouldRingOnTheWay(Boolean settingsShouldRingOnTheWay) {
        this.settingsShouldRingOnTheWay = settingsShouldRingOnTheWay;
    }

    public Boolean getSettingsSouldSnoozeWhenLigthChange() {
        return isSettingsSouldSnoozeWhenLigthChange();
    }

    public void setSettingsSouldSnoozeWhenLigthChange(Boolean settingsSouldSnoozeWhenLigthChange) {
        this.settingsSouldSnoozeWhenLigthChange = settingsSouldSnoozeWhenLigthChange;
    }

    public Boolean getSettingsShouldSnoozeWhenStartToMove() {
        return isSettingsShouldSnoozeWhenStartToMove();
    }

    public void setSettingsShouldSnoozeWhenStartToMove(Boolean settingsShouldSnoozeWhenStartToMove) {
        this.settingsShouldSnoozeWhenStartToMove = settingsShouldSnoozeWhenStartToMove;
    }

    public Boolean getSettingsShouldNotRingByConflictsInCalender() {
        return settingsShouldNotRingByConflictsInCalender;
    }

    public void setSettingsShouldNotRingByConflictsInCalender(Boolean settingsShouldNotRingByConflictsInCalender) {
        this.settingsShouldNotRingByConflictsInCalender = settingsShouldNotRingByConflictsInCalender;
    }

    public MyLocation getHome() {
        if(home==null)
        {return new MyLocation("Home", 51.446905, 7.271703);}
        return home;
    }

    public void setHome(MyLocation home) {
        this.home = home;
    }

    public boolean isAngeschaltet() {
        return angeschaltet;
    }

    public void setAngeschaltet(boolean angeschaltet) {
        this.angeschaltet = angeschaltet;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public boolean isSettingsShouldRingAtHome() {
        return settingsShouldRingAtHome;
    }

    public void setSettingsShouldRingAtHome(boolean settingsShouldRingAtHome) {
        this.settingsShouldRingAtHome = settingsShouldRingAtHome;
    }

    public boolean isSettingsShouldRingOnTheWay() {
        return settingsShouldRingOnTheWay;
    }

    public void setSettingsShouldRingOnTheWay(boolean settingsShouldRingOnTheWay) {
        this.settingsShouldRingOnTheWay = settingsShouldRingOnTheWay;
    }

    public boolean isSettingsSouldSnoozeWhenLigthChange() {
        return settingsSouldSnoozeWhenLigthChange;
    }

    public void setSettingsSouldSnoozeWhenLigthChange(boolean settingsSouldSnoozeWhenLigthChange) {
        this.settingsSouldSnoozeWhenLigthChange = settingsSouldSnoozeWhenLigthChange;
    }

    public boolean isSettingsShouldSnoozeWhenStartToMove() {
        return settingsShouldSnoozeWhenStartToMove;
    }

    public void setSettingsShouldSnoozeWhenStartToMove(boolean settingsShouldSnoozeWhenStartToMove) {
        this.settingsShouldSnoozeWhenStartToMove = settingsShouldSnoozeWhenStartToMove;
    }
}
