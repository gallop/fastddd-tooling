package ${contextPersistencePkg};

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gallop.fastddd.crud.repository.BaseRepository;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * ${aggregateClassName} Mapper
 *
 * @author ${author}
 * @date ${date}
 */
@Mapper
public interface ${aggregateClassName}Mapper extends BaseRepository<${aggregateClassName}PO> {

    default Optional<${aggregateClassName}PO> selectByName(String name) {
        return Optional.ofNullable(selectOne(
                new LambdaQueryWrapper<${aggregateClassName}PO>()
                        .eq(${aggregateClassName}PO::getName, name)));
    }

    default List<${aggregateClassName}PO> selectAll() {
        return selectList(new LambdaQueryWrapper<>());
    }
}
