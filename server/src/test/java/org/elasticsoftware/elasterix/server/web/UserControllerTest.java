package org.elasticsoftware.elasterix.server.web;

import static org.testng.Assert.assertEquals;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
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
	public void testGetUserNotExisting() throws Exception {
		String url = "users/lwoltersXYZ";
		ListenableFuture<Response> responseFuture = httpClient.prepareGet(baseUrl + url).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.NOT_FOUND.getCode());
		log.info(response.getResponseBody("UTF-8"));
	}
	
	@Test(enabled = true)
	public void testCreateUserPostNoData() throws Exception {
		String url = "users/lwolters";
		String data = "";
		ListenableFuture<Response> responseFuture = httpClient.preparePost(baseUrl + url).setBody(data).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.NO_CONTENT.getCode());
	}
	
	@Test(enabled = true)
	public void testCreateUserPostDataNotState() throws Exception {
		String url = "users/lwolters";
		String data = "{\"state\" : {\"id\" : 122}}";
		ListenableFuture<Response> responseFuture = httpClient.preparePost(baseUrl + url)
				.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
				.setBody(data).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.NOT_ACCEPTABLE.getCode());
	}
	
	@Test(enabled = true)
	public void testCreateUserPostDataState() throws Exception {
		String url = "users/lwolters";
		String data = "{\"username\" : \"lwolters\"}";
		ListenableFuture<Response> responseFuture = httpClient.preparePost(baseUrl + url)
				.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
				.setBody(data).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.OK.getCode());
		assertEquals(CONTENT_TYPE_JSON, response.getContentType());
		log.info(response.getResponseBody());
	}
}
