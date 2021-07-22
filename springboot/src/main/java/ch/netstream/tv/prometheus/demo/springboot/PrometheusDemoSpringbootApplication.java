package ch.netstream.tv.prometheus.demo.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.scheduling.annotation.EnableScheduling;

import ch.netstream.tv.prometheus.demo.springboot.services.Satellite;
import ch.netstream.tv.prometheus.demo.springboot.services.SatelliteId;
import ch.netstream.tv.prometheus.demo.springboot.services.SolWeather;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

@TypeHint(types = { // Needed for the Native Image build to enable Reflections access for Jackson
		SatelliteId.class,
		Satellite.class,
		SolWeather.class
})
@SpringBootApplication
@EnableScheduling
public class PrometheusDemoSpringbootApplication {

	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

	public static void main(String[] args) {
		SpringApplication.run(PrometheusDemoSpringbootApplication.class, args);
	}

}
