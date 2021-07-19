package ch.netstream.tv;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Path("/demo")
public class DemoResource {


    private final MeterRegistry registry;

    private final Counter helloCounter;


    public DemoResource(MeterRegistry registry) {
        this.registry = registry;
        helloCounter = registry.counter("demo.quarkus.hellos", "implementation", "quarkus");
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String demo() {
        helloCounter.increment();
        return "Hello RESTEasy";
    }

}