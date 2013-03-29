package org.elasterix.server.serialization;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasterix.elasticactors.serialization.MessageSerializer;

/**
 * @author Leonard Wolters
 */
public final class JacksonMessageSerializer<T> implements MessageSerializer<T> {
    private final ObjectMapper objectMapper;

    public JacksonMessageSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ByteBuffer serialize(T object) throws IOException {
        return ByteBuffer.wrap(objectMapper.writeValueAsBytes(object));
    }
}
