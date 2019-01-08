package club.tempvs.message.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ErrorsDto {

    Map<String, String> errors = new HashMap<>();

    public void addError(String field, String message) {
        String value = this.errors.get(field);
        this.errors.put(field, (value == null) ? message : value + "\n" + message);
    }
}
