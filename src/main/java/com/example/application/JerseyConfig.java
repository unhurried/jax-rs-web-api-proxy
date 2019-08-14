package com.example.application;

import javax.inject.Named;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@Named
@ApplicationPath("/application")
public class JerseyConfig extends ResourceConfig {
	public JerseyConfig() {
		packages("com.example");
	}
}