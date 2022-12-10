package src;

import com.google.gson.Gson;

// Object form of station status feed
public class EventLine {
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

    public EventLine(String legacy_id, boolean eightd_has_available_keys, int num_bikes_available, int num_bikes_disabled, int is_returning,
                     String station_id, int num_ebikes_available, int is_renting, String station_status, int num_docks_disabled, int is_installed,
                     long last_reported, int num_docks_available){
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
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        String jsonInString = gson.toJson(this);
        return jsonInString;
    }



}
