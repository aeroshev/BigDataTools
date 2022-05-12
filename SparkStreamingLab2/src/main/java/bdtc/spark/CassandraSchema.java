package bdtc.spark;

import java.util.Date;

public class CassandraSchema {
    private Date dateTime;
    private String areaName;
    private Integer sensorValue;

    public CassandraSchema() {}

    public CassandraSchema(Date dateTime, String areaName, Integer sensorValue) {
        this.dateTime = dateTime;
        this.areaName = areaName;
        this.sensorValue = sensorValue;
    }

    public Date getDateTime() { return this.dateTime; }

    public String getAreaName() { return this.areaName; }

    public Integer getSensorValue() { return this.sensorValue; }
}
