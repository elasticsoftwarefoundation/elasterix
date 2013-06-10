package org.elasticsoftware.elasterix.server.web;

import static org.testng.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.elasticsoftware.elasterix.server.ApiConfig;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import com.google.common.net.HttpHeaders;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

/**
 * @author Leonard Wolters
 */
public class UserControllerTest extends AbstractControllerTest {
	private static final Logger log = Logger.getLogger(UserControllerTest.class);
	private int count = 1;

	@Test(enabled = true)
	public void testGetUserNotExisting() throws Exception {
		String url = "users/lwolters" + count++;
		ListenableFuture<Response> responseFuture = httpClient.prepareGet(baseUrl + url).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.NOT_FOUND.getCode());
		log.info(response.getResponseBody("UTF-8"));
	}
	
	@Test(enabled = true)
	public void testCreateUserPostNoData() throws Exception {
		String url = "users/lwolters" + count++;
		String data = "";
		ListenableFuture<Response> responseFuture = httpClient.preparePost(baseUrl + url).setBody(data).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.NO_CONTENT.getCode());
	}
	
	@Test(enabled = true)
	public void testCreateUserPostDataNotState() throws Exception {
		String url = "users/lwolters" + count++;
		String data = "{\"state\" : {\"id\" : 122}}";
		ListenableFuture<Response> responseFuture = httpClient.preparePost(baseUrl + url)
				.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
				.setBody(data).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.NOT_ACCEPTABLE.getCode());
	}
	
	@Test(enabled = true)
	public void testCreateUserPostDataState() throws Exception {
		String uid = "lwolters" + count++;
		String url = "users/" + uid;
		String data = "{\"username\" : \"" + uid + "\"}";
		ListenableFuture<Response> responseFuture = httpClient.preparePost(baseUrl + url)
				.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
				.setBody(data).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.OK.getCode());
		assertEquals(CONTENT_TYPE_JSON, response.getContentType());
		log.info(response.getResponseBody());
	}
	
	@Test(enabled = true)
	public void testDeleteNotExistingUserWithCheck() throws Exception {
		String url = "users/lwolters" + count++;
		ApiConfig.checkForExistenceBeforeDelete = true;
		ListenableFuture<Response> responseFuture = httpClient.prepareDelete(baseUrl + url).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.NOT_FOUND.getCode());
	}

	@Test(enabled = true)
	public void testDeleteNotExistingUserWithoutCheck() throws Exception {
		String url = "users/lwolters" + count++;
		ApiConfig.checkForExistenceBeforeDelete = false;
		ListenableFuture<Response> responseFuture = httpClient.prepareDelete(baseUrl + url).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.OK.getCode());
	}

	@Test(enabled = true)
	public void testDeleteExistingUser() throws Exception {
		String uid = "lwolters" + count++;
		String url = "users/" + uid;
		String data = "{\"username\" : \"" + uid + "\"}";
		ListenableFuture<Response> responseFuture = httpClient.preparePost(baseUrl + url)
				.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
				.setBody(data).execute();
		Response response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.OK.getCode());
		
		// now delete
		responseFuture = httpClient.prepareDelete(baseUrl + url).execute();
		response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.OK.getCode());
		
		// check if delete
		responseFuture = httpClient.prepareGet(baseUrl + url).execute();
		response = responseFuture.get();
		assertEquals(response.getStatusCode(), HttpResponseStatus.NOT_FOUND.getCode());
	}
}
