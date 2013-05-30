package org.elasticsoftware.elasterix.server.web;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.biasedbit.efflux.logging.Logger;
import com.google.common.net.HttpHeaders;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

/**
 * @author Leonard Wolters
 */
public class UserControllerTest extends AbstractControllerTest {
	private static final Logger log = Logger.getLogger(UserControllerTest.class);
	
	@Test(enabled = true)
	public void testCreateUserGet() throws Exception {
		String url = "users/lwolters/create";
		ListenableFuture<Response> responseFuture = httpClient.prepareGet(baseUrl + url).execute();
		Response response = responseFuture.get();
		// 406 = not acceptable
		assertEquals(406, response.getStatusCode());
	}
	
	@Test(enabled = true)
	public void testCreateUserPostNoData() throws Exception {
		String url = "users/lwolters/create";
		String data = "";
		ListenableFuture<Response> responseFuture = httpClient.preparePost(baseUrl + url).setBody(data).execute();
		Response response = responseFuture.get();
		// 204 = no content
		assertEquals(204, response.getStatusCode());
	}
	
	@Test(enabled = true)
	public void testCreateUserPostDataNotState() throws Exception {
		String url = "users/lwolters/create";
		String data = "{\"state\" : {\"id\" : 122}}";
		ListenableFuture<Response> responseFuture = httpClient.preparePost(baseUrl + url)
				.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
				.setBody(data).execute();
		Response response = responseFuture.get();
		// 204 = no content
		assertEquals(204, response.getStatusCode());
	}
	
	@Test(enabled = true)
	public void testCreateUserPostDataState() throws Exception {
		String url = "users/lwolters/create";
		String data = "{\"username\" : \"lwolters\"}";
		ListenableFuture<Response> responseFuture = httpClient.preparePost(baseUrl + url)
				.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
				.setBody(data).execute();
		Response response = responseFuture.get();
		assertEquals(200, response.getStatusCode());
		assertEquals(response.getContentType(), CONTENT_TYPE_JSON);
		log.info(response.getResponseBody());
	}

	@Test(enabled = false)
	public void testGetUserNotExisting() throws Exception {
		String url = "users/lwolters";
		ListenableFuture<Response> responseFuture = httpClient.prepareGet(baseUrl + url).execute();
		Response response = responseFuture.get();
		assertEquals(200, response.getStatusCode());
		assertEquals(response.getContentType(), CONTENT_TYPE_JSON);
		log.info(response.getResponseBody("UTF-8"));
	}
}
