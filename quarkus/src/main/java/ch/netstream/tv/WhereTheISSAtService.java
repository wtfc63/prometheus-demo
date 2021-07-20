package ch.netstream.tv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * API docs: https://wheretheiss.at/w/developer
 */
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey="where-is-iss-at-api")
public interface WhereTheISSAtService {

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonSerialize
    record SatelliteId (int id, String name) {}

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonSerialize
    record Satellite (
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

