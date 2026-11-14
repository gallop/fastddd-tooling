package ${packageName}.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ${msg.proj.userInfoResponse.classDesc}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /**
     * ${msg.proj.userInfoResponse.userLabel}
     */
    private UserInfo user;

    /**
     * ${msg.proj.userInfoResponse.permissionsLabel}
     */
    private List<String> permissions;

    /**
     * ${msg.proj.userInfoResponse.rolesLabel}
     */
    private List<String> roles;
}
