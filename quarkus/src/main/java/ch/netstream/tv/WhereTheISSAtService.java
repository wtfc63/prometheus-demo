package ch.netstream.tv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/v1")
@RegisterRestClient(configKey="where-is-iss-at-api")
public interface WhereTheISSAtService {

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonSerialize
    public record SatelliteId (int id, String name) {}

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonSerialize
    public record Satellite (
            int id,
            String name,
            double latitude,
            double longitude,
            double altitude,
            double velocity,
            String visibility,
            double footprint,
            long timestamp,
            double daynum,
            double solar_lat,
            double solar_lon,
            String units
    ) {}

    @GET
    @Path("/satellites")
    List<SatelliteId> getSatelliteIds();

    @GET
    @Path("/satellites/{satelliteId}")
    Satellite getSatellite(@PathParam int satelliteId);

}
