

package org.shopping.common.components.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shopping.common.components.exception.ApplicationException;

public class JsonUtility {

    private static final JsonUtility INSTANCE = new JsonUtility();

    private ObjectMapper mapper = new ObjectMapper();

    private JsonUtility() {
        this.mapper.setSerializationInclusion(Include.NON_NULL);
    }

    public static JsonUtility getInstance() {
        return INSTANCE;
    }

    public <T> T getObject(String jsonString, Class<T> objectClass) {
        try {
            return this.mapper.readValue(jsonString, objectClass);
        } catch (Exception var4) {
            throw new ApplicationException(1002, "Error mapping string to class, caused by " + var4.getMessage(), var4);
        }
    }

    public String getString(Object object) {
        return this.writeValueAsString(this.mapper, object);
    }

    private String writeValueAsString(ObjectMapper mapper, Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception var4) {
            throw new ApplicationException(1002, "Error writing object as snake case string, caused by " + var4.getMessage(), var4);
        }
    }
}
