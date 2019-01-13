package club.tempvs.message.converter;

import club.tempvs.message.dto.UserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInfoDtoConverter implements Converter<String, UserInfoDto> {

    private final ObjectMapper objectMapper;

    @Override
    public UserInfoDto convert(final String userInfoDtoJson) {
        try {
            return objectMapper.readValue(userInfoDtoJson, UserInfoDto.class);
        } catch (Exception e) {
            return null;
        }
    }
}
