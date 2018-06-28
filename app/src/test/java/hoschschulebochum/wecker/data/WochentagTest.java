package hoschschulebochum.wecker.data;

import junit.framework.Assert;

import org.junit.Test;

public class WochentagTest {

    @Test
    public void getAnzahlTageBetweenTodayAndWeekday()
    {
        Assert.assertEquals(4,Wochentag.getAnzahlTageBetweenAktuellerWochentagAndWeekday(Wochentag.Montag, Wochentag.Freitag));
        Assert.assertEquals(3,Wochentag.getAnzahlTageBetweenAktuellerWochentagAndWeekday(Wochentag.Dienstag, Wochentag.Freitag));
        Assert.assertEquals(5,Wochentag.getAnzahlTageBetweenAktuellerWochentagAndWeekday(Wochentag.Sonntag, Wochentag.Freitag));
        Assert.assertEquals(3,Wochentag.getAnzahlTageBetweenAktuellerWochentagAndWeekday(Wochentag.Samstag, Wochentag.Dienstag));
    }
}