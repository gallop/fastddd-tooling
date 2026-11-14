package ${packageName}.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ${msg.proj.tokenResponse.classDesc}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /**
     * ${msg.proj.tokenResponse.accessTokenLabel}
     */
    private String accessToken;

    /**
     * ${msg.proj.tokenResponse.refreshTokenLabel}
     */
    private String refreshToken;

    /**
     * ${msg.proj.tokenResponse.tokenTypeLabel}
     */
    private String tokenType;

    /**
     * ${msg.proj.tokenResponse.expiresInLabel}
     */
    private Long expiresIn;
}
