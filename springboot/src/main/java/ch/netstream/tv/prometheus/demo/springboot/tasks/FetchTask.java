package ch.netstream.tv.prometheus.demo.springboot.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.netstream.tv.prometheus.demo.springboot.services.MAAS2Service;
import ch.netstream.tv.prometheus.demo.springboot.services.Satellite;
import ch.netstream.tv.prometheus.demo.springboot.services.SatelliteId;
import ch.netstream.tv.prometheus.demo.springboot.services.SolWeather;
import ch.netstream.tv.prometheus.demo.springboot.services.WhereTheISSAtService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

@Component
public class FetchTask {


    public static final String LABEL_IMPLEMENTATION = "implementation";
    public static final String LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT = "springboot";

    public static final String LABEL_OBJECT = "object";
    public static final String LABEL_OBJECT_VALUE_ISS = "iss";
    public static final String LABEL_OBJECT_VALUE_MARS = "mars";

    private static final String METRIC_PREFIX = "demo";
    private static final int FALLBACK_ISS_SATELLITE_ID = 25544;


    private static final Logger log = LoggerFactory.getLogger(FetchTask.class);


    private final MeterRegistry registry;
    private final WhereTheISSAtService whereTheISSAtService;
    private final MAAS2Service maas2Service;

    private final Counter executionCounter;
    private final Map<String, Double> gaugeCache;

    private final Timer whereIsIssAtFetchTimer;
    private final DistributionSummary issAltitudeSummary;

    private final Timer marsWeatherFetchTimer;
    private final DistributionSummary marsWeatherSummaryPressure;

    private Integer issSatelliteId;

    @Value("${fetch.mars-weather.sols-to-get}")
    int solsToGet;

    @Autowired
    public FetchTask(
            MeterRegistry registry,
            WhereTheISSAtService whereTheISSAtService,
            MAAS2Service maas2Service) {
        this.registry = registry;
        this.whereTheISSAtService = whereTheISSAtService;
        this.maas2Service = maas2Service;

        executionCounter = registry.counter(
                METRIC_PREFIX + ".fetch.executions",
                LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT);
        gaugeCache = new HashMap<>();

        whereIsIssAtFetchTimer = registry.timer(
                METRIC_PREFIX + ".fetch.iss",
                LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT);
        issAltitudeSummary = registry.summary(
                METRIC_PREFIX + ".iss.altitude.meters.total",
                LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT,
                LABEL_OBJECT, LABEL_OBJECT_VALUE_ISS);

        marsWeatherFetchTimer = registry.timer(
                METRIC_PREFIX + ".fetch.mars",
                LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT);
        marsWeatherSummaryPressure = registry.summary(
                METRIC_PREFIX + ".mars.weather.pressure.psi.total",
                LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT,
                LABEL_OBJECT, LABEL_OBJECT_VALUE_MARS);
    }

    @Timed(
            value = METRIC_PREFIX + ".fetch",
            extraTags = { LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT }
    )
    @Scheduled(fixedRate = 30 * 1000)
    public void fetchData() {
        executionCounter.increment();

        Satellite iss =
                whereIsIssAtFetchTimer.record(this::getIssInformation);
        log.debug("Got ISS information: " + iss);
        gaugeCache.put("iss.latitude", Objects.requireNonNull(iss).getLatitude());
        registry.gauge(
                METRIC_PREFIX + ".iss.latitude",
                Tags.of(
                        LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT,
                        LABEL_OBJECT, LABEL_OBJECT_VALUE_ISS),
                gaugeCache,
                c -> c.get("iss.latitude"));
        gaugeCache.put("iss.longitude", iss.getLongitude());
        registry.gauge(
                METRIC_PREFIX + ".iss.longitude",
                Tags.of(
                        LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT,
                        LABEL_OBJECT, LABEL_OBJECT_VALUE_ISS),
                gaugeCache,
                c -> c.get("iss.longitude"));
        gaugeCache.put("iss.altitude", convertKilometersToMeters(iss.getAltitude()));
        registry.gauge(
                METRIC_PREFIX + ".iss.altitude.meters",
                Tags.of(
                        LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT,
                        LABEL_OBJECT, LABEL_OBJECT_VALUE_ISS),
                gaugeCache,
                c -> c.get("iss.altitude"));
        gaugeCache.put("iss.velocity", iss.getVelocity());
        registry.gauge(
                METRIC_PREFIX + ".iss.velocity.kmh",
                Tags.of(
                        LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT,
                        LABEL_OBJECT, LABEL_OBJECT_VALUE_ISS),
                gaugeCache,
                c -> c.get("iss.velocity"));
        issAltitudeSummary.record(
                convertKilometersToMeters(iss.getAltitude()));

        List<SolWeather> marsWeather =
                marsWeatherFetchTimer.record(this::getMarsWeather);
        log.debug("Got Mars weather for the last " + solsToGet + " sols: " + marsWeather);
        Objects.requireNonNull(marsWeather).forEach(sw -> {
            gaugeCache.put("mars." + sw.getSol() + ".min_temp", (double) sw.getMin_temp());
            registry.gauge(
                    METRIC_PREFIX + ".mars.weather.temperature.min.celsius",
                    Tags.of(
                            LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT,
                            LABEL_OBJECT, LABEL_OBJECT_VALUE_MARS,
                            "sol", Integer.toString(sw.getSol())),
                    gaugeCache,
                    c -> c.get("mars." + sw.getSol() + ".min_temp"));

            gaugeCache.put("mars." + sw.getSol() + ".max_temp", (double) sw.getMax_temp());
            registry.gauge(
                    METRIC_PREFIX + ".mars.weather.temperature.max.celsius",
                    Tags.of(
                            LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT,
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
                            LABEL_IMPLEMENTATION, LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT,
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
                    .mapToObj(maas2Service::getSolWeather)
                    .collect(Collectors.toList());
        } catch (RuntimeException ex) {
            return Collections.emptyList();
        }
    }

    private Double convertKilometersToMeters(Double km) {
        return (km != null) ? (1000 * km) : null;
    }

}
