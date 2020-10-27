package com.barkindustries.mascotapp.ui.lostPets;

import android.location.Location;

public class LocationCommunicator {
    public Location location;
    private static LocationCommunicator locationCommunicator;


    public static LocationCommunicator getLocationCommunicator() {
        if(locationCommunicator == null) {
            locationCommunicator = new LocationCommunicator();
        }
        return locationCommunicator;
    }

    private LocationCommunicator() {

    }
}
