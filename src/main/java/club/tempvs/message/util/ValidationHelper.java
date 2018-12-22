package club.tempvs.message.util;

import club.tempvs.message.dto.ErrorsDto;

public interface ValidationHelper {
    ErrorsDto getErrors();
    void addError(ErrorsDto errorsDto, String field, String messageKey);
    void addError(ErrorsDto errorsDto, String field, String messageKey, Object[] args);
    void processErrors(ErrorsDto errorsDto);
}
