package org.elasterix.server.serialization;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasterix.elasticactors.ActorState;

/**
 * @author Leonard Wolters
 */
public final class JacksonActorState implements ActorState {
    private final ObjectMapper objectMapper;
    private final Map<String, Object> stateMap;
    private volatile Object stateObject;

    public JacksonActorState(ObjectMapper objectMapper, Map<String, Object> stateMap) {
        this.objectMapper = objectMapper;
        this.stateMap = stateMap;
    }

    public JacksonActorState(ObjectMapper objectMapper, Object stateObject) {
        this.objectMapper = objectMapper;
        this.stateObject = stateObject;
        this.stateMap = null;
    }

    @Override
    public Map<String, Object> getAsMap() {
        if(stateObject != null) {
            return objectMapper.convertValue(stateObject,JacksonActorStateDeserializer.MAP_TYPE);
        }
        return stateMap;
    }

    @Override
    public <T> T getAsObject(Class<T> objectClass) {
        if (stateObject == null) {
            stateObject = objectMapper.convertValue(stateMap, objectClass);
        }
        return (T) stateObject;
    }
}
