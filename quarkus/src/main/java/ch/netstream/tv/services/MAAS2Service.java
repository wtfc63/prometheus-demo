package ch.netstream.tv.services;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.netstream.tv.FetchTask;
import io.micrometer.core.annotation.Timed;

/**
 * API docs: https://maas2.apollorion.com/
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey="maas2-api")
public interface MAAS2Service {

    @GET
    @Path("/")
    SolWeather getLatestWeather();

    @GET
    @Path("/{sol}")
    SolWeather getSolWeather(@PathParam int sol);

}
