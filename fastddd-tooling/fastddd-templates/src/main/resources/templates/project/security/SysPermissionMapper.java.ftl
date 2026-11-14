package ${packageName}.mapper;

import ${packageName}.entity.SysPermission;
import com.gallop.fastddd.crud.repository.BaseRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ${msg.proj.security.syspermissionmapper.mapperDesc}
 *
 * @author ${author}
 * @since ${date}
 */
@Mapper
public interface SysPermissionMapper extends BaseRepository<SysPermission> {

    @Select("SELECT * FROM sys_permission WHERE permission_code = ${r"#{permissionCode}"} AND deleted = 0")
    SysPermission selectByPermissionCode(@Param("permissionCode") String permissionCode);

    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = ${r"#{roleId}"} AND p.deleted = 0 AND rp.deleted = 0")
    List<SysPermission> selectByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT * FROM sys_permission WHERE resource_type = ${r"#{resourceType}"} AND deleted = 0")
    List<SysPermission> selectByResourceType(@Param("resourceType") Integer resourceType);

    @Select("SELECT * FROM sys_permission WHERE parent_id = ${r"#{parentId}"} AND deleted = 0")
    List<SysPermission> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = ${r"#{userId}"} AND p.deleted = 0 AND rp.deleted = 0 AND ur.deleted = 0")
    List<SysPermission> selectByUserId(@Param("userId") Long userId);

    default boolean existsByPermissionCode(String permissionCode) {
        return selectByPermissionCode(permissionCode) != null;
    }
}
