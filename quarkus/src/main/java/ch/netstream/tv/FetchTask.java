package ch.netstream.tv;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;

@ApplicationScoped
public class FetchTask {


    public static final int FALLBACK_ISS_SATELLITE_ID = 25544;

    private final MeterRegistry registry;
    private final Counter executionCounter;

    private final Timer whereIsIssAtFetchTimer;
    private final DistributionSummary issAltitudeSummary;

    private Integer issSatelliteId;


    @Inject
    @RestClient
    WhereTheISSAtService whereTheISSAtService;


    public FetchTask(MeterRegistry registry) {
        this.registry = registry;
        executionCounter = registry.counter(
                "demo.quarkus.fetch.executions",
                "implementation", "quarkus");
        whereIsIssAtFetchTimer = registry.timer(
                "demo.quarkus.fetch.iss",
                "implementation", "quarkus");
        issAltitudeSummary = registry.summary(
                "demo.quarkus.iss.altitude.meters.total",
                "implementation", "quarkus",
                "object", "iss");
    }

    @Scheduled(every="5s")
    void fetchData() {
        executionCounter.increment();

        WhereTheISSAtService.Satellite iss = whereIsIssAtFetchTimer.record(this::getIssInformation);
        registry.gauge(
                "demo.quarkus.iss.latitude",
                Tags.of("implementation", "quarkus", "object", "iss"),
                iss.latitude());
        registry.gauge(
                "demo.quarkus.iss.longitude",
                Tags.of("implementation", "quarkus", "object", "iss"),
                iss.longitude());
        registry.gauge(
                "demo.quarkus.iss.altitude.meters",
                Tags.of("implementation", "quarkus", "object", "iss"),
                convertKilometersToMeters(iss.altitude()));
        registry.gauge(
                "demo.quarkus.iss.velocity.kmh",
                Tags.of("implementation", "quarkus", "object", "iss"),
                iss.velocity());
        issAltitudeSummary.record(convertKilometersToMeters(iss.altitude()));
    }

    private WhereTheISSAtService.Satellite getIssInformation() {
        if (issSatelliteId == null) {
            issSatelliteId = whereTheISSAtService.getSatelliteIds().stream()
                    .filter(s -> s.name().equals("iss"))
                    .map(WhereTheISSAtService.SatelliteId::id)
                    .findFirst()
                    .orElse(FALLBACK_ISS_SATELLITE_ID);
        }
        return whereTheISSAtService.getSatellite(issSatelliteId);
    }

    private double convertKilometersToMeters(double km) {
        return 1000 * km;
    }

}
