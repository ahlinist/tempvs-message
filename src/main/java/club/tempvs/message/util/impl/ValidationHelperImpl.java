package club.tempvs.message.util.impl;

import club.tempvs.message.dto.ErrorsDto;
import club.tempvs.message.util.ValidationHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ValidationHelperImpl implements ValidationHelper {

    private MessageSource messageSource;
    private ObjectMapper objectMapper;

    @Autowired
    public ValidationHelperImpl(MessageSource messageSource, ObjectMapper objectMapper) {
        this.messageSource = messageSource;
        this.objectMapper = objectMapper;
    }

    public ErrorsDto getErrors() {
        return new ErrorsDto();
    }

    public void addError(ErrorsDto errorsDto, String field, String messageKey, Object[] args, Locale locale) {
        String value = messageSource.getMessage(messageKey, args, messageKey, locale);
        errorsDto.addError(field, value);
    }

    public void processErrors(ErrorsDto errorsDto) {
        if (!errorsDto.getErrors().isEmpty()) {
            try {
                throw new IllegalArgumentException(objectMapper.writeValueAsString(errorsDto));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
