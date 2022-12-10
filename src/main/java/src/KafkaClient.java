package src;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class KafkaClient {
//    private Properties props = new Properties();
    private Producer<String, String> producer;

    // Load kafka config
    public static Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }
    public KafkaClient(Properties config) {
        producer = new KafkaProducer<String, String>(config);
    }

    // Takes list of events to be produced, keeping track of the highest time stamp to calculate changes for next poll
    public long produceEvents(String topicName, EventLine[] lines, long maxTimeStamp){
        System.out.println("Starting to produce events");
        int numberOfEvents = 0;
        long tempMaxTimeStamp = maxTimeStamp;
        for (EventLine line: lines){
            // Only publish event if it has changed since we last checked
            if (line.last_reported > maxTimeStamp){
                // Update our running max
                if (line.last_reported > tempMaxTimeStamp){
                    tempMaxTimeStamp = line.last_reported;
                }
                produceEvent(topicName, line);
                numberOfEvents++;
            }
        }
        producer.close();
        System.out.println("PRODUCED " + numberOfEvents + " EVENTS");
        return tempMaxTimeStamp;
    }

    // Helper method to produce just one event
    public void produceEvent(String topicName, EventLine line) {
        try{
            System.out.println("Producing: " + line);
            // Set station id as key for better partitioning
            producer.send(new ProducerRecord<>(topicName, line.station_id, line.toString()));
        }
        catch (Exception e){
            System.err.println(e);
        }
    }
}
