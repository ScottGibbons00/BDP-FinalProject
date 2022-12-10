package src;

import com.google.gson.Gson;

import java.util.ArrayList;

public class EventLineParser {
    // OLD
    // Json deserializer before starting to use Gson to do that
    public static ArrayList<EventLine> OldParseLines(String rawData){
        // Just get array
        String formattedData = rawData.substring(rawData.indexOf('['), rawData.lastIndexOf(']'));
        ArrayList<EventLine> eventlines = new ArrayList<EventLine>();
        while(formattedData.indexOf('{') != -1){
            try{
                String lineToConsider = formattedData.substring(formattedData.indexOf('{'), formattedData.indexOf('}'));

                // numDisabled
                String numDisabledString = lineToConsider.substring(lineToConsider.indexOf(":") + 1, lineToConsider.indexOf(','));
                int numDisabled = Integer.parseInt(numDisabledString);

                // legacyId
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String legacyId = lineToConsider.substring(lineToConsider.indexOf(":") + 2, lineToConsider.indexOf("\","));

                // numAvailable
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String numAvailableString = lineToConsider.substring(lineToConsider.indexOf(":") + 1, lineToConsider.indexOf(','));
                int numAvailable = Integer.parseInt(numAvailableString);

                // lastReported
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String lastReportedString = lineToConsider.substring(lineToConsider.indexOf(":") + 1, lineToConsider.indexOf(','));
                long lastReported = Long.parseLong(lastReportedString);

                // numEbikesAvailable
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String numEbikesString = lineToConsider.substring(lineToConsider.indexOf(":") + 1, lineToConsider.indexOf(','));
                int numEbikes = Integer.parseInt(numEbikesString);

                // isRenting
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String isRentingString = lineToConsider.substring(lineToConsider.indexOf(":") + 1, lineToConsider.indexOf(','));
                int isRenting = Integer.parseInt(isRentingString);

                // isReturning
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String isReturningString = lineToConsider.substring(lineToConsider.indexOf(":") + 1, lineToConsider.indexOf(','));
                int isReturning = Integer.parseInt(isReturningString);

                // stationId
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String stationId = lineToConsider.substring(lineToConsider.indexOf(":") + 2, lineToConsider.indexOf("\","));

                // numDocksAvail
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String numDocksAvailString = lineToConsider.substring(lineToConsider.indexOf(":") + 1, lineToConsider.indexOf(','));
                int numDocksAvail = Integer.parseInt(numDocksAvailString);

                // eightd_has_available_keys
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String availKeysString = lineToConsider.substring(lineToConsider.indexOf(":") + 1, lineToConsider.indexOf(','));
                boolean availKeys = false;
                if (availKeysString.equalsIgnoreCase("true")) availKeys = true;

                // station_status
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String stationStatus = lineToConsider.substring(lineToConsider.indexOf(":") + 2, lineToConsider.indexOf("\","));

                // is_installed
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String isInstalledString = lineToConsider.substring(lineToConsider.indexOf(":") + 1, lineToConsider.indexOf(','));
                int isInstalled = Integer.parseInt(isInstalledString);

                // num_docks_disabled
                lineToConsider = lineToConsider.substring(lineToConsider.indexOf(',') + 1);
                String numDocksDisabledString = lineToConsider.substring(lineToConsider.indexOf(":") + 1);
                int numDocksDisabled = Integer.parseInt(numDocksDisabledString);

                EventLine lineToAdd = new EventLine(legacyId, availKeys, numAvailable, numDisabled, isReturning, stationId, numEbikes, isRenting, stationStatus, numDocksDisabled, isInstalled, lastReported, numDocksAvail);
                eventlines.add(lineToAdd);

                System.out.println(lineToAdd);
            }
            catch (Exception e){
                System.out.println(e);
            }
            formattedData = formattedData.substring(formattedData.indexOf('}') + 1);
        }

        return eventlines;
    }

    // Convert GBFS feed into Java object
    public static EventLine[] ParseLines(String rawData){
        Gson gson = new Gson();
        CitiBike fullData = gson.fromJson(rawData, CitiBike.class);
        return fullData.data.stations;
    }

}
