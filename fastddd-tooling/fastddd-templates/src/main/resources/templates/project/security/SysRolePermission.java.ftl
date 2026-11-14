package ${packageName}.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gallop.fastddd.model.po.BasePO;

import jakarta.validation.constraints.NotNull;

/**
 * ${msg.proj.security.sysrolepermission.entityDesc}
 *
 * @author ${author}
 * @since ${date}
 */
@TableName("sys_role_permission")
public class SysRolePermission extends BasePO<Long> {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "${msg.proj.security.sysrolepermission.roleIdLabel}${msg.common.validation.notBlank}")
    private Long roleId;

    @NotNull(message = "${msg.proj.security.sysrolepermission.permissionIdLabel}${msg.common.validation.notBlank}")
    private Long permissionId;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getPermissionId() { return permissionId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
}
