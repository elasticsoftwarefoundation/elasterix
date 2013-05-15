package org.elasticsoftware.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Static Server Configuration 
 * 
 * @author Leonard Wolters
 */
public class ServerConfig {
	/** Example:  Mon, 13 May 2013 18:16:52 GMT */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE d MMM YYYY HH:mm:ss Z");

	public static String getRealm() {
		return "elastic-software";
	}
	
	public static String getServerName() {
		return "server1";
	}
	
	public static String getSupported() {
		return "replaces, timer";
	}
	
	public static String getAllow() {
		return "INVITE, ACK, CANCEL, OPTIONS, BYE, "
				+ "REFER, SUBSCRIBE, NOTIFY, INFO, PUBLISH";
	}
	
	public static String getDateNow() {
		return DATE_FORMAT.format(new Date());
	}
}
