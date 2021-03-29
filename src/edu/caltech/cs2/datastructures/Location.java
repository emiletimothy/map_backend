package edu.caltech.cs2.datastructures;

import com.google.gson.JsonObject;

import java.util.List;

public class Location {

    public enum Type {
        BUILDING,
        WAYPOINT;
    }

    public static final double EARTH_RAD_FT = 20925721.784777;
    public final long id;
    public final double lat;
    public final double lon;
    public final String amenity;
    public String name;
    public final String address;
    public final String shop;
    public final Type type;


    public Location(long id, double latitude, double longitude,
                    String name, String addr, String amenity, String shop, String type) {
        this.id = id;
        this.lat = latitude;
        this.lon = longitude;
        this.type = type.equals("building") ? Type.BUILDING : Type.WAYPOINT;
        this.amenity = amenity;
        this.shop = shop;
        this.name = name;
        this.address = addr;
    }

    public Location(long id) {
        this(id, 0, 0, null, null, null, null, "waypoint");
    }

    public Location(JsonObject e) {
        this.id = Long.parseLong(e.get("id").getAsString());
        this.lat = e.get("lat").getAsDouble();
        this.lon = e.get("lon").getAsDouble();
        String[] props = new String[5];

        int i = 0;
        for (String prop : List.of("name", "amenity", "shop", "type", "address")) {
            if (e.has(prop)) {
                props[i] = e.get(prop).getAsString();
            }
            i++;
        }

        this.name = props[0];
        this.amenity = props[1];
        this.shop = props[2];
        this.type = props[3].equals("building") ? Type.BUILDING : Type.WAYPOINT;
        this.address = props[4];
    }

    public String displayString() {
        String s = this.name;
        if (this.address != null) {
            s += " (" + this.address + ")";
        } else {
            s += " (" + this.lat + ", " + this.lon + ")";
        }

        return s;
    }

    public double getDistance(double lat, double lon) {
        double dLat = Math.toRadians(lat - this.lat);
        double dLon = Math.toRadians(lon - this.lon);
        return 2 * EARTH_RAD_FT * Math.asin(Math.sqrt(
                Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(Math.toRadians(this.lat)) *
                        Math.cos(Math.toRadians(lat))));
    }

    public double getDistance(Location other) {
        return this.getDistance(other.lat, other.lon);
    }

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        return new Location(0, lat1, lon1, null, null, null, null, "building").getDistance(lat2, lon2);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("{");
        s.append("\"id\": " + "\"" + this.id + "\"");
        s.append(", ");
        s.append("\"lat\": " + this.lat);
        s.append(", ");
        s.append("\"lon\": " + this.lon);
        s.append(", ");
        s.append("\"type\": " + "\"" + (this.type == Type.BUILDING ? "building" : "footpath") + "\"");
        if (this.amenity != null) {
            s.append(", \"amenity\": " + "\"" + this.amenity + "\"");
        }
        if (this.address != null) {
            s.append(", \"address\": " + "\"" + this.address + "\"");
        }
        if (this.shop != null) {
            s.append(", \"shop\": " + "\"" + this.shop + "\"");
        }
        if (this.name != null) {
            s.append(", \"name\": " + "\"" + this.name.replace("\"", "\\\"") + "\"");
        }
        s.append("}");
        return s.toString();
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Location)) {
            return false;
        }
        return this.id == ((Location)o).id;
    }
}
