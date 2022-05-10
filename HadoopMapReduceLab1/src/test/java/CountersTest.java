import bdtc.lab1.CounterType;
import bdtc.lab1.HW1Mapper;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import org.apache.hadoop.fs.Path;
import java.util.regex.*;

import static org.junit.Assert.assertEquals;


public class CountersTest {

    private MapDriver<LongWritable, Text, Text, IntWritable> mapDriver;

    private final String testMalformedLog = "mama mila ramu";
    private final String testLog = "2023-04-28 01:50:25 AstraLinux: 17-7-9845: Quality choice onto they think cold kind coach.\n";
    private final Pattern digitCodePattern = Pattern.compile("[\\d]+-[\\d]+-[\\d]+:");
    private final Pattern linuxCodeLog = Pattern.compile("-[0-7]-");
    private final String correctAnswer = "debug";

    @Before
    public void setUp() {
        HW1Mapper mapper = new HW1Mapper();
        mapDriver = MapDriver.newMapDriver(mapper);
        mapDriver.addCacheFile(new Path("matcher.txt").toUri());
    }

    @Test
    public void testMapperCounterOne() throws IOException  {
        mapDriver
                .withInput(new LongWritable(), new Text(testMalformedLog))
                .runTest();
        assertEquals("Expected 1 counter increment", 1, mapDriver.getCounters()
                .findCounter(CounterType.MALFORMED).getValue());
    }

    @Test
    public void testMapperCounterZero() throws IOException {
        Matcher matcher = digitCodePattern.matcher(testLog);
        String res = "";
        if (matcher.find()) {
            Matcher codeMatcher = linuxCodeLog.matcher(matcher.group());
            if (codeMatcher.find()) {
                res = codeMatcher.group().replaceAll("[-]", "");
                System.out.println(res);
            }
        }
        mapDriver
                .withInput(new LongWritable(), new Text(testLog))
                .withOutput(new Text(correctAnswer), new IntWritable(1))
                .runTest();
        assertEquals("Expected 1 counter increment", 0, mapDriver.getCounters()
                .findCounter(CounterType.MALFORMED).getValue());
    }

    @Test
    public void testMapperCounters() throws IOException {
        Matcher matcher = digitCodePattern.matcher(testLog);
        String res = "";
        if (matcher.find()) {
            Matcher codeMatcher = linuxCodeLog.matcher(matcher.group());
            if (codeMatcher.find()) {
                res = codeMatcher.group().replaceAll("[-]", "");
                System.out.println(res);
            }
        }
        mapDriver
                .withInput(new LongWritable(), new Text(testLog))
                .withInput(new LongWritable(), new Text(testMalformedLog))
                .withInput(new LongWritable(), new Text(testMalformedLog))
                .withOutput(new Text(correctAnswer), new IntWritable(1))
                .runTest();

        assertEquals("Expected 2 counter increment", 2, mapDriver.getCounters()
                .findCounter(CounterType.MALFORMED).getValue());
    }
}

