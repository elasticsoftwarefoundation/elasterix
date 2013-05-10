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

package org.elasticsoftware.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.ActorSystem;
import org.elasticsoftware.elasticactors.test.TestActorSystem;
import org.elasticsoftware.server.actors.User;
import org.elasticsoftware.server.actors.UserAgentClient;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipRequest;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

/**
 * @author Leonard Wolters
 */
public abstract class AbstractSipTest {
    private TestActorSystem testActorSystem;
	protected ActorSystem actorSystem;
	protected SipClient sipClient;
	protected Md5PasswordEncoder md5Encoder = new Md5PasswordEncoder();
	
	protected List<ActorRef> users = new ArrayList<ActorRef>();
	protected List<ActorRef> uacs = new ArrayList<ActorRef>();

	@BeforeTest
	public void init() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();

		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.elasticsoftware").setLevel(Level.DEBUG);

        testActorSystem = TestActorSystem.create();
		actorSystem = testActorSystem.create(new ElasterixServer());
		sipClient = new SipClient();

		// create two users
		User.State state = new User.State("leonard@elasticsoftware.org", "lwolters", 
				md5Encoder.encodePassword("test", null));
		ActorRef ref = actorSystem.actorOf("user/lwolters", User.class, state);
		users.add(ref);
		ref = actorSystem.actorOf("user/jwijgerd", User.class, 
				new User.State("joost@elasticsoftware.org", "jwijgerd", 
						md5Encoder.encodePassword("test", null)));
		users.add(ref);
		
		// create UAC
		UserAgentClient.State uacState = new UserAgentClient.State("device1");
		ref = actorSystem.actorOf("uac/device1", UserAgentClient.class, uacState);
		uacs.add(ref);

		// wait some time for actors to be created
		Thread.sleep(300);
	}

	@AfterTest
	public void destroy() throws Exception {
//		for(ActorRef ref : users) {
//			actorSystem.stop(ref);
//		}
//		for(ActorRef ref : uacs) {
//			actorSystem.stop(ref);
//		}
//		if(sipClient != null) {
//			sipClient.close();
//		}
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
	
	protected void setAuthorization(SipRequest message, String userName, String nonce,
			String hash) {
		// Authorization: Digest username="124",realm="combird",nonce="24855234",
		// uri="sip:sip.outerteams.com:5060",response="749c35e9fe30d6ba46cc801bdfe535a0",algorithm=MD5
		message.addHeader(SipHeader.AUTHORIZATION, String.format("Digest username=\"%s\",realm=\"elasticsoftware\""
				+ ",nonce=\"%s\",uri=\"%s\",response=\"%s\",algorithm=MD5", userName, nonce, "", hash));
	}
}
