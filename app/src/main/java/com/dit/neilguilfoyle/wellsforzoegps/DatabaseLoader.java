package com.dit.neilguilfoyle.wellsforzoegps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Neil Guilfoyle on 05/03/2015.
 */
public class DatabaseLoader {

    String url;
    String username;
    String password;

    public DatabaseLoader(String connectionString, String username, String password) throws ClassNotFoundException, SQLException
    {
        this.url = connectionString;
        this.username = username;
        this.password = password;
        Class.forName("org.postgresql.Driver");
    }

    public boolean TryLogin(String username, String passwordHash) throws SQLException {

        Connection conn= DriverManager.getConnection(url,
                this.username,
                this.password);
        String sql="SELECT \"Password\" FROM \"Users\" WHERE \"Username\" = ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        //Bind variables are enumerated starting with 1;
        ps.setString(1,username);
        ResultSet rs=ps.executeQuery();
        rs.next();
        String hash=rs.getString(1);
        if (ps != null) {
            ps.close();
        }
        if (rs != null) {
            rs.close();
        }
        if (conn != null) {
            conn.close();
        }

        return passwordHash.equals(hash);
    }

    public void AddVillage(String village) throws SQLException {
        String insert = "INSERT INTO \"Location\"( \"Village\") VALUES (?);";

        Connection conn = DriverManager.getConnection(url, username, password);

        PreparedStatement ps = conn.prepareStatement(insert);
        ps.setString(1,village);
        ps.executeUpdate();

        if (ps != null) {
            ps.close();
        }
        if (conn != null) {
            conn.close();
        }
    }

    public void InsertWellData(String wellLong, String wellLat, String desc, String village, Double level, Double depth, String replacedBy, Date replaceDateStr) throws SQLException {
        String insert = "INSERT INTO \"Wells\"( \"WellLng\", \"WellLat\", \"Description\" ,\"Village\","+
                "\"WaterLevel\",\"WellDepth\",\"ValveReplacedBy\", \"ValveReplacementDate\") "+
                "VALUES (?, ?, ? ,?, ?, ?, ?";
        boolean hasDate = replaceDateStr != null && !replaceDateStr.equals("");
        if(hasDate) {
            insert += ",?";
        }
        insert +=");";

        Connection conn = DriverManager.getConnection(url, username, password);

        PreparedStatement ps = conn.prepareStatement(insert);
        ps.setString(1,wellLong);
        ps.setString(2,wellLat);
        ps.setString(3,desc);
        ps.setString(4,village);
        ps.setDouble(5,level);
        ps.setDouble(6,depth);
        ps.setString(7,replacedBy);
        if(hasDate) {
            ps.setDate(8,replaceDateStr);
        }

        ps.executeUpdate();

        if (ps != null) {
            ps.close();
        }
        if (conn != null) {
            conn.close();
        }
    }

    public ArrayList<LatLng> ReadAllWells() throws SQLException {
        Connection conn= DriverManager.getConnection(url,
                this.username,
                this.password);
        String sql="SELECT \"WellLng\",\"WellLat\" FROM \"Wells\"";
        ArrayList<LatLng> coords = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs=ps.executeQuery();
        while(rs.next()){
            String lng = rs.getString(1);
            String lat = rs.getString(2);
            double longitude = StringToCoordinateConverter.CoordToDD(lng);
            double latitude = StringToCoordinateConverter.CoordToDD(lat);
            coords.add(new LatLng(latitude, longitude));
            Log.i("Well", coords.get(coords.size()-1).toString());
        }
        if (ps != null) {
            ps.close();
        }
        if (rs != null) {
            rs.close();
        }
        if (conn != null) {
            conn.close();
        }

        return coords;
    }
}
