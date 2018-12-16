package club.tempvs.message.util;

import club.tempvs.message.dto.ErrorsDto;

import java.util.Locale;

public interface ValidationHelper {
    ErrorsDto getErrors();
    void addError(ErrorsDto errorsDto, String field, String messageKey, Object[] args, Locale locale);
    void processErrors(ErrorsDto errorsDto);
}
