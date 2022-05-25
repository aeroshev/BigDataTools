package bdtc.spark;

import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import scala.Tuple2;

import java.util.*;

public class Approximate {
    private static Date minDate = new Date();

    public static Tuple2<String, Integer> getSensorValue(Tuple2<String, Measurement> measure) {
        if (measure._2 == null) {
            throw new IllegalArgumentException("====Measure is null!!!=====");
        }
        return new Tuple2<>(measure._1, measure._2.getSensorValue());
    }

    public static JavaDStream<CassandraSchema> mean(JavaPairDStream<String, Measurement> measurements) {
        /*
        Convert to area - value
         */
        try {
            JavaPairDStream<String, Integer> valueMeasurements = measurements.mapToPair(Approximate::getSensorValue);

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
        } catch (Exception e) {
            e.printStackTrace();
        }


       return null;
    };
}
