package ${packageName}.security;

import ${packageName}.mapper.SecurityQueryMapper;
import com.gallop.fastddd.security.spi.SecurityPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomPermissionServiceImpl implements SecurityPermissionService {

    private final SecurityQueryMapper securityQueryMapper;

    @Override
    public List<String> getPermissionsByUserId(String userId) {
        return securityQueryMapper.selectPermissionCodesByUserId(Long.valueOf(userId));
    }

    @Override
    public List<String> getRolesByUserId(String userId) {
        return securityQueryMapper.selectRoleCodesByUserId(Long.valueOf(userId));
    }
}
