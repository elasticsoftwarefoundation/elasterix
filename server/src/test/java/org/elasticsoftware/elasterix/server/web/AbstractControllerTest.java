/*
 * Copyright 2013 Joost van de Wijgerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsoftware.elasterix.server.web;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.elasticsoftware.elasterix.server.ElasterixServer;
import org.elasticsoftware.elasticactors.ActorSystem;
import org.elasticsoftware.elasticactors.http.HttpActorSystem;
import org.elasticsoftware.elasticactors.test.TestActorSystem;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import com.ning.http.client.AsyncHttpClient;

/**
 * @author Leonard Wolters
 */
public abstract class AbstractControllerTest {
	protected static final long SLEEP = 400;
	protected static final String CONTENT_TYPE_JSON = "application/json";
	private TestActorSystem testActorSystem;
	protected AsyncHttpClient httpClient;
	protected String baseUrl = "http://localhost:8080/api/2.0/";
	
	@BeforeTest
	public void init() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();

		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.elasticsoftware").setLevel(Level.DEBUG);
		Logger.getLogger("org.elasticsoftware.sip").setLevel(Level.INFO);
		
		testActorSystem = TestActorSystem.create();
		ActorSystem httpSystem = testActorSystem.create(new HttpActorSystem());
	    ActorSystem elasterixSystem = testActorSystem.create(new ElasterixServer());
		
		httpClient = new AsyncHttpClient();

		// wait some time for actors to be created
		Thread.sleep(SLEEP);
	}
	
	@AfterTest
	public void destroy() throws Exception {
        testActorSystem.destroy();
	}
	
	protected boolean startsWith(String input, String startsWith) {
		input = input.trim();
		startsWith = startsWith.trim();
		
		if(input.length() < startsWith.length()) {
			System.err.println(String.format("Length input to short. input[%d] != startsWith[%d]", 
					input.length(), startsWith.length()));
			return false;
		}
		for(int i = 0; i < input.length(); i++) {
			if(input.charAt(i) != startsWith.charAt(i)) {
				System.err.println(String.format("Characters differ. Index[%d]. \n%s\n======\n%s", 
						i, input.subSequence(0, i), startsWith.subSequence(0, i)));
				return false;
			}
		}
		return true;
	}
}
