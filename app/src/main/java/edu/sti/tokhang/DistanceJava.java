package edu.sti.tokhang;

/**
 * Created by aser on 3/31/2017.
 */

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class DistanceJava {


    ArrayList<LatLng> markerLoc;
    LatLng currentLoc;
    ArrayList<Double> markerDist;

    public DistanceJava(ArrayList<LatLng> markerLoc,LatLng currLoc){
        this.markerLoc=markerLoc;
        this.currentLoc=currLoc;
        setDistance();
    }

    public void setDistance(){
        markerLoc= new ArrayList<>();
        markerDist=new ArrayList<>();
        for(int i=0;i<markerLoc.size();i++){
            Location locA = new Location("point A");
            locA.setLatitude(markerLoc.get(i).latitude);
            locA.setLatitude(markerLoc.get(i).longitude);
            Location locB = new Location("point B");
            locB.setLatitude(currentLoc.latitude);
            locB.setLatitude(currentLoc.longitude);
            markerDist.add((double)locA.distanceTo(locB));
        }
    }
    public ArrayList<Double> getDistance(){
        return markerDist;
    }
}
