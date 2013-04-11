/*
 * Copyright 2013 Joost van de Wijgerd, Leonard Wolters
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

import org.apache.log4j.Logger;
import org.elasterix.elasticactors.ActorSystem;
import org.elasterix.server.sip.SipService;
import org.elasticsoftwarefoundation.elasticactors.base.SpringBasedActorSystem;
import org.springframework.context.ApplicationContext;

/**
 * Elasterix Implementation of the Elastic Actor Framework
 * <br>
 * 
 * @author Joost van de Wijgerd
 * @author Leonard Wolters
 */
public class ElasterixServer extends SpringBasedActorSystem {
	private static final Logger log = Logger.getLogger(ElasterixServer.class);

	private final String name;
	private final int numberOfShards;

	/**
	 * Default constructor
	 */
	public ElasterixServer() {
		this("ElasterixServer", 2);
	}

	public ElasterixServer(String name, int numberOfShards) {
        super("elasterix-server-beans.xml");
		this.name = name;
		this.numberOfShards = numberOfShards;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getNumberOfShards() {
		return numberOfShards;
	}

	@Override
	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
	}

    @Override
    protected void doInitialize(ApplicationContext applicationContext, ActorSystem actorSystem) {
        SipService sipService = applicationContext.getBean(SipService.class);
        sipService.setActorSystem(actorSystem);
    }

    @Override
	public void create(ActorSystem actorSystem, String... strings) throws Exception {

	}

	@Override
	public void activate(ActorSystem actorSystem) throws Exception {
		log.info(String.format("activate."));
	}

}
