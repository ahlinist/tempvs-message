package club.tempvs.message.model;

import static java.util.Objects.nonNull;

import club.tempvs.message.dto.UserInfoDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Locale;

@Data
@NoArgsConstructor
public class User {

    private Long profileId;
    private Long userProfileId;
    private String userName;
    private String timezone;
    private Locale locale;

    public User(UserInfoDto userInfoDto) {
        this.profileId = userInfoDto.getProfileId();
        this.userProfileId = userInfoDto.getUserProfileId();
        this.userName = userInfoDto.getUserName();
        this.timezone = userInfoDto.getTimezone();
        this.timezone = nonNull(userInfoDto.getTimezone()) ? userInfoDto.getTimezone() : "UTC";
        this.locale = new Locale(userInfoDto.getLang());
    }
}
