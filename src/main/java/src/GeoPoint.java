package src;

public class GeoPoint {
    float lat;
    float lon;

    // Helper object for elasticsearch/kibana
    public GeoPoint(float lat, float lon){
        this.lat = lat;
        this.lon = lon;
    }
}
