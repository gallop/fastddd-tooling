package ${packageName}.security;

import com.gallop.fastddd.security.spi.SecurityIdentityService;
import com.gallop.fastddd.security.spi.SecurityPermissionService;
import com.gallop.fastddd.security.spi.SecurityUserDetails;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ${msg.proj.userDetailsServiceImpl.classDesc}
 * ${msg.proj.userDetailsServiceImpl.classDetail}
 *
 * @author ${author}
 * @date ${date}
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final SecurityIdentityService identityService;
    private final SecurityPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("${msg.proj.userDetailsServiceImpl.logLoadUser}", username);

        SecurityUserDetails userDetails = identityService.loadUserByUsername(username);

        if (userDetails == null) {
            log.warn("${msg.proj.userDetailsServiceImpl.logUserNotExists}", username);
            throw new UsernameNotFoundException("${msg.proj.userDetailsServiceImpl.userNotExistsPrefix}" + username);
        }

        if (!userDetails.isEnabled()) {
            log.warn("${msg.proj.userDetailsServiceImpl.logUserDisabled}", username);
            throw new UsernameNotFoundException("${msg.proj.userDetailsServiceImpl.userDisabledPrefix}" + username);
        }

        Collection<GrantedAuthority> authorities = getUserAuthorities(userDetails.getUserId());

        return User.builder()
                .username(userDetails.getUsername())
                .password(userDetails.getPassword())
                .authorities(authorities)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .accountExpired(false)
                .build();
    }

    /**
     * ${msg.proj.userDetailsServiceImpl.authoritiesTitle}
     * ${msg.proj.userDetailsServiceImpl.authoritiesDetail}
     *
     * @param userId ${msg.proj.userDetailsServiceImpl.paramUserId}
     * @return ${msg.proj.userDetailsServiceImpl.returnPermissions}
     */
    private Collection<GrantedAuthority> getUserAuthorities(String userId) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<String> roles = permissionService.getRolesByUserId(userId);
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));

        List<String> permissions = permissionService.getPermissionsByUserId(userId);
        permissions.forEach(perm -> authorities.add(new SimpleGrantedAuthority(perm)));

        log.debug("${msg.proj.userDetailsServiceImpl.logPermissionCount}", userId, authorities.size());
        return authorities;
    }
}
