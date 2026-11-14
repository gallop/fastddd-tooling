package ${packageName}.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gallop.fastddd.model.po.BasePO;

import jakarta.validation.constraints.NotNull;

/**
 * ${msg.proj.security.sysuserrole.entityDesc}
 *
 * @author ${author}
 * @since ${date}
 */
@TableName("sys_user_role")
public class SysUserRole extends BasePO<Long> {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "${msg.proj.security.sysuserrole.userIdLabel}${msg.common.validation.notBlank}")
    private Long userId;

    @NotNull(message = "${msg.proj.security.sysuserrole.roleIdLabel}${msg.common.validation.notBlank}")
    private Long roleId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
