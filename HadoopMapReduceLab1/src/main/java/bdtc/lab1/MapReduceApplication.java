package bdtc.lab1;

import lombok.extern.log4j.Log4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import java.io.IOException;


@Log4j
public class MapReduceApplication {
    /*
    Класс MapReduce приложения
     */

    private static void readSeqFile(Path pathToFile) throws IOException {
        /*
        Функция чтения SequenceFile после выполения MapReduce Job
         */
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);

        SequenceFile.Reader reader = new SequenceFile.Reader(fs, pathToFile, conf);

        Text key = new Text();
        IntWritable val = new IntWritable();

        while (reader.next(key, val)) {
            log.info(key + ":" + val);
        }
    }


    public static void main(String[] args) throws Exception {
        /*
        Старт программы
         */

        if (args.length < 2) {
            throw new RuntimeException("You should specify input and output folders!");
        }
        /*
        Настройка конфигурации Hadoop кластера
         */
        Configuration conf = new Configuration();
        conf.set("mapreduce.map.output.compress", "true");
        conf.set("mapred.map.output.compress.codec", "org.apache.hadoop.io.compress.SnappyCodec");
        // https://stackoverflow.com/questions/17265002/hadoop-no-filesystem-for-scheme-file
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl",org.apache.hadoop.fs.LocalFileSystem.class.getName());

        /*
        Настройка job
         */
        Job job = Job.getInstance(conf, "logs alert count");
        job.setJarByClass(MapReduceApplication.class);
        job.setMapperClass(HW1Mapper.class);
        job.setReducerClass(HW1Reducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        try {

            job.addCacheFile(new Path("/glossary/matcher.txt").toUri());
        }
        catch (Exception e) {
            System.out.println("Failed add cache");
            System.exit(1);
        }

        Path outputDirectory = new Path(args[1]);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, outputDirectory);
        log.info("=====================JOB STARTED=====================");
        job.waitForCompletion(true);
        log.info("=====================JOB ENDED=====================");
        // проверяем статистику по счётчикам
        Counter counter = job.getCounters().findCounter(CounterType.MALFORMED);
        log.info("=====================COUNTERS " + counter.getName() + ": " + counter.getValue() + "=====================");
        log.info("=====================READ SEQUENCE FILE==================");
        readSeqFile(new Path(outputDirectory, "part-r-00000"));
    }
}
