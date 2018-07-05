package hoschschulebochum.wecker.data;

import android.location.Location;


public class MyLocation {
    //Beispielsweise HOME
    String bezeichner;
    double latitude;
    double longitude;

    Location location;

    static final long radiusInMeter=500;

    public MyLocation(String bezeichner, double latitude, double lonitude)
    {
        this.bezeichner=bezeichner;
        this.latitude=latitude;
        this.longitude=lonitude;
        //TODO restliche Daten füllen
    }


    /**
     *  Vergleicht, ob sich die persistierte Lokation im Umkreis von 500 Meter zur aktuellen Position befindet.
     * @param latitude GPS koordinaten der aktuellen Position
     * @param longitude latitude GPS koordinaten der aktuellen Position
     * @return true, wenn Orte nahe an einander sind, sonst false
     */
    public boolean doesLocationFit(double latitude, double longitude)
    {
         double entfernung = distance(this.latitude, this.longitude, latitude,longitude, 'K')*1000;
         System.out.println(entfernung);
         return entfernung<radiusInMeter;
    }


    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        //Kilimeter
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }


    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public String getBezeichner() {
        return bezeichner;
    }

    public void setBezeichner(String bezeichner) {
        this.bezeichner = bezeichner;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public static long getRadiusInMeter() {
        return radiusInMeter;
    }




}
