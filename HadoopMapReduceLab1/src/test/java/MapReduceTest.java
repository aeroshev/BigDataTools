import bdtc.lab1.HW1Mapper;
import bdtc.lab1.HW1Reducer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;


public class MapReduceTest {

    private MapDriver<LongWritable, Text, Text, IntWritable> mapDriver;
    private ReduceDriver<Text, IntWritable, Text, IntWritable> reduceDriver;
    private MapReduceDriver<LongWritable, Text, Text, IntWritable, Text, IntWritable> mapReduceDriver;
    private Matcher matcher, matcherCode;
    private String res = "";

    private final String testLog = "2023-04-28 01:50:25 AstraLinux: 17-7-9845: Quality choice onto they think cold kind coach.\n";

    private final Pattern digitCodePattern = Pattern.compile("[\\d]+-[\\d]+-[\\d]+:");
    private final Pattern linuxCodeLog = Pattern.compile("-[0-7]-");
    private final String correctAnswer = "debug";

    @Before
    public void setUp() {
        HW1Mapper mapper = new HW1Mapper();
        HW1Reducer reducer = new HW1Reducer();
        mapDriver = MapDriver.newMapDriver(mapper);
        reduceDriver = ReduceDriver.newReduceDriver(reducer);
        mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
        mapDriver.addCacheFile(new Path("matcher.txt").toUri());
        reduceDriver.addCacheFile(new Path("matcher.txt").toUri());
        mapReduceDriver.addCacheFile(new Path("matcher.txt").toUri());
        matcher = digitCodePattern.matcher(testLog);
        if (matcher.find()) {
            matcherCode = linuxCodeLog.matcher(matcher.group());
            if (matcherCode.find()) {
                res = matcherCode.group().replaceAll("[-]", "");
            }
        }
    }

    @Test
    public void testMapper() throws IOException {
        mapDriver
                .withInput(new LongWritable(), new Text(testLog))
                .withOutput(new Text(correctAnswer), new IntWritable(1))
                .runTest();
    }

    @Test
    public void testReducer() throws IOException {
        List<IntWritable> values = new ArrayList<>();
        values.add(new IntWritable(1));
        values.add(new IntWritable(1));
        reduceDriver
                .withInput(new Text(testLog), values)
                .withOutput(new Text(testLog), new IntWritable(2))
                .runTest();
    }

    @Test
    public void testMapReduce() throws IOException {
        mapReduceDriver
                .withInput(new LongWritable(), new Text(testLog))
                .withInput(new LongWritable(), new Text(testLog))
                .withOutput(new Text(correctAnswer), new IntWritable(2))
                .runTest();
    }
}
