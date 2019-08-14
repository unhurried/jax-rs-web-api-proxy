package com.example.application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages={"com.example"})
@EntityScan(basePackages={"com.example"})
public class Application extends SpringBootServletInitializer {
	public static void main(String[] args) {
		new Application()
				.configure(new SpringApplicationBuilder(Application.class))
				.run(args);
	}
}