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

import org.elasterix.elasticactors.ActorState;
import org.elasterix.elasticactors.ActorSystem;
import org.elasterix.elasticactors.ActorSystemBootstrapper;
import org.elasterix.elasticactors.ActorSystemConfiguration;
import org.elasterix.elasticactors.serialization.Deserializer;
import org.elasterix.elasticactors.serialization.MessageDeserializer;
import org.elasterix.elasticactors.serialization.MessageSerializer;
import org.elasterix.elasticactors.serialization.Serializer;

/**
 * @author Joost van de Wijgerd
 * @author Leonard Wolters
 */
public class ElasterixServer implements ActorSystemConfiguration, ActorSystemBootstrapper {
	
    @Override
    public void initialize(ActorSystem actorSystem) throws Exception {
        // bootstrap here
    }

    @Override
    public void create(ActorSystem actorSystem, String... strings) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void activate(ActorSystem actorSystem) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return "ElasterixServer";
    }

    @Override
    public int getNumberOfShards() {
        return 0; 
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public <T> MessageSerializer<T> getSerializer(Class<T> tClass) {
        return null;  
    }

    @Override
    public <T> MessageDeserializer<T> getDeserializer(Class<T> tClass) {
        return null;  
    }

    @Override
    public Serializer<ActorState, byte[]> getActorStateSerializer() {
        return null;  
    }

    @Override
    public Deserializer<byte[], ActorState> getActorStateDeserializer() {
        return null; 
    }
}
