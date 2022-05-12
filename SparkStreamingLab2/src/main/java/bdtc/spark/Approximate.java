package bdtc.spark;

import org.slf4j.Logger;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import scala.Tuple2;

import java.util.*;

public class Approximate {
    private static Date minDate = new Date();

    public static JavaDStream<CassandraSchema> mean(JavaPairDStream<String, Measurement> measurements) {
        /*
        Convert to area - value
         */
        JavaPairDStream<String, Integer> valueMeasurements = measurements.mapToPair(
                measurement -> new Tuple2<>(measurement._1, measurement._2.getSensorValue())
        );
        /*
        Extract start date
         */
        JavaPairDStream<String, Measurement> minDateMeasurement = measurements.reduceByKey(Measurement::getMinDate);
        /*
        Collect mean value at last minute
         */
        JavaPairDStream<String, Integer> meanMeasurements = valueMeasurements
                .reduceByKey((i1, i2) -> (i1 + i2) / 2);
        /*
        Prepare answer for Cassandra
         */
        return meanMeasurements.map(
                mean -> new CassandraSchema(minDate, mean._1, mean._2)
        );
    };
}
