package ch.netstream.tv.services;

import java.time.LocalTime;
import java.time.OffsetDateTime;

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


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public OffsetDateTime getTerrestrial_date() {
        return terrestrial_date;
    }

    public void setTerrestrial_date(OffsetDateTime terrestrial_date) {
        this.terrestrial_date = terrestrial_date;
    }

    public int getLs() {
        return ls;
    }

    public void setLs(int ls) {
        this.ls = ls;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public int getMin_temp() {
        return min_temp;
    }

    public void setMin_temp(int min_temp) {
        this.min_temp = min_temp;
    }

    public int getMax_temp() {
        return max_temp;
    }

    public void setMax_temp(int max_temp) {
        this.max_temp = max_temp;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public String getPressure_string() {
        return pressure_string;
    }

    public void setPressure_string(String pressure_string) {
        this.pressure_string = pressure_string;
    }

    public Double getAbs_humidity() {
        return abs_humidity;
    }

    public void setAbs_humidity(Double abs_humidity) {
        this.abs_humidity = abs_humidity;
    }

    public Double getWind_speed() {
        return wind_speed;
    }

    public void setWind_speed(Double wind_speed) {
        this.wind_speed = wind_speed;
    }

    public String getAtmo_opacity() {
        return atmo_opacity;
    }

    public void setAtmo_opacity(String atmo_opacity) {
        this.atmo_opacity = atmo_opacity;
    }

    public LocalTime getSunrise() {
        return sunrise;
    }

    public void setSunrise(LocalTime sunrise) {
        this.sunrise = sunrise;
    }

    public LocalTime getSunset() {
        return sunset;
    }

    public void setSunset(LocalTime sunset) {
        this.sunset = sunset;
    }

    public String getLocal_uv_irradiance_index() {
        return local_uv_irradiance_index;
    }

    public void setLocal_uv_irradiance_index(String local_uv_irradiance_index) {
        this.local_uv_irradiance_index = local_uv_irradiance_index;
    }

    public int getMin_gts_temp() {
        return min_gts_temp;
    }

    public void setMin_gts_temp(int min_gts_temp) {
        this.min_gts_temp = min_gts_temp;
    }

    public int getMax_gts_temp() {
        return max_gts_temp;
    }

    public void setMax_gts_temp(int max_gts_temp) {
        this.max_gts_temp = max_gts_temp;
    }

    public String getWind_direction() {
        return wind_direction;
    }

    public void setWind_direction(String wind_direction) {
        this.wind_direction = wind_direction;
    }

    public int getSol() {
        return sol;
    }

    public void setSol(int sol) {
        this.sol = sol;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getTZ_Data() {
        return TZ_Data;
    }

    public void setTZ_Data(String TZ_Data) {
        this.TZ_Data = TZ_Data;
    }


    @Override
    public String toString() {
        return "SolWeather{" +
                "status=" + status +
                ", id=" + id +
                ", terrestrial_date=" + terrestrial_date +
                ", ls=" + ls +
                ", season='" + season + '\'' +
                ", min_temp=" + min_temp +
                ", max_temp=" + max_temp +
                ", pressure=" + pressure +
                ", pressure_string='" + pressure_string + '\'' +
                ", abs_humidity=" + abs_humidity +
                ", wind_speed=" + wind_speed +
                ", atmo_opacity='" + atmo_opacity + '\'' +
                ", sunrise=" + sunrise +
                ", sunset=" + sunset +
                ", local_uv_irradiance_index='" + local_uv_irradiance_index + '\'' +
                ", min_gts_temp=" + min_gts_temp +
                ", max_gts_temp=" + max_gts_temp +
                ", wind_direction='" + wind_direction + '\'' +
                ", sol=" + sol +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", TZ_Data='" + TZ_Data + '\'' +
                '}';
    }

}
