package ${packageName}.security.strategy;

import com.gallop.fastddd.security.spi.SecurityIdentityService;
import com.gallop.fastddd.security.spi.SecurityUserDetails;
import com.gallop.fastddd.security.strategy.AuthStrategy;
import com.gallop.fastddd.security.strategy.AuthenticationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * ${msg.proj.security.mc.wechat.strategyDesc}
 *
 * <p>${msg.proj.security.mc.wechat.usageNote}</p>
 *
 * <p>${msg.proj.security.mc.wechat.flowNote}</p>
 *
 * @author FastDDD
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatMiniAuthStrategy implements AuthStrategy {

    private final SecurityIdentityService identityService;

    @Value("${r"${fastddd.security.wechat-mini.app-id:}"}")
    private String appId;

    @Value("${r"${fastddd.security.wechat-mini.secret:}"}")
    private String appSecret;

    @Value("${r"${fastddd.security.wechat-mini.dev-mode:false}"}")
    private boolean devMode;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String JSCODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    @Override
    public String getType() {
        return "wechat-mini";
    }

    @Override
    public Authentication authenticate(AuthenticationRequest request) {
        String code = request.getExtraParam("wechatCode");

        if (code == null || code.isBlank()) {
            throw new AuthenticationServiceException("${msg.proj.security.mc.wechat.authCodeLabel}${msg.common.validation.notBlank}");
        }

        // 1. ${msg.proj.security.mc.wechat.step1CallApi}
        String openid = getOpenidByCode(code);
        if (openid == null || openid.isBlank()) {
            throw new BadCredentialsException("${msg.proj.security.mc.wechat.authFailedNoOpenid}");
        }

        log.info("${msg.proj.security.mc.wechat.logAuthenticated}: openid={}", openid);

        // 2. ${msg.proj.security.mc.wechat.step2FindUser}
        // TODO: ${msg.proj.security.mc.wechat.todoOpenidField}
        // ${msg.proj.security.mc.wechat.fallbackNote}
        SecurityUserDetails userDetails = identityService.loadUserByUsername("wx_" + openid);

        if (userDetails == null) {
            // TODO: ${msg.proj.security.mc.wechat.todoAutoRegister}
            throw new BadCredentialsException("${msg.proj.security.mc.wechat.notBoundHint}");
        }

        if (!userDetails.isEnabled()) {
            throw new AuthenticationServiceException("${msg.proj.security.mc.wechat.accountDisabled}");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    /**
     * ${msg.proj.security.mc.wechat.getOpenidDesc}
     */
    private String getOpenidByCode(String code) {
        if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) {
            if (!devMode) {
                log.error("${msg.proj.security.mc.wechat.configMissingStrict}");
                return null;
            }
            log.warn("${msg.proj.security.mc.wechat.configMissingDev}");
            return "dev_openid_" + code;
        }

        try {
            String encodedAppId = URLEncoder.encode(appId, StandardCharsets.UTF_8);
            String encodedSecret = URLEncoder.encode(appSecret, StandardCharsets.UTF_8);
            String encodedCode = URLEncoder.encode(code, StandardCharsets.UTF_8);
            String url = String.format(JSCODE2SESSION_URL, encodedAppId, encodedSecret, encodedCode);
            String response = restTemplate.getForObject(url, String.class);

            JsonNode json = objectMapper.readTree(response);
            if (json.has("openid")) {
                return json.get("openid").asText();
            }

            log.error("${msg.proj.security.mc.wechat.apiError}: {}", response);
            return null;
        } catch (Exception e) {
            log.error("${msg.proj.security.mc.wechat.jscodeCallFailed}", e);
            return null;
        }
    }
}
