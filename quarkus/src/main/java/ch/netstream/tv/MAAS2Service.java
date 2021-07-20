package ch.netstream.tv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import java.time.LocalTime;
import java.time.OffsetDateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * API docs: https://maas2.apollorion.com/
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey="maas2-api")
public interface MAAS2Service {

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonSerialize
    record SolWeather (
            int status,
            int id,
            OffsetDateTime terrestrial_date,
            int ls,
            String season,
            int min_temp,
            int max_temp,
            int pressure,
            String pressure_string,
            Double abs_humidity,
            Double wind_speed,
            String atmo_opacity,
            LocalTime sunrise,
            LocalTime sunset,
            String local_uv_irradiance_index,
            int min_gts_temp,
            int max_gts_temp,
            String wind_direction,
            int sol,
            String unitOfMeasure,
            String TZ_Data
    ) {}

    @GET
    @Path("/")
    SolWeather getLatestWeather();

    @GET
    @Path("/{sol}")
    SolWeather getSolWeather(@PathParam int sol);

}
