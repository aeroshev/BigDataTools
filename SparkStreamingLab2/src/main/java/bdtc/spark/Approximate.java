package bdtc.spark;

import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;


public class Approximate {

    public static JavaPairDStream<String, Measurement> mean(JavaPairDStream<String, Measurement> measurements) {

        return measurements
                .reduceByKeyAndWindow(
                        (i1, i2) -> new Measurement(
                                i1.getDateTime(),
                                i1.getAreaName(),
                                i1.getSensorName(),
                                i1.getSensorValue() + i2.getSensorValue()
                        ),
                        Durations.seconds(30),
                        Durations.seconds(10)
                );
    };
}
