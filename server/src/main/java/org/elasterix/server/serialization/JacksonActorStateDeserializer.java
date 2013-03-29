package org.elasterix.server.serialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.SimpleType;
import org.elasterix.elasticactors.ActorState;
import org.elasterix.elasticactors.serialization.Deserializer;

/**
 * @author Leonard Wolters
 */
public final class JacksonActorStateDeserializer implements Deserializer<byte[], ActorState> {
    private final ObjectMapper objectMapper;
    public static final MapType MAP_TYPE = MapType.construct(HashMap.class, 
    		SimpleType.construct(String.class), SimpleType.construct(Object.class));

    public JacksonActorStateDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ActorState deserialize(byte[] serializedObject) throws IOException {
        Map<String,Object> jsonMap = objectMapper.readValue(serializedObject, MAP_TYPE);
        return new JacksonActorState(objectMapper,jsonMap);
    }
}
