package ch.netstream.tv.prometheus.demo.springboot.services;

import lombok.Data;

@Data
public class Satellite {

    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private double altitude;
    private double velocity;
    private String visibility;
    private double footprint;
    private long timestamp;
    private double daynum;
    private double solar_lat;
    private double solar_lon;
    private String unit;

}
