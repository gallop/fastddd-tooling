package ${packageName}.security;

import ${packageName}.mapper.${entityClassName}Mapper;
import ${packageName}.entity.${entityClassName};
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gallop.fastddd.security.spi.SecurityIdentityService;
import com.gallop.fastddd.security.spi.SecurityUserDetails;
import com.gallop.fastddd.security.spi.SecurityUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomIdentityServiceImpl implements SecurityIdentityService {

    private final ${entityClassName}Mapper ${entityClassNameLower}Mapper;

    @Override
    public SecurityUserDetails loadUserByUsername(String username) {
        ${entityClassName} entity = ${entityClassNameLower}Mapper.selectOne(
                new QueryWrapper<${entityClassName}>()
                        .eq("${usernameColumn}", username)
                        .eq("deleted", 0));
        if (entity == null) {
            return null;
        }
        return SecurityUserDetails.builder()
                .userId(String.valueOf(entity.getId()))
                .username(entity.${usernameGetter}())
                .password(entity.${passwordGetter}())
                .enabled(entity.${statusGetter}() != null && entity.${statusGetter}() != 0)
                .build();
    }

    @Override
    public SecurityUserInfo getUserInfo(String userId) {
        ${entityClassName} entity = ${entityClassNameLower}Mapper.selectById(Long.valueOf(userId));
        if (entity == null) {
            return null;
        }
        return SecurityUserInfo.builder()
                .id(String.valueOf(entity.getId()))
                .username(entity.${usernameGetter}())
                <#if realNameGetter??>.realName(entity.${realNameGetter}())</#if>
                <#if emailGetter??>.email(entity.${emailGetter}())</#if>
                <#if phoneGetter??>.phone(entity.${phoneGetter}())</#if>
                <#if avatarGetter??>.avatar(entity.${avatarGetter}())</#if>
                .enabled(entity.${statusGetter}() != null && entity.${statusGetter}() != 0)
                <#if userTypeGetter??>.userType(entity.${userTypeGetter}())</#if>
                .status(entity.${statusGetter}())
                .build();
    }
}
