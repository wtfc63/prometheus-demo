package ch.netstream.tv.prometheus.demo.springboot.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ch.netstream.tv.prometheus.demo.springboot.tasks.FetchTask;
import io.micrometer.core.annotation.Timed;

/**
 * API docs: https://maas2.apollorion.com/
 */
@Service
public class MAAS2Service {


    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${maas2-api.baseURL}")
    private String baseUrl;


    public SolWeather getLatestWeather() {
        return restTemplate.getForObject(baseUrl + "/", SolWeather.class);
    }

    @Timed(
            value = "demo.service.maas2.getsolweather",
            extraTags = { FetchTask.LABEL_IMPLEMENTATION, FetchTask.LABEL_IMPLEMENTATION_VALUE_SPRINGBOOT }
    )
    public SolWeather getSolWeather(int sol) {
        return restTemplate.getForObject(baseUrl + "/" + sol, SolWeather.class);
    }

}
