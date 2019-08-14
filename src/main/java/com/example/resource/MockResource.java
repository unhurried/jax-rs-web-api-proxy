package com.example.resource;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/** A mock resource that returns a received HTTP request in the resonse body. */
@Named
@Path("/mock/{path:.*}")
@Produces(MediaType.APPLICATION_JSON)
public class MockResource {
	@Context Request request;
	@Context UriInfo uriInfo;
	@Context HttpHeaders httpHeaders;

	// Redirect all the requests with GET, POST, PUT and DELETE methods to any().
	@GET
	public Response get() {
		return any(null);
	}
	@POST
	public Response post(String body) {
		return any(body);
	}
	@PUT
	public Response put(String body) {
		return any(body);
	}
	@DELETE
	public Response delete() {
		return any(null);
	}

	private Response any(String body) {
		Map<String, Object> response = new HashMap<>();
		response.put("method", request.getMethod());
		response.put("path", uriInfo.getPath());
		response.put("query_parameters", uriInfo.getQueryParameters());
		response.put("headers", httpHeaders.getRequestHeaders());
		response.put("body", body);
		return Response.ok().entity(response).build();
	}
}
