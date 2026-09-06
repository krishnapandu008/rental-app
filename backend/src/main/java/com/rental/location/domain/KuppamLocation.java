package com.rental.location.domain;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KuppamLocation implements LocationData {

    private static final String LOCATION_ID = "kuppam";
    private static final String DISPLAY_NAME = "Kuppam";
    private static final String STATE = "Andhra Pradesh";
    private static final String DISTRICT = "Chittoor";
    private static final String PIN_CODE = "517425";
    private static final Double DEFAULT_LAT = 12.7504;
    private static final Double DEFAULT_LNG = 78.3449;

    private final List<Area> areas;
    private final Map<String, List<String>> areaStreetMap;
    private final List<String> allStreets;

    public KuppamLocation() {
        this.areas = buildAreas();
        this.areaStreetMap = buildAreaStreetMap();
        this.allStreets = buildAllStreets();
    }

    @Override
    public String getLocationId() { return LOCATION_ID; }
    @Override
    public String getDisplayName() { return DISPLAY_NAME; }
    @Override
    public String getState() { return STATE; }
    @Override
    public String getDistrict() { return DISTRICT; }
    @Override
    public String getPinCode() { return PIN_CODE; }
    @Override
    public Double getDefaultLatitude() { return DEFAULT_LAT; }
    @Override
    public Double getDefaultLongitude() { return DEFAULT_LNG; }
    @Override
    public List<Area> getAreas() { return areas; }
    @Override
    public List<String> getAllStreets() { return allStreets; }
    @Override
    public Map<String, List<String>> getAreaStreetMap() { return areaStreetMap; }

    @Override
    public boolean supportsLocation(String query) {
        if (query == null || query.isEmpty()) return true;
        String lower = query.toLowerCase().trim();
        return lower.contains("kuppam") || lower.contains("ku") ||
               lower.contains("chittoor") || lower.contains("andhra");
    }

    private List<Area> buildAreas() {
        return List.of(
            new Area("rs_pet", "R.S. Pet (Railway Station Area)", 12.7504, 78.3449,
                List.of("Nethaji Road", "Dr. N.T.R Palace Road", "Station Road",
                        "Palace Extension Street", "Resu Street")),
            new Area("old_pet", "Old Pet", 12.7493, 78.3448,
                List.of("Ramachandra Road", "T.B. Road", "Main Bazaar Street",
                        "Gandhi Road", "Temple Street")),
            new Area("new_pet", "New Pet", 12.7510, 78.3434,
                List.of("New Pet Main Road", "High School Road", "Police Station Road")),
            new Area("dollars_colony", "Dollars Colony / Babu Nagar", 12.7480, 78.3450,
                List.of("Dollars Colony Main Road", "Babu Nagar Street", "Agastya Street")),
            new Area("sekharnagar", "Sekhar Nagar", 12.7475, 78.3438,
                List.of("Sekhar Nagar 1st Cross Street", "Sekhar Nagar 2nd Cross Street")),
            new Area("dynamic_colony", "Dynamic Colony", 12.7520, 78.3425,
                List.of("Dynamic Colony Main Road", "Water Tank Road")),
            new Area("kamathamur", "Kamathamur & Lakshmipuram", 12.7450, 78.3410,
                List.of("Kamathamur Road", "Lakshmipuram Street")),
            new Area("town_center", "General Kuppam Town Center", 12.7500, 78.3440,
                List.of("APSRTC Bus Depot Road", "NH-42 Bypass Corridor"))
        );
    }

    private Map<String, List<String>> buildAreaStreetMap() {
        Map<String, List<String>> map = new HashMap<>();
        for (Area area : areas) {
            map.put(area.getId(), area.getStreets());
        }
        return Collections.unmodifiableMap(map);
    }

    private List<String> buildAllStreets() {
        List<String> streets = new ArrayList<>();
        for (Area area : areas) {
            streets.addAll(area.getStreets());
        }
        return Collections.unmodifiableList(streets);
    }
}