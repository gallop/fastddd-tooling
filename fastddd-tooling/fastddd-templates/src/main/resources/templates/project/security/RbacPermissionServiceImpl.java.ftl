package ${packageName}.security;

import ${packageName}.mapper.SysUserMapper;
import ${packageName}.mapper.SysRoleMapper;
import ${packageName}.entity.SysRole;
import com.gallop.fastddd.security.spi.SecurityPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbacPermissionServiceImpl implements SecurityPermissionService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<String> getPermissionsByUserId(String userId) {
        return sysUserMapper.selectPermissionsByUserId(Long.valueOf(userId));
    }

    @Override
    public List<String> getRolesByUserId(String userId) {
        return sysRoleMapper.selectByUserId(Long.valueOf(userId)).stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }
}
