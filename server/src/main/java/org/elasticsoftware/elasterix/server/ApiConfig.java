package org.elasticsoftware.elasterix.server;

/**
 * Static Server Configuration 
 * 
 * @author Leonard Wolters
 */
public class ApiConfig {
	public static boolean checkForExistenceBeforeDelete = true;
	
	public static boolean checkForExistenceBeforeDelete() {
		return checkForExistenceBeforeDelete;
	}
}
