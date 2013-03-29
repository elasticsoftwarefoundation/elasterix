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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.ActorState;
import org.elasterix.elasticactors.ActorSystem;
import org.elasterix.elasticactors.ActorSystemBootstrapper;
import org.elasterix.elasticactors.ActorSystemConfiguration;
import org.elasterix.elasticactors.serialization.Deserializer;
import org.elasterix.elasticactors.serialization.MessageDeserializer;
import org.elasterix.elasticactors.serialization.MessageSerializer;
import org.elasterix.elasticactors.serialization.Serializer;
import org.elasterix.server.actors.Device;
import org.elasterix.server.actors.User;
import org.elasterix.server.serialization.JacksonActorRefDeserializer;
import org.elasterix.server.serialization.JacksonActorRefSerializer;
import org.elasterix.server.serialization.JacksonActorStateDeserializer;
import org.elasterix.server.serialization.JacksonActorStateSerializer;
import org.elasterix.server.serialization.JacksonMessageDeserializer;
import org.elasterix.server.serialization.JacksonMessageSerializer;
import org.elasterix.server.sip.SipMessageHandlerImpl;
import org.elasterix.sip.SipMessageHandler;
import org.elasterix.sip.SipMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Elasterix Implementation of the Elastic Actor Framework
 * <br>
 * 
 * @author Joost van de Wijgerd
 * @author Leonard Wolters
 */
public class ElasterixServer implements ActorSystemConfiguration, ActorSystemBootstrapper {
	private static final Logger log = Logger.getLogger(ElasterixServer.class);

	private final String name;
	private final int numberOfShards;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Serializer<ActorState, byte[]> actorStateSerializer = new JacksonActorStateSerializer(objectMapper);
	private final Deserializer<byte[], ActorState> actorStateDeserializer = new JacksonActorStateDeserializer(objectMapper);

	@Autowired
	private SipMessageSender sipMessageSender;

	/** Sip Message Handler needs to be manually created due to reference to Actor System */
	private SipMessageHandler sipMessageHandler;
	
	private final Map<Class<?>, MessageSerializer<?>> messageSerializers = new HashMap<Class<?>, MessageSerializer<?>>() {{
		put(User.class, new JacksonMessageSerializer<User>(objectMapper));
		put(Device.class, new JacksonMessageSerializer<Device>(objectMapper));
	}};

	private final Map<Class<?>, MessageDeserializer<?>> messageDeserializers = new HashMap<Class<?>, MessageDeserializer<?>>() {{
		put(User.class, new JacksonMessageDeserializer<User>(User.class, objectMapper));
		put(Device.class, new JacksonMessageDeserializer<Device>(Device.class, objectMapper));
	}};
	
	/**
	 * Default constructor
	 */
	public ElasterixServer() {
		this("ElasterixServer", 2);
	}
	public ElasterixServer(String name, int numberOfShards) {
		this.name = name;
		this.numberOfShards = numberOfShards;
	}

	@Override
	public void initialize(ActorSystem actorSystem) throws Exception {
		// register jackson module for Actor serialization
		objectMapper.registerModule(
				new SimpleModule("ElasterixModule",new Version(0,1,0,"SNAPSHOT"))
				.addSerializer(ActorRef.class, new JacksonActorRefSerializer())
				.addDeserializer(ActorRef.class, new JacksonActorRefDeserializer(
						actorSystem.getParent().getActorRefFactory())));
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
	public <T> MessageSerializer<T> getSerializer(Class<T> messageClass) {
		return (MessageSerializer<T>) messageSerializers.get(messageClass);
	}

	@Override
	public <T> MessageDeserializer<T> getDeserializer(Class<T> messageClass) {
		return (MessageDeserializer<T>) messageDeserializers.get(messageClass);
	}

	@Override
	public Serializer<ActorState, byte[]> getActorStateSerializer() {
		return actorStateSerializer;
	}

	@Override
	public Deserializer<byte[], ActorState> getActorStateDeserializer() {
		return actorStateDeserializer;
	}

	@Override
	public void create(ActorSystem actorSystem, String... strings) throws Exception {
		log.info(String.format("create. [%s]", StringUtils.arrayToCommaDelimitedString(strings)));
		// Construct message handler (we need it with an actorSystem attached
		sipMessageHandler = new SipMessageHandlerImpl(actorSystem);
	}

	@Override
	public void activate(ActorSystem actorSystem) throws Exception {
		log.info(String.format("activate."));
	}
}
