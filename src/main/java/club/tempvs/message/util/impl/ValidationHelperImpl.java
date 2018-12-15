package club.tempvs.message.util.impl;

import club.tempvs.message.dto.ErrorsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ValidationHelperImpl {

    private MessageSource messageSource;

    @Autowired
    public ValidationHelperImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public ErrorsDto getErrors() {
        return new ErrorsDto();
    }

    public void addError(ErrorsDto errorsDto, String field, String messageKey, Object[] args, Locale locale) {
        String value = messageSource.getMessage(messageKey, args, messageKey, locale);
        errorsDto.addError(field, value);
    }
}
