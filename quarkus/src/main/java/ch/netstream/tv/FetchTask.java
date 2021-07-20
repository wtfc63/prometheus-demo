package ch.netstream.tv;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class FetchTask {


    public static final int FALLBACK_ISS_SATELLITE_ID = 25544;

    private final MeterRegistry registry;
    private final Counter executionCounter;
    private final Map<String, Double> gaugeCache;

    private final Timer whereIsIssAtFetchTimer;
    private final DistributionSummary issAltitudeSummary;

    private final Timer marsWeatherFetchTimer;
    private final DistributionSummary marsWeatherSummaryPressure;

    private Integer issSatelliteId;

    @Inject
    Logger log;

    @Inject
    @RestClient
    WhereTheISSAtService whereTheISSAtService;

    @Inject
    @RestClient
    MAAS2Service maas2Service;

    @ConfigProperty(name ="fetch.mars-weather.sols-to-get")
    int solsToGet;


    public FetchTask(MeterRegistry registry) {
        this.registry = registry;
        executionCounter = registry.counter(
                "demo.quarkus.fetch.executions",
                "implementation", "quarkus");
        gaugeCache = new HashMap<>();

        whereIsIssAtFetchTimer = registry.timer(
                "demo.quarkus.fetch.iss",
                "implementation", "quarkus");
        issAltitudeSummary = registry.summary(
                "demo.quarkus.iss.altitude.meters.total",
                "implementation", "quarkus",
                "object", "iss");

        marsWeatherFetchTimer = registry.timer(
                "demo.quarkus.fetch.mars",
                "implementation", "quarkus");
        marsWeatherSummaryPressure = registry.summary(
                "demo.quarkus.mars.weather.pressure.psi.total",
                "implementation", "quarkus",
                "object", "mars");
    }

    @Scheduled(every="5s")
    void fetchData() {
        executionCounter.increment();

        WhereTheISSAtService.Satellite iss = whereIsIssAtFetchTimer.record(this::getIssInformation);
        log.debug("Got ISS information: " + iss);
        gaugeCache.put("iss.latitude", iss.latitude());
        registry.gauge(
                "demo.quarkus.iss.latitude",
                Tags.of("implementation", "quarkus", "object", "iss"),
                gaugeCache,
                c -> c.get("iss.latitude"));
        gaugeCache.put("iss.longitude", iss.longitude());
        registry.gauge(
                "demo.quarkus.iss.longitude",
                Tags.of("implementation", "quarkus", "object", "iss"),
                gaugeCache,
                c -> c.get("iss.longitude"));
        gaugeCache.put("iss.altitude", convertKilometersToMeters(iss.altitude()));
        registry.gauge(
                "demo.quarkus.iss.altitude.meters",
                Tags.of("implementation", "quarkus", "object", "iss"),
                gaugeCache,
                c -> c.get("iss.altitude"));
        gaugeCache.put("iss.velocity", iss.velocity());
        registry.gauge(
                "demo.quarkus.iss.velocity.kmh",
                Tags.of("implementation", "quarkus", "object", "iss"),
                gaugeCache,
                c -> c.get("iss.velocity"));
        issAltitudeSummary.record(
                convertKilometersToMeters(iss.altitude()));

        List<MAAS2Service.SolWeather> marsWeather = marsWeatherFetchTimer.record(this::getMarsWeather);
        log.debug("Got Mars weather for the last " + solsToGet + " sols: " + marsWeather);
        marsWeather.forEach(sw -> {
            gaugeCache.put("mars." + sw.sol() + ".min_temp", (double) sw.min_temp());
            registry.gauge(
                    "demo.quarkus.mars.weather.temperature.min.celsius",
                    Tags.of(
                            "implementation", "quarkus",
                            "object", "mars",
                            "sol", Integer.toString(sw.sol())),
                    gaugeCache,
                    c -> c.get("mars." + sw.sol() + ".min_temp"));

            gaugeCache.put("mars." + sw.sol() + ".max_temp", (double) sw.max_temp());
            registry.gauge(
                    "demo.quarkus.mars.weather.temperature.max.celsius",
                    Tags.of(
                            "implementation", "quarkus",
                            "object", "mars",
                            "sol", Integer.toString(sw.sol())),
                    gaugeCache,
                    c -> c.get("mars." + sw.sol() + ".max_temp"));

            final double psiPressure = sw.pressure() / 1000.0;
            marsWeatherSummaryPressure.record(psiPressure);
            gaugeCache.put("mars." + sw.sol() + ".pressure", psiPressure);
            registry.gauge(
                    "demo.quarkus.mars.weather.pressure.psi",
                    Tags.of(
                            "implementation", "quarkus",
                            "object", "mars",
                            "sol", Integer.toString(sw.sol())),
                    gaugeCache,
                    c -> c.get("mars." + sw.sol() + ".pressure"));
        });
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

    private List<MAAS2Service.SolWeather> getMarsWeather() {
        try {
            MAAS2Service.SolWeather latest = maas2Service.getLatestWeather();
            return IntStream.range(latest.sol() - solsToGet + 1, latest.sol() + 1)
                    .mapToObj(s -> maas2Service.getSolWeather(s))
                    .collect(Collectors.toList());
        } catch (RuntimeException ex) {
            return Collections.emptyList();
        }
    }

    private Double convertKilometersToMeters(Double km) {
        return (km != null) ? (1000 * km) : null;
    }

}
