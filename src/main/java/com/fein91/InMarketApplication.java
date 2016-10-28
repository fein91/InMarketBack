package com.fein91;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.MultipartConfigElement;

@SpringBootApplication
@Controller
public class InMarketApplication {

	@RequestMapping(value = "/{[path:[^\\.]*}")
	public String redirect() {
		return "forward:/";
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize("128KB");
		factory.setMaxRequestSize("128KB");
		return factory.createMultipartConfig();
	}

	public static void main(String[] args) {
		SpringApplication.run(InMarketApplication.class, args);
	}

}
