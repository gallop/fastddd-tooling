package ${packageName}.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ${msg.proj.loginResponse.classDesc}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * ${msg.proj.loginResponse.accessTokenLabel}
     */
    private String accessToken;

    /**
     * ${msg.proj.loginResponse.refreshTokenLabel}
     */
    private String refreshToken;

    /**
     * ${msg.proj.loginResponse.tokenTypeLabel}
     */
    private String tokenType;

    /**
     * ${msg.proj.loginResponse.expiresInLabel}
     */
    private Long expiresIn;

    /**
     * ${msg.proj.loginResponse.userLabel}
     */
    private UserInfo user;

    /**
     * ${msg.proj.loginResponse.permissionsLabel}
     */
    private List<String> permissions;
}
