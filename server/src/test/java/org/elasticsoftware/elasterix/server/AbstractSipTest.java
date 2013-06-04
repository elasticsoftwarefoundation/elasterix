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

package org.elasticsoftware.elasterix.server;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.elasticsoftware.elasterix.server.actors.User;
import org.elasticsoftware.elasterix.server.actors.UserAgentClient;
import org.elasticsoftware.elasticactors.ActorSystem;
import org.elasticsoftware.elasticactors.test.TestActorSystem;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipUser;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.util.StringUtils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

/**
 * @author Leonard Wolters
 */
public abstract class AbstractSipTest {
	protected static final long SLEEP = 400;
    private TestActorSystem testActorSystem;
	protected ActorSystem actorSystem;
	protected SipClient sipServer;
	protected SipClient localSipServer;
	protected Md5PasswordEncoder encoder = new Md5PasswordEncoder();
	
	@BeforeTest
	public void init() throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();

		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.elasticsoftware.elasterix").setLevel(Level.DEBUG);
		Logger.getLogger("org.elasticsoftware.sip").setLevel(Level.INFO);

        testActorSystem = TestActorSystem.create();
		actorSystem = testActorSystem.create(new ElasterixServer());

		sipServer = new SipClient("localhost", 5060);
		localSipServer = new SipClient(9090);
		
		addUser("lwolters");
		addUser("jwijgerd");
		
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
	
	protected void setAuthorization(SipRequest message, String username, String nonce, String password) {

		String realm = ServerConfig.getRealm();
		String uri = "sip:sip.localhost.com:5060";
		
		// create hash 
		String ha1 = String.format("%s:%s:%s", username, realm, password);
		String ha2 = String.format("%s:%s", "REGISTER", uri);
		String hash = encoder.encodePassword(String.format("%s:%s:%s", 
				encoder.encodePassword(ha1, null), nonce, 
				encoder.encodePassword(ha2, null)), null);
		
		// Authorization: Digest username="124",realm="combird",nonce="24855234",
		// uri="sip:sip.outerteams.com:5060",response="749c35e9fe30d6ba46cc801bdfe535a0",algorithm=MD5
		message.setHeader(SipHeader.AUTHORIZATION, String.format("Digest username=\"%s\",realm=\"%s\""
				+ ",nonce=\"%s\",uri=\"%s\",response=\"%s\",algorithm=MD5", username, 
				realm, nonce, uri, hash));
	}
	protected void setAuthorization(SipRequest message, String password) {
		Map<String, String> map = tokenize(message.getHeaderValue(SipHeader.AUTHORIZATION));
		setAuthorization(message, map.get("username"), map.get("nonce"), password);
	}
	private Map<String, String> tokenize(String value) {
		Map<String, String> map = new HashMap<String, String>();
		
		// sanity check
		if(StringUtils.isEmpty(value)) {
			return map;
		}
		
		// Authorization: Digest username="124",realm="elasticsoftware",nonce="24855234",
		// uri="sip:sip.outerteams.com:5060",response="749c35e9fe30d6ba46cc801bdfe535a0",algorithm=MD5
		StringTokenizer st = new StringTokenizer(value, " ,", false);
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			int idx = token.indexOf("=");
			if(idx != -1) {
				map.put(token.substring(0, idx).toLowerCase(), 
						token.substring(idx+1).replace('\"', ' ').trim());
			} else {
				map.put(token.toLowerCase(), token);
			}
		}
		return map;
	}
	
	protected SipClient getSipClient() {
		return new SipClient("localhost", 5060);
	}
	
	protected void addUser(String userId) throws Exception {
		addUser(userId, null);
	}
	protected void addUser(String userId, SipUser user) throws Exception {
		User.State state = new User.State(userId + "@elasticsoftware.org", userId, "test");
		if(user != null) {
			state.addUserAgentClient(user, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10));
		}
		actorSystem.actorOf("user/" + userId, User.class, state);
	}
	protected SipUser createSipUser(String username, String domain, int port) {
		return new SipUser(String.format("\"Unknown\"<sip:%s@%s:%d>", 
				username, domain, port));
	}
	protected void addUserAgentClient(SipUser user) throws Exception {
		UserAgentClient.State state = new UserAgentClient.State(user.getUsername(), 
				user.getDomain(), user.getPort());
		actorSystem.actorOf(String.format("uac/%s_%s_%d", user.getUsername(), user.getDomain(), 
				user.getPort()), UserAgentClient.class, state);		
	}
}
