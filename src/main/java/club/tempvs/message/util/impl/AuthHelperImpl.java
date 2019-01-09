package club.tempvs.message.util.impl;

import club.tempvs.message.api.UnauthorizedException;
import club.tempvs.message.util.AuthHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class AuthHelperImpl implements AuthHelper {

    private static final String CHAR_ENCODING = "UTF-8";

    @Value("${authorization.token}")
    private String token;

    public void authenticate(String receivedToken) {
        byte[] tokenBytes;

        try {
            tokenBytes = token.getBytes(CHAR_ENCODING);
        } catch (Exception e) {
            tokenBytes = new byte[]{};
        }

        String tokenHash = DigestUtils.md5DigestAsHex(tokenBytes);

        if (receivedToken == null || !receivedToken.equals(tokenHash)) {
            throw new UnauthorizedException("Authentication failed. Wrong token is received.");
        }
    }
}
