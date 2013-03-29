package org.elasterix.server.serialization;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasterix.elasticactors.ActorState;
import org.elasterix.elasticactors.serialization.Serializer;

import java.io.IOException;

/**
 * @author Leonard Wolters
 */
public final class JacksonActorStateSerializer implements Serializer<ActorState, byte[]> {
    private final ObjectMapper objectMapper;

    public JacksonActorStateSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(ActorState object) throws IOException {
        return objectMapper.writeValueAsBytes(object.getAsMap());
    }
}
