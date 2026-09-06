package com.rental.location.domain;

import java.util.List;
import java.util.Map;

public interface LocationData {
    String getLocationId();
    String getDisplayName();
    String getState();
    String getDistrict();
    String getPinCode();
    Double getDefaultLatitude();
    Double getDefaultLongitude();
    List<Area> getAreas();
    List<String> getAllStreets();
    Map<String, List<String>> getAreaStreetMap();
    boolean supportsLocation(String query);
}