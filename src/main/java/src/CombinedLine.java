package src;

import com.google.gson.Gson;

// Combine station status and station details data.
public class CombinedLine {
    String legacy_id;
    boolean eightd_has_available_keys;
    int num_bikes_disabled;
    int num_bikes_available;
    int is_returning;
    String station_id;
    int num_ebikes_available;
    int is_renting;
    String station_status;
    int num_docks_disabled;
    int is_installed;
    long last_reported;
    int num_docks_available;
    String region_id;
    int capacity;
    float lon;
    float lat;
    String name;
    GeoPoint location;
    String date;

    public CombinedLine(String legacy_id, boolean eightd_has_available_keys, int num_bikes_available, int num_bikes_disabled, int is_returning,
                     String station_id, int num_ebikes_available, int is_renting, String station_status, int num_docks_disabled, int is_installed,
                     long last_reported, int num_docks_available, String region_id, int capacity, float lon, float lat, String name, String date){
        this.legacy_id = legacy_id;
        this.eightd_has_available_keys = eightd_has_available_keys;
        this.num_bikes_disabled = num_bikes_disabled;
        this.num_bikes_available = num_bikes_available;
        this.is_returning = is_returning;
        this.station_id = station_id;
        this.num_ebikes_available = num_ebikes_available;
        this.is_renting = is_renting;
        this.station_status = station_status;
        this.num_docks_disabled = num_docks_disabled;
        this.is_installed = is_installed;
        this.last_reported = last_reported;
        this.num_docks_available = num_docks_available;
        this.region_id = region_id;
        this.capacity = capacity;
        this.lon = lon;
        this.lat = lat;
        this.name = name;
        this.location = new GeoPoint(lat, lon);
        this.date = date;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        String jsonInString = gson.toJson(this);
        return jsonInString;
    }
}
