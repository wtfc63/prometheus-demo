package ch.netstream.tv.prometheus.demo.springboot.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import io.micrometer.core.annotation.Counted;

@RestController
public class DemoController {


    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);


    public DemoController() {

    }


    @GetMapping
    public String root() {
        return "Hello there";
    }

    @GetMapping("/demo")
    @Counted(
            value = "demo.springboot.hellos",
            extraTags = { "implementation", "springboot" })
    public String demo() {
        return "Hello Spring Boot";
    }


	@ExceptionHandler(Exception.class)
	@ResponseStatus(
			value = HttpStatus.INTERNAL_SERVER_ERROR,
			reason = "An Exception occured")
	public String handleException(HttpServletRequest request, Exception exception) {
		String msg = String.format("An uncaught Exception occured: %s", exception.getMessage());
		logger.error(msg, exception);
		return msg;
	}

}
