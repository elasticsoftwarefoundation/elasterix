package org.elasticsoftware.elasterix.server.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.biasedbit.efflux.logging.Logger;

/**
 * Ant based style <code>ApiMethod</code> matcher capable of matching registered
 * API Methods based on their url paths.
 * 
 * @author Leonard Wolters
 */
public class ApiMethodMatcher {
	private static final Logger log = Logger.getLogger(ApiMethodMatcher.class);
	private final ConcurrentMap<String, ApiMethod> methods = new ConcurrentHashMap<String,ApiMethod>();
	private final PathMatcher pathMatcher = new AntPathMatcher();
	
	/**
	 * Paths are considered 'relative'. For example, if the complete url is
	 * 'api.elasterix.org/api/2.0/users/lwolters/update the urlPath provided should match
	 * 'update'
	 * 
	 * @param urlPath
	 * @param method
	 */
	public void registerMethod(String urlPath, ApiMethod method) {
		if(log.isDebugEnabled()) {
			log.debug(String.format("registerMethod. UrlPath[%s]", urlPath));
		}
		methods.putIfAbsent(urlPath, method);
	}

	public ApiMethod getApiMethod(String urlPath) {

		// direct match?
		ApiMethod apiMethod = methods.get(urlPath);
		if(apiMethod != null) {
			return apiMethod;
		}

		// Pattern match?
		List<String> matchingPatterns = new ArrayList<String>();
		for (String registeredPattern : this.methods.keySet()) {
			if (pathMatcher.match(registeredPattern, urlPath)) {
				matchingPatterns.add(registeredPattern);
			}
		}
		String bestPatternMatch = null;
		Comparator<String> patternComparator = pathMatcher.getPatternComparator(urlPath);
		if (!matchingPatterns.isEmpty()) {
			Collections.sort(matchingPatterns, patternComparator);
			if (log.isDebugEnabled()) {
				log.debug("Matching patterns for request [" + urlPath + "] are " + matchingPatterns);
			}
			bestPatternMatch = matchingPatterns.get(0);
		}
		if (bestPatternMatch != null) {
			return methods.get(bestPatternMatch);
		}
		return null;
	}
}
