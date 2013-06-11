package org.elasticsoftware.elasterix.server;

import org.testng.annotations.Test;

/**
 * @author Joost van de Wijgerd
 */
public class ServerConfigTest {

    @Test
    public void testDateFormat() {
        ServerConfig.getDateNow();
    }
}
