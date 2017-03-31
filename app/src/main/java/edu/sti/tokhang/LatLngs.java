package edu.sti.tokhang;

/**
 * Created by ChiShingWan on 31/03/2017.
 */

public class LatLngs {
    public final double	latitude;
    public final double	longitude;

    LatLngs(double latitude, double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }

    double getLongitude(){
        return longitude;
    }
    double getLatitude(){
        return latitude;
    }
}

