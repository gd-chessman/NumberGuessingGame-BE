package org.example.numberguessinggame.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.validation.FieldError;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {

    private final String message;
    private final Map<String, String> errors;

    public ErrorMessage(String message, List<FieldError> fieldErrors) {
        this.message = message;
        this.errors = new LinkedHashMap<>();
        for (FieldError fe : fieldErrors) {
            this.errors.put(fe.getField(), fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid");
        }
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
