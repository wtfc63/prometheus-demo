package ch.netstream.tv.prometheus.demo.springboot.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * API docs: https://wheretheiss.at/w/developer
 */
@Service
public class WhereTheISSAtService {


    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${where-is-iss-at-api.baseURL}")
    private String baseUrl;


    public List<SatelliteId> getSatelliteIds() {
        return Arrays.asList(Objects.requireNonNull(
                restTemplate.getForObject(baseUrl + "/satellites", SatelliteId[].class)));
    }

    public Satellite getSatellite(int satelliteId) {
        return restTemplate.getForObject(baseUrl + "/satellites/" + satelliteId, Satellite.class);
    }

}

