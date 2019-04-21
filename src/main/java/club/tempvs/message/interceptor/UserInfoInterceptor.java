package club.tempvs.message.interceptor;

import club.tempvs.message.dto.UserInfoDto;
import club.tempvs.message.holder.UserHolder;
import club.tempvs.message.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class UserInfoInterceptor implements HandlerInterceptor {

    private static final String USER_INFO_HEADER = "User-Info";

    private final ObjectMapper objectMapper;
    private final UserHolder userHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userInfoHeaderValue = request.getHeader(USER_INFO_HEADER);
        response.setHeader(USER_INFO_HEADER, userInfoHeaderValue);

        try {
            UserInfoDto userInfoDto = objectMapper.readValue(userInfoHeaderValue, UserInfoDto.class);
            User user = new User(userInfoDto);
            userHolder.setUser(user);
            LocaleContextHolder.setLocale(user.getLocale());
        } catch (Exception e) {
            //do nothing
        }

        return true;
    }
}
