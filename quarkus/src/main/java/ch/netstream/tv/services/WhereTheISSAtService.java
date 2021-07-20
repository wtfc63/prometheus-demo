package ch.netstream.tv.services;

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

    @GET
    @Path("/satellites")
    List<SatelliteId> getSatelliteIds();

    @GET
    @Path("/satellites/{satelliteId}")
    Satellite getSatellite(@PathParam int satelliteId);

}

