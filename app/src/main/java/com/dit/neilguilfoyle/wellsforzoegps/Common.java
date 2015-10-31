package com.dit.neilguilfoyle.wellsforzoegps;

/**
 * Created by neil on 19/03/2015.
 */
public class Common {
    public static GpsLoadActivity Context;
    public static String Lat;
    public static String Lng;

    private static String serverName = "46.22.134.194";
    private static String port = "5432";
    private static String databaseName = "WellsForZoe_fyp";
    private static String connString = String.format("jdbc:postgresql://%s:%s/%s", serverName, port, databaseName);

    public static String getConnectionString(){
        return connString;
    }
}
