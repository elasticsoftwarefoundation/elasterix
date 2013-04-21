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

package org.elasterix.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.ActorSystem;
import org.elasterix.elasticactors.test.TestActorSystem;
import org.elasterix.server.actors.User;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test that creates a test actor system and loads the ElasterixServer<br>
 * 
 * @author Joost van de Wijgerd
 * @author Leonard Wolters
 */
public class ElasterixSipServerTest {
	private static final Logger log = Logger.getLogger(ElasterixSipServerTest.class);
	protected ActorSystem actorSystem;
	protected SipClient sipClient;
	
	protected List<ActorRef> users = new ArrayList<ActorRef>();
	protected List<ActorRef> uacs = new ArrayList<ActorRef>();

	@BeforeMethod
	public void init() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		
		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.elasterix.sip").setLevel(Level.INFO);
		Logger.getLogger("org.elasterix").setLevel(Level.DEBUG);

		actorSystem = TestActorSystem.create(new ElasterixServer());
		sipClient = new SipClient();
		
		// create a couple of users
		users.add(actorSystem.actorOf("user/124", User.class));
	}
	
	@AfterMethod
	protected void destroy() throws Exception {
		for(ActorRef ref : users) {
			actorSystem.stop(ref);
		}
		for(ActorRef ref : uacs) {
			actorSystem.stop(ref);
		}
	}

	@Test(enabled = true)
	public void testRegisterNonExistingUser() throws Exception {
		// send a sip register message to the Sip Server
		// see http://tools.ietf.org/html/rfc3261#section-10.2
		
//		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
//		req.addHeader(SipHeader.MAX_FORWARDS, "70");
//		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
//		req.addHeader(SipHeader.CALL_ID, "a84b4c76e66710");
//		req.addHeader(SipHeader.CSEQ, "314159 REGISTER");
//		req.addHeader(SipHeader.FROM, "Leonard Wolters <sip:leonard@localhost.com>");
//		req.addHeader(SipHeader.TO, "Leonard Wolters <sip:leonard@localhost.com>;tag=1928301774");

		String response = sipClient.sendMessage("sip-register.txt");
		log.info("RESPONSE: " + response);
	}
}

