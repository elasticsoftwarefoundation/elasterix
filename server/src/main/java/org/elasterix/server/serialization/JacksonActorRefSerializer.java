package org.elasterix.server.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.elasterix.elasticactors.ActorRef;

/**
 * @author Leonard Wolters
 */
public final class JacksonActorRefSerializer extends JsonSerializer<ActorRef> {
    @Override
    public void serialize(ActorRef value, JsonGenerator jgen, SerializerProvider provider) 
    throws IOException, JsonProcessingException {
        jgen.writeString(value.toString());
    }
}
