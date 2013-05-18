package org.elasticsoftware.server.messages;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.SipUser;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Leonard Wolters
 */
public class SipUserTest {
	private static final Logger log = Logger.getLogger(SipUserTest.class);
	
	@BeforeTest
	public void init() {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();

		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.elasticsoftware").setLevel(Level.DEBUG);
	}

	@Test
	public void testSipUserContact() throws Exception {
		String value = "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>";
		SipUser user = new SipUser(value);
		Assert.assertNotNull(user);
		log.info(user);
		Assert.assertTrue(StringUtils.isEmpty(user.getDisplayName()));
		Assert.assertEquals("124", user.getUsername());
		Assert.assertEquals("62.163.143.30", user.getDomain());
		Assert.assertEquals(60236, user.getPort());
	}

	@Test
	public void testSipUserTo() throws Exception {
		String value = "\"Hans de Borst\"<sip:124@sip.outerteams.com:5060>";
		SipUser user = new SipUser(value);
		Assert.assertNotNull(user);
		log.info(user);
		Assert.assertEquals("Hans de Borst", user.getDisplayName());
		Assert.assertEquals("124", user.getUsername());
		Assert.assertEquals("sip.outerteams.com", user.getDomain());
		Assert.assertEquals(5060, user.getPort());
	}
}
