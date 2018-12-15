package club.tempvs.message.dto;

import java.util.HashMap;
import java.util.Map;

public class ErrorsDto {

    Map<String, String> errors = new HashMap<>();

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public void addError(String field, String message) {
        String value = this.errors.get(field);
        this.errors.put(field, (value == null) ? message : value + "\n" + message);
    }
}
