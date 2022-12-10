package src;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.google.gson.Gson;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.kafka.clients.consumer.*;
import org.elasticsearch.client.RestClient;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;


public class CustomKafkaConsumer {


    public static void main(String[] args) {
        try{
            // Credentials for elasticsearch
            BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
            credsProv.setCredentials(
                    AuthScope.ANY, new UsernamePasswordCredentials("MmstczlZUUJIQjdMRmpCRkh2Nl86alowb3B6aUVRbnFjYU93TlI4bjNTUQ==", "qKas9YQBROFdIWUOBRfF")
            );
            Header[] defaultHeaders =
                    new Header[]{new BasicHeader("Authorization",
                            "ApiKey TjZaUi1ZUUJST0ZkSVdVT1hCZ3E6dGM0SVp5b09TX3VNLWFKN25FY2laUQ==")};
            RestClient restClient = RestClient.builder(
                    new HttpHost("finalproject.es.us-east-1.aws.found.io", 9243, "https"))
                    .setDefaultHeaders(defaultHeaders)
                    .build();

//            Request request = new Request(
//                    "GET",
//                    "/_cluster/health");
//            Response response = restClient.performRequest(request);

            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());

// And create the API client
            ElasticsearchClient client = new ElasticsearchClient(transport);

            // Credentials for cassandra
            String serverIP = "127.0.0.1";
            String keyspace = "fp";

            Cluster cluster = Cluster.builder()
                    .addContactPoints(serverIP)
                    .build();

            Session session = cluster.connect(keyspace);

            // credentials for kafka
            Properties config = loadConfig("./src/main/java/src/consumer.config");
            config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            config.put(ConsumerConfig.GROUP_ID_CONFIG, "cassandra-consumer");
            config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            // credentials for redis
//            Jedis jedis = new Jedis("bdp-final-cache.d5efma.ng.0001.use1.cache.amazonaws.com", 6379); // deployed to ec2
            Jedis jedis = new Jedis("127.0.0.1", 6379); // local testing

            // subscribe to StationStatus topic
            Consumer<String, String> consumer = new KafkaConsumer<String, String>(config);
            consumer.subscribe(Arrays.asList("StationStatus"));

            try {
                while (true) {
                    // get new records
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        // key: StationID
                        // value: json data
                        String key = record.key();
                        String value = record.value();

                        Gson gson = new Gson();
                        try{
                            // Parse line into object
                            EventLine parsedLine = gson.fromJson(value, EventLine.class);

                            // try to retrieve extra station data from redis
                            String redisValue = jedis.get(key);

                            // Check if we need to reset station data
                            if (redisValue == null){
                                ResetStationData(jedis);
                            }
                            redisValue = jedis.get(key);
                            StationLine station = gson.fromJson(redisValue, StationLine.class);

                            // string pattern used for cassandra/elasticsearch
                            String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";

                            DateFormat df = new SimpleDateFormat(pattern);

                            Instant instant = Instant.ofEpochSecond(parsedLine.last_reported);
                            Date date = Date.from(instant);
                            String dateString = df.format(date);

                            // combine the two data sources
                            CombinedLine combinedLine = new CombinedLine(
                                    parsedLine.legacy_id,
                                    parsedLine.eightd_has_available_keys,
                                    parsedLine.num_bikes_available,
                                    parsedLine.num_bikes_disabled,
                                    parsedLine.is_returning,
                                    parsedLine.station_id,
                                    parsedLine.num_ebikes_available,
                                    parsedLine.is_renting,
                                    parsedLine.station_status,
                                    parsedLine.num_docks_disabled,
                                    parsedLine.is_installed,
                                    parsedLine.last_reported,
                                    parsedLine.num_docks_available,
                                    station.region_id,
                                    station.capacity,
                                    station.lon,
                                    station.lat,
                                    station.name,
                                    dateString
                            );

                            // insert data into elasticsearch
                            IndexRequest<CombinedLine> req;
                            String combinedString = combinedLine.toString();
                            System.out.println(combinedString);
                            StringReader sReader = new StringReader(combinedLine.toString());
                            req = IndexRequest.of(b -> b
                                    .index("final-project")
                                    .withJson(sReader)
                            );

                            client.index(req);

                            // insert into cassandra:
                            String cqlStatement = String.format("insert into fp.citi_bike (" +
                                    "legacy_id, " +
                                    "timestamp, " +
                                    "num_bikes_available, " +
                                    "num_bikes_disabled, " +
                                    "station_id, " +
                                    "num_ebikes_available, " +
                                    "station_status, " +
                                    "num_docks_available, " +
                                    "capacity, " +
                                    "ebike_ratio, " +
                                    "avail_ratio, " +
                                    "region_id)" + " values ('%s', '%s', %d, %d, '%s', %d, '%s', %d, %d, %f, %f, '%s');",
                                    combinedLine.legacy_id,
                                    combinedLine.date,
                                    combinedLine.num_bikes_available,
                                    combinedLine.num_bikes_disabled,
                                    combinedLine.station_id,
                                    combinedLine.num_ebikes_available,
                                    combinedLine.station_status,
                                    combinedLine.num_docks_available,
                                    combinedLine.capacity,
                                    (double) combinedLine.num_ebikes_available / Math.max((double) combinedLine.capacity, 1),
                                    (double) combinedLine.num_bikes_available / Math.max((double) combinedLine.capacity, 1),
                                    combinedLine.region_id
                                    );
                            System.out.println("EXECUTING: ");
                            System.out.println(cqlStatement);
                            session.execute(cqlStatement);
                        }
                        catch (Exception e){
                            System.out.println("CANNOT DESERIALIZE EVENT");
                        }

                    }
                }
            } finally {
                consumer.close();
            }
        }
        catch (Exception e){
            System.err.println(e);
        }
    }

    // Load config file for kafka
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

    // if we reset redis or the cache expires, or there is a new station, we re-retrieve station information data
    // This data barely changes, so I've put it in a cache for faster lookups
    public static void ResetStationData(Jedis jedis){
        try{
            String fullFile = "";
            URL url = new URL("https://gbfs.citibikenyc.com/gbfs/es/station_information.json");
            BufferedReader read = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String i;
            while ((i = read.readLine()) != null)
                fullFile += i;
            read.close();

            Gson gson = new Gson();
            StationInfo fullData = gson.fromJson(fullFile, StationInfo.class);
            if (fullData == null) return;
            for (StationLine station: fullData.data.stations){
                jedis.set(station.station_id, station.toString());
            }
        }
       catch (Exception e){
           System.out.println(e);
       }
    }


}
