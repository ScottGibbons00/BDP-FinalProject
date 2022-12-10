package src;

import com.google.gson.Gson;

// Main long-term station information, such as capacity, longitude and latitude, etc.
public class StationLine {
    String region_id;
    int capacity;
    String station_id;
    float lon;
    float lat;
    String name;

    @Override
    public String toString() {
        Gson gson = new Gson();
        String jsonInString = gson.toJson(this);
        return jsonInString;
    }
}
