package ${contextEventPkg};

import com.gallop.fastddd.common.domain.DomainEvent;

/**
 * ${aggregateClassName} ${msg.proj.context.domainEvent.desc}
 *
 * @author ${author}
 * @date ${date}
 */
public class ${aggregateClassName}CreatedEvent extends DomainEvent {

    public ${aggregateClassName}CreatedEvent(Long aggregateId) {
        super(aggregateId, "${aggregateClassName}");
    }
}
