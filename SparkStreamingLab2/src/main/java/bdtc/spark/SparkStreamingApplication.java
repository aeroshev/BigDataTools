package bdtc.spark;

import java.util.*;
import java.lang.NullPointerException;
import org.slf4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.*;
import java.util.Date;
import scala.Tuple2;



public class SparkStreamingApplication {
    static String cassandraAddress = "localhost:9042";
    static String kafkaAddress = "localhost:29092";

    public static void main(String[] args) throws Exception {
        /*
        Kafka topics
         */
        Collection<String> topics = Arrays.asList(
                "area_1", "area_2", "area_3", "area_4", "area_5", "area_6", "area_7", "area_8", "area_9", "area_10"
        );
        /*
        Spark configuration
        set app name and address cassandra
         */
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[*]");
        sparkConf.setAppName("SensorApproximate");
        sparkConf.set("spark.cassandra.connection.host", cassandraAddress);
        final Logger logger = sparkConf.log();
        /*
        Setup Spark Streaming context
         */
        JavaStreamingContext streamingContext = new JavaStreamingContext(
                sparkConf, Durations.seconds(10)
        );
        /*
        Setup kafka settings
         */
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", kafkaAddress);
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "spark-streaming-approximate-value");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);
        /*
        Create stream from kafka
         */
        JavaInputDStream<ConsumerRecord<String, String>> stream =
          KafkaUtils.createDirectStream(
                  streamingContext,
                  LocationStrategies.PreferConsistent(),
                  ConsumerStrategies.Subscribe(topics, kafkaParams)
          );
        /*
        Extract data from kafka from topics
         */
        JavaPairDStream<String, String> measurementsByTopic = stream
                .mapToPair(
                    record -> new Tuple2<>(record.topic(), record.value())
                );
        logger.info("========Input data from kafka: " + stream.toString() + "=========");
        /*
        Convert to lines
         */
        JavaPairDStream<String, String> rawMeasurements = measurementsByTopic.flatMapValues(
                raw_measure -> Arrays.asList(raw_measure.split(";")).iterator()
        );
        /*
        Casting to Measure object
         */
        JavaPairDStream<String, Measurement> measurements = rawMeasurements.mapValues(
                measure -> {
                    String[] patches = measure.split(",");
                    patches = StringUtils.stripAll(patches);
                    try {
                        Date parsedDate = Measurement.dateFormat.parse(patches[0]);
                        Integer parsedValue = Integer.parseInt(patches[3]);

                        return new Measurement(
                                parsedDate,
                                patches[1],
                                patches[2],
                                parsedValue
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
        );
        /*
        Extract only values for Cassandra and calculate mean
         */
        try {
            JavaDStream<CassandraSchema> cassandraMeasurements = Approximate.mean(measurements);
            /*
            Save to Cassandra table spark.measurements
            */
            cassandraMeasurements.foreachRDD(
                    rdd -> {
                        javaFunctions(rdd)
                                .writerBuilder("spark", "measurements", mapToRow(CassandraSchema.class))
                                .saveToCassandra();
                    }
            );
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        /*
        Run app
         */
        streamingContext.start();
        streamingContext.awaitTermination();
    }
}
