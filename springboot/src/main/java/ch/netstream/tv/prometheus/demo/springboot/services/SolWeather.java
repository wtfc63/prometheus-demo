package ch.netstream.tv.prometheus.demo.springboot.services;

import java.time.LocalTime;
import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class SolWeather {

    private int status;
    private int id;
    private OffsetDateTime terrestrial_date;
    private int ls;
    private String season;
    private int min_temp;
    private int max_temp;
    private int pressure;
    private String pressure_string;
    private Double abs_humidity;
    private Double wind_speed;
    private String atmo_opacity;
    private LocalTime sunrise;
    private LocalTime sunset;
    private String local_uv_irradiance_index;
    private int min_gts_temp;
    private int max_gts_temp;
    private String wind_direction;
    private int sol;
    private String unitOfMeasure;
    private String TZ_Data;

}
