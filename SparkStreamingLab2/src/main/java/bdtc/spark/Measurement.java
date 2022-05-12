package bdtc.spark;

import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Measurement implements Serializable {
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private Date dateTime;
    private String areaName;
    private String sensorName;
    private Integer sensorValue;

    public Measurement() {}

    public Measurement(Date dateTime, String areaName, String sensorName, Integer sensorValue) {
        this.dateTime = dateTime;
        this.areaName = areaName;
        this.sensorName = sensorName;
        this.sensorValue = sensorValue;
    }

    public Date getDateTime() { return this.dateTime; }

    public String getAreaName() { return this.areaName; }

    public String getSensorName() { return this.sensorName; }

    public Integer getSensorValue() { return this.sensorValue; }

    public static Measurement getMinDate(Measurement measurement1, Measurement measurement2) {
        Date date1 = measurement1.getDateTime();
        Date date2 = measurement2.getDateTime();
        if (date1.before(date2))
            return measurement1;
        else
            return measurement2;
    }
}
