package ${contextPersistencePkg};

import com.baomidou.mybatisplus.annotation.TableName;
import com.gallop.fastddd.model.po.BasePO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * ${aggregateClassName} ${msg.proj.context.po.desc}
 *
 * TODO: ${msg.proj.context.po.todo}
 *
 * @author ${author}
 * @date ${date}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("${tableName}")
public class ${aggregateClassName}PO extends BasePO<Long> {

    // TODO: ${msg.proj.context.po.todo}
    private String name;
    private String status;
}
