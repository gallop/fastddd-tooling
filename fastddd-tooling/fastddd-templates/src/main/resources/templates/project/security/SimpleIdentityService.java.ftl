package ${packageName}.security;

import com.gallop.fastddd.security.spi.SecurityUserDetails;
import com.gallop.fastddd.security.spi.SecurityIdentityService;
import com.gallop.fastddd.security.spi.SecurityUserInfo;
import org.springframework.stereotype.Component;

/**
 * SecurityIdentityService ${msg.proj.security.simpleidentityservice.implSuffix}
 *
 * TODO: ${msg.proj.security.simpleidentityservice.replaceHint}
 * ${msg.proj.security.simpleidentityservice.studioHint}
 */
@Component
public class AppIdentityService implements SecurityIdentityService {

    @Override
    public SecurityUserDetails loadUserByUsername(String username) {
        // TODO: ${msg.proj.security.simpleidentityservice.todoLoadUser}
        throw new UnsupportedOperationException("${msg.proj.security.simpleidentityservice.implRequiredByUsername}");
    }

    @Override
    public SecurityUserInfo getUserInfo(String userId) {
        // TODO: ${msg.proj.security.simpleidentityservice.todoGetUserInfo}
        throw new UnsupportedOperationException("${msg.proj.security.simpleidentityservice.implRequiredGetUserInfo}");
    }
}
