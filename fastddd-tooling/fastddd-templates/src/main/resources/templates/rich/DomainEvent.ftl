package ${domainPackage};

import com.gallop.fastddd.common.domain.DomainEvent;

/**
 * ${entityName}${msg.rich.domainEvent.createdSuffix}
 *
 * @author ${author}
 * @date ${date}
 */
public class ${entityName}CreatedEvent extends DomainEvent {

    private static final long serialVersionUID = 1L;

    public ${entityName}CreatedEvent(Long aggregateId) {
        super(aggregateId, "${entityName}");
    }

    @Override
    public String getEventType() {
        return "${entityName}Created";
    }
}
