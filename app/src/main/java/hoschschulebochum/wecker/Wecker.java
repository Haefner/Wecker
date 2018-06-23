package hoschschulebochum.wecker;

import java.sql.Time;

public class Wecker {

    //Allgemeine Einstellungen zu einem Wecker
    private Time time;
    private final int SekondsToSnooze = 10;
    private Boolean settingsShouldRingAtHome;
    private Boolean settingsShouldRingOnTheWay;
    private Boolean settingsSouldSnoozeWhenLigthChange;
    private Boolean settingsShouldSnoozeWhenStartToMove;
    private Boolean settingsShouldNotRingByConflictsInCalender;
    private MyLocation home;


    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public int getSekondsToSnooze() {
        return SekondsToSnooze;
    }

    public Boolean getSettingsShouldRingAtHome() {
        return settingsShouldRingAtHome;
    }

    public void setSettingsShouldRingAtHome(Boolean settingsShouldRingAtHome) {
        this.settingsShouldRingAtHome = settingsShouldRingAtHome;
    }

    public Boolean getSettingsShouldRingOnTheWay() {
        return settingsShouldRingOnTheWay;
    }

    public void setSettingsShouldRingOnTheWay(Boolean settingsShouldRingOnTheWay) {
        this.settingsShouldRingOnTheWay = settingsShouldRingOnTheWay;
    }

    public Boolean getSettingsSouldSnoozeWhenLigthChange() {
        return settingsSouldSnoozeWhenLigthChange;
    }

    public void setSettingsSouldSnoozeWhenLigthChange(Boolean settingsSouldSnoozeWhenLigthChange) {
        this.settingsSouldSnoozeWhenLigthChange = settingsSouldSnoozeWhenLigthChange;
    }

    public Boolean getSettingsShouldSnoozeWhenStartToMove() {
        return settingsShouldSnoozeWhenStartToMove;
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
        return home;
    }

    public void setHome(MyLocation home) {
        this.home = home;
    }
}
