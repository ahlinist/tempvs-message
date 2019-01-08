package club.tempvs.message.util.impl;

import club.tempvs.message.dto.ErrorsDto;
import club.tempvs.message.util.ValidationHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ValidationHelperImpl implements ValidationHelper {

    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    public ErrorsDto getErrors() {
        return new ErrorsDto();
    }

    public void addError(ErrorsDto errorsDto, String field, String messageKey) {
        addError(errorsDto, field, messageKey, null);
    }

    public void addError(ErrorsDto errorsDto, String field, String messageKey, Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
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
