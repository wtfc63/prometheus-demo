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

import ch.netstream.tv.services.MAAS2Service;
import ch.netstream.tv.services.Satellite;
import ch.netstream.tv.services.SatelliteId;
import ch.netstream.tv.services.SolWeather;
import ch.netstream.tv.services.WhereTheISSAtService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class FetchTask {


    public static final String LABEL_IMPLEMENTATION = "implementation";
    public static final String LABEL_IMPLEMENTATION_VALUE_QUARKUS = "quarkus";

    public static final String LABEL_OBJECT = "object";
    public static final String LABEL_OBJECT_VALUE_ISS = "iss";
    public static final String LABEL_OBJECT_VALUE_MARS = "mars";

    private static final String METRIC_PREFIX = "demo";
    private static final int FALLBACK_ISS_SATELLITE_ID = 25544;


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
                METRIC_PREFIX + ".fetch.executions",
                LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS);
        gaugeCache = new HashMap<>();

        whereIsIssAtFetchTimer = registry.timer(
                METRIC_PREFIX + ".fetch.iss",
                LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS);
        issAltitudeSummary = registry.summary(
                METRIC_PREFIX + ".iss.altitude.meters.total",
                LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS,
                LABEL_OBJECT, LABEL_OBJECT_VALUE_ISS);

        marsWeatherFetchTimer = registry.timer(
                METRIC_PREFIX + ".fetch.mars",
                LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS);
        marsWeatherSummaryPressure = registry.summary(
                METRIC_PREFIX + ".mars.weather.pressure.psi.total",
                LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS,
                LABEL_OBJECT, LABEL_OBJECT_VALUE_MARS);
    }

    @Scheduled(every="5s")
    void fetchData() {
        executionCounter.increment();

        Satellite iss =
                whereIsIssAtFetchTimer.record(this::getIssInformation);
        log.debug("Got ISS information: " + iss);
        gaugeCache.put("iss.latitude", iss.getLatitude());
        registry.gauge(
                METRIC_PREFIX + ".iss.latitude",
                Tags.of(
                        LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS,
                        LABEL_OBJECT, LABEL_OBJECT_VALUE_ISS),
                gaugeCache,
                c -> c.get("iss.latitude"));
        gaugeCache.put("iss.longitude", iss.getLongitude());
        registry.gauge(
                METRIC_PREFIX + ".iss.longitude",
                Tags.of(
                        LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS,
                        LABEL_OBJECT, LABEL_OBJECT_VALUE_ISS),
                gaugeCache,
                c -> c.get("iss.longitude"));
        gaugeCache.put("iss.altitude", convertKilometersToMeters(iss.getAltitude()));
        registry.gauge(
                METRIC_PREFIX + ".iss.altitude.meters",
                Tags.of(
                        LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS,
                        LABEL_OBJECT, LABEL_OBJECT_VALUE_ISS),
                gaugeCache,
                c -> c.get("iss.altitude"));
        gaugeCache.put("iss.velocity", iss.getVelocity());
        registry.gauge(
                METRIC_PREFIX + ".iss.velocity.kmh",
                Tags.of(
                        LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS,
                        LABEL_OBJECT, LABEL_OBJECT_VALUE_ISS),
                gaugeCache,
                c -> c.get("iss.velocity"));
        issAltitudeSummary.record(
                convertKilometersToMeters(iss.getAltitude()));

        List<SolWeather> marsWeather =
                marsWeatherFetchTimer.record(this::getMarsWeather);
        log.debug("Got Mars weather for the last " + solsToGet + " sols: " + marsWeather);
        marsWeather.forEach(sw -> {
            gaugeCache.put("mars." + sw.getSol() + ".min_temp", (double) sw.getMin_temp());
            registry.gauge(
                    METRIC_PREFIX + ".mars.weather.temperature.min.celsius",
                    Tags.of(
                            LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS,
                            LABEL_OBJECT, LABEL_OBJECT_VALUE_MARS,
                            "sol", Integer.toString(sw.getSol())),
                    gaugeCache,
                    c -> c.get("mars." + sw.getSol() + ".min_temp"));

            gaugeCache.put("mars." + sw.getSol() + ".max_temp", (double) sw.getMax_temp());
            registry.gauge(
                    METRIC_PREFIX + ".mars.weather.temperature.max.celsius",
                    Tags.of(
                            LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS,
                            LABEL_OBJECT, LABEL_OBJECT_VALUE_MARS,
                            "sol", Integer.toString(sw.getSol())),
                    gaugeCache,
                    c -> c.get("mars." + sw.getSol() + ".max_temp"));

            final double psiPressure = sw.getPressure() / 1000.0;
            marsWeatherSummaryPressure.record(psiPressure);
            gaugeCache.put("mars." + sw.getSol() + ".pressure", psiPressure);
            registry.gauge(
                    METRIC_PREFIX + ".mars.weather.pressure.psi",
                    Tags.of(
                            LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_QUARKUS,
                            LABEL_OBJECT, LABEL_OBJECT_VALUE_MARS,
                            "sol", Integer.toString(sw.getSol())),
                    gaugeCache,
                    c -> c.get("mars." + sw.getSol() + ".pressure"));
        });
    }

    private Satellite getIssInformation() {
        if (issSatelliteId == null) {
            issSatelliteId = whereTheISSAtService.getSatelliteIds().stream()
                    .filter(s -> s.getName().equals("iss"))
                    .map(SatelliteId::getId)
                    .findFirst()
                    .orElse(FALLBACK_ISS_SATELLITE_ID);
        }
        return whereTheISSAtService.getSatellite(issSatelliteId);
    }

    private List<SolWeather> getMarsWeather() {
        try {
            SolWeather latest = maas2Service.getLatestWeather();
            return IntStream.range(latest.getSol() - solsToGet + 1, latest.getSol() + 1)
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
