package org.elasticsoftware.elasterix.server.messages;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.elasticsoftware.elasticactors.http.messages.HttpRequest;
import org.elasticsoftware.elasticactors.http.messages.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.util.StringUtils;

import com.biasedbit.efflux.logging.Logger;

/**
 * @author Leonard Wolters
 */
public class ApiHttpMessage {
	private static final Logger log = Logger.getLogger(ApiHttpMessage.class);

	private String version;
	private String actorId;
	private String domain;
	private HttpRequest httpRequest;
	private String action;

	private ObjectMapper objectMapper; 
	
	@JsonCreator
	protected ApiHttpMessage( @JsonProperty("httpRequest") HttpRequest httpRequest,
			@JsonProperty("version") String version,
			@JsonProperty("domain") String domain, 
			@JsonProperty("actorId") String actorId,
			@JsonProperty("action") String action) {
		this.httpRequest = httpRequest;
		this.actorId = actorId;
		this.domain = domain;
		this.version = version;
		this.action = action;
		
		// init object mapper
		objectMapper = new ObjectMapper();
		//objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector());
		objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
	}

	@JsonProperty("version")
	public String getVersion() {
		return version;
	}

	@JsonProperty("domain")
	public String getDomain() {
		return domain;
	}

	@JsonProperty("actorId")
	public String getActorId() {
		return actorId;
	}

	@JsonProperty("action")
	public String getAction() {
		return action;
	}

	@JsonProperty("httpRequest")
	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	@JsonIgnore
	public JsonNode getContent() {
		return getContent(JsonNode.class);
	} 

	@JsonIgnore
	public <T> T getContent(Class<T> clazz) {
		if(httpRequest.getContent() == null) {
			if(log.isDebugEnabled()) {
				log.debug(String.format("No content. Content type[%s]", httpRequest.getContentType()));
			}
			return null;
		}
		if("application/json".equalsIgnoreCase(httpRequest.getContentType())) {
			try {
				return (T) objectMapper.readValue(httpRequest.getContent(), clazz); 
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		} else {
			log.warn(String.format("Not json content[%s]", httpRequest.getContentType()));
		}
		return (T) null;
	}

	public static ApiHttpMessage parse(HttpRequest request) {

		String url = request.getUrl();
		if(url.startsWith("/")) url = url.substring(1);
		String[] tokens = url.split("/");
		if(log.isDebugEnabled()) {
			log.debug("Tokens: " + StringUtils.arrayToCommaDelimitedString(tokens));
		}
		if(tokens.length < 4) {
			log.warn(String.format("Invalid url path[%s]. Not of type '/api/*/users/....'", url));
			return null;
		}

		String action = (tokens.length > 4 ? tokens[4] : null);
		return new ApiHttpMessage(request, tokens[1], tokens[2], tokens[3], action);
	}

	public HttpResponse toHttpResponse(HttpResponseStatus status, Object jsonObject) {
		
		// headers
		Map<String,List<String>> headers = new HashMap<String,List<String>>();
		headers.put(HttpHeaders.Names.CONTENT_TYPE, Arrays.asList("application/json"));

		// response
		byte[] response = status.getReasonPhrase().getBytes(Charset.forName("UTF-8"));
		if(jsonObject != null) {
			try {
				response = objectMapper.writeValueAsBytes(jsonObject);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
		return new HttpResponse(status.getCode(), headers, response);
	}
}