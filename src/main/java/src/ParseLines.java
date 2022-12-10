package src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

public class ParseLines {

    public static void main(String[] args) {


        // Driver for producer code
        String topicName = "StationStatus";
        try {
            // Load Kafka Config
            Properties config = KafkaClient.loadConfig("./src/main/java/src/kafka.config");
            try {

                long maxTimestamp = 0;
                while (true){
                    String fullFile = "";
                    // Pull data from gbfs feed
                    URL url = new URL("https://gbfs.citibikenyc.com/gbfs/en/station_status.json");
                    BufferedReader read = new BufferedReader(
                            new InputStreamReader(url.openStream()));
                    String i;
                    while ((i = read.readLine()) != null)
                        fullFile += i;
                    read.close();

                    // For testing, use local file
//            File myObj = new File("./src/main/java/src/shortLogs.txt");
//            Scanner myReader = new Scanner(myObj);
//
//            while (myReader.hasNextLine()) {
//                String data = myReader.nextLine();
//                fullFile += data;
//            }
//            myReader.close();

                    // Convert GBFS data into array
                    EventLine[] events = EventLineParser.ParseLines(fullFile);
                    KafkaClient kClient = new KafkaClient(config);
                    maxTimestamp = kClient.produceEvents(topicName, events, maxTimestamp);
                    // sleep 20 seconds
                    Thread.sleep(20000);
                }

            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}
