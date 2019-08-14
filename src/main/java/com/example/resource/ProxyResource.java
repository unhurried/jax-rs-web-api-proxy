package com.example.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

@Named
@Path("/proxy/{path:.*}")
public class ProxyResource {
	// Proxy Target
	private static final String TARGET_SCHEME = "http";
	private static final String TARGET_HOST = "localhost";
	private static final String TARGET_PATH = "/application/mock";
	private static final int TARGET_PORT = 8080;

	// Proxy Configuration
	private static final List<String> REQ_HEADER_TO_REMOVE = Arrays.asList("connection", "content-length", "host", "user-agent");
	private static final List<String> RES_HEADER_TO_REMOVE = Collections.<String>emptyList();

	@Context Request request;
	@Context UriInfo urlInfo;
	@Context HttpHeaders requestHeaders;

	// Redirect all the requests with GET, POST, PUT and DELETE methods to any().
	@GET
	public Response get(@PathParam("path") String path) {
		return any(path, null);
	}
	@POST
	public Response post(@PathParam("path") String path, InputStream entity) {
		return any(path, entity);
	}
	@PUT
	public Response put(@PathParam("path") String path, InputStream entity) {
		return any(path, entity);
	}
	@DELETE
	public Response delete(@PathParam("path") String path) {
		return any(path, null);
	}

	public Response any(String path, InputStream entity) {
		CloseableHttpClient hc = HttpClients.createDefault();

		// Build target URI by converting the scheme, host port and path parts of the request URI.
		URI targetUri = urlInfo.getRequestUriBuilder()
				.scheme(TARGET_SCHEME)
				.host(TARGET_HOST)
				.port(TARGET_PORT)
				.replacePath(TARGET_PATH)
				.path(path)
				.build();
		HttpRequest hrb = new HttpRequest(request.getMethod(), targetUri.toString());

		// Proxy all the request headers except ones in the black list (REQ_HEADER_TO_REMOVE).
		for (Entry<String, List<String>> entry : requestHeaders.getRequestHeaders().entrySet()) {
			if (!REQ_HEADER_TO_REMOVE.contains(entry.getKey().toLowerCase())) {
				for (String value : entry.getValue()) {
					hrb.addHeader(entry.getKey(), value);
				}
			}
		}

		// Set the request body to the proxy request as an InputStream so that it can be sent efficiently.
		hrb.setEntity(new InputStreamEntity(entity));

		// Execute a proxy request and retrieve the hedaders and the entity of the its response.
		Header[] responseHeaders = null;
		InputStream responseEntity = null;
		try {
			CloseableHttpResponse hr = hc.execute(hrb);
			responseEntity = hr.getEntity().getContent();
			responseHeaders = hr.getAllHeaders();
		} catch (IOException e) {
			throw new WebApplicationException("Proxy request failed.");
		}

		// Return the headers and the entity in a response to the client.
		ResponseBuilder rb = Response.ok();
		for (Header header: responseHeaders) {
			if (!RES_HEADER_TO_REMOVE.contains(header.getName().toLowerCase())) {
				rb = rb.header(header.getName(), header.getValue());
			}
		}
		return rb.entity(responseEntity).build();
	}

	// A HttpRequest class for Apache HttpClient that accepts an arbitrary HTTP method in its constructor.
	class HttpRequest extends HttpEntityEnclosingRequestBase {
		private final String method;
		HttpRequest(final String method, final String uri) {
			super();
			setURI(URI.create(uri));
			this.method = method;
		}

		@Override
		public String getMethod() {
			return this.method;
		}
	}
}
