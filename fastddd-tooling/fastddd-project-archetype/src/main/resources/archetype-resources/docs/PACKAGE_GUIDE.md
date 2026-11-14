# FastDDD Package Structure Guide

This guide explains how to organize your code based on the domain model type you selected during project initialization.

## Your Project Configuration

- **Project**: $artifactId
- **Base Package**: $package
- **Model Type**: $modelType
- **Database**: $databaseType

---

#if($modelType == "ANEMIC")
## ANEMIC Model (贫血模型) Package Structure

The ANEMIC model follows a traditional layered architecture with PO (Persistent Object) + Service pattern.

### Recommended Package Structure

```
$package/
├── domain/           # Domain entities (PO - Persistent Objects)
├── service/          # Business logic layer
├── repository/       # Data access layer (MyBatis Plus Mappers)
└── controller/       # REST API controllers
```

### Detailed Breakdown

#### 1. domain/ - Domain Layer
Contains persistent objects (PO) that map directly to database tables.

```java
package com.example.app.domain;

import com.gallop.fastddd.model.BasePO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_info")
public class User extends BasePO<Long> {
    private String username;
    private String email;
    private String phone;
}
```

#### 2. service/ - Service Layer
Contains business logic. Extends `BaseServiceImpl` for CRUD operations.

```java
package com.example.app.service;

import com.example.app.domain.User;
import com.gallop.fastddd.crud.service.IService;
import java.util.List;

public interface UserService extends IService<User> {
    User findByUsername(String username);
    List<User> searchByKeyword(String keyword);
}
```

```java
package com.example.app.service.impl;

import com.example.app.domain.User;
import com.example.app.service.UserService;
import com.example.app.repository.UserRepository;
import com.gallop.fastddd.crud.service.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> searchByKeyword(String keyword) {
        // Your business logic here
        return userRepository.searchByKeyword(keyword);
    }
}
```

#### 3. repository/ - Repository Layer
Data access using MyBatis Plus.

```java
package com.example.app.repository;

import com.example.app.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserRepository extends BaseMapper<User> {
    @Select("SELECT * FROM user_info WHERE username = #{username}")
    User findByUsername(String username);

    List<User> searchByKeyword(String keyword);
}
```

#### 4. controller/ - Controller Layer
REST API endpoints.

```java
package com.example.app.controller;

import com.example.app.domain.User;
import com.example.app.service.UserService;
import com.gallop.fastddd.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController<UserService, User, Long> {

    @Autowired
    private UserService userService;

    @GetMapping("/username/{username}")
    public User getByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }
}
```

### Quick Start Commands

Create the package structure in your terminal:

```bash
# From src/main/java directory
mkdir -p $package/domain
mkdir -p $package/service
mkdir -p $package/service/impl
mkdir -p $package/repository
mkdir -p $package/controller
```

#else
## RICH Model (充血模型) Package Structure

The RICH model follows Domain-Driven Design (DDD) principles with CQRS pattern and event-driven architecture.

### Recommended Package Structure

```
$package/
├── domain/               # Domain layer (Aggregates, Entities, Value Objects, Events)
├── application/          # Application layer (Commands, Queries, Handlers)
├── infrastructure/       # Infrastructure layer (Repositories, Mappers, PO)
└── interfaces/           # Interface layer (Controllers, DTOs)
```

### Detailed Breakdown

#### 1. domain/ - Domain Layer
Contains aggregates, entities, value objects, and domain events.

```java
package com.example.app.domain;

import com.gallop.fastddd.common.domain.AggregateRoot;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Order extends AggregateRoot<Long> {
    private String orderNo;
    private String customerId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItem> items = new ArrayList<>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    // Domain logic
    public static Order create(String customerId, List<OrderItem> items) {
        Order order = new Order();
        order.orderNo = generateOrderNo();
        order.customerId = customerId;
        order.items = items;
        order.status = OrderStatus.PENDING;
        order.totalAmount = calculateTotal(items);
        return order;
    }

    public void pay() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be paid");
        }
        status = OrderStatus.PAID;
        // Publish domain event
    }

    private static String generateOrderNo() {
        return "ORD-" + System.currentTimeMillis();
    }

    private static BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

#### 2. application/ - Application Layer
Contains commands, queries, and handlers (CQRS pattern).

```
application/
├── command/             # Commands (write operations)
│   ├── CreateOrderCommand.java
│   ├── UpdateOrderCommand.java
│   └── DeleteOrderCommand.java
├── query/               # Queries (read operations)
│   ├── GetOrderByIdQuery.java
│   └── SearchOrdersQuery.java
└── handler/             # Command/Query handlers
    ├── CreateOrderCommandHandler.java
    ├── GetOrderByIdQueryHandler.java
    └── ...
```

**Command Example:**
```java
package com.example.app.application.command;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateOrderCommand extends Command {
    @NotBlank(message = "Customer ID cannot be empty")
    private String customerId;

    @NotNull(message = "Order items cannot be null")
    private List<OrderItemDto> items;
}
```

**CommandHandler Example:**
```java
package com.example.app.application.handler;

import com.example.app.application.command.CreateOrderCommand;
import com.example.app.domain.Order;
import com.example.app.infrastructure.persistence.OrderRepository;
import com.example.app.interfaces.dto.OrderDto;
import com.gallop.fastddd.application.cqrs.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, OrderDto> {

    private final OrderRepository orderRepository;

    @Override
    public OrderDto handle(CreateOrderCommand command) {
        // Convert command to domain
        Order order = Order.create(command.getCustomerId(), ...);

        // Persist
        orderRepository.save(order);

        // Return DTO
        return OrderDto.from(order);
    }
}
```

#### 3. infrastructure/ - Infrastructure Layer
Contains repositories, mappers, and persistent objects.

```
infrastructure/
├── persistence/         # Data access
│   ├── OrderPO.java            # Persistent object (MyBatis)
│   ├── OrderMapper.java        # MapStruct mapper
│   ├── OrderRepository.java    # Repository interface
│   ├── OrderRepositoryImpl.java
│   └── OrderBaseMapper.java    # MyBatis Plus mapper
└── event/               # Event handlers
    └── OrderEventHandler.java
```

**Repository Example:**
```java
package com.example.app.infrastructure.persistence;

import com.example.app.domain.Order;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order entity);
    Optional<Order> findById(Long id);
    Optional<Order> findByOrderNo(String orderNo);
}
```

#### 4. interfaces/ - Interface Layer
Contains controllers and DTOs.

```
interfaces/
├── controller/          # REST controllers
│   ├── OrderCommandController.java
│   └── OrderQueryController.java
└── dto/                 # Data Transfer Objects
    ├── OrderDto.java
    ├── CreateOrderRequest.java
    └── OrderItemDto.java
```

**CommandController Example:**
```java
package com.example.app.interfaces.controller;

import com.example.app.application.command.CreateOrderCommand;
import com.example.app.application.handler.CreateOrderCommandHandler;
import com.example.app.interfaces.dto.OrderDto;
import com.gallop.fastddd.application.cqrs.CommandController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderCommandController extends CommandController<CreateOrderCommand, OrderDto> {

    private final CreateOrderCommandHandler handler;

    public OrderCommandController(CreateOrderCommandHandler handler) {
        this.handler = handler;
    }

    @Override
    protected CreateOrderCommandHandler getCommandHandler() {
        return handler;
    }
}
```

### Quick Start Commands

Create the package structure in your terminal:

```bash
# From src/main/java directory
mkdir -p $package/domain
mkdir -p $package/domain/event
mkdir -p $package/application/command
mkdir -p $package/application/query
mkdir -p $package/application/handler
mkdir -p $package/infrastructure/persistence
mkdir -p $package/infrastructure/event
mkdir -p $package/interfaces/controller
mkdir -p $package/interfaces/dto
```

#end

---

## General Development Workflow

### Step 1: Create Domain Model
Define your domain entities in the `domain/` package.

### Step 2: Create Repository Interface
Define data access contracts in `repository/` or `infrastructure/`.

### Step 3: Implement Service/Handler
Add business logic in `service/` or `application/handler/`.

### Step 4: Create Controller
Expose REST APIs through `controller/` or `interfaces/controller/`.

### Step 5: Test
Write unit tests in `src/test/java` mirroring the main package structure.

---

## Additional Resources

- **FastDDD Documentation**: See project README.md
- **Code Generation**: Use `fastddd-maven-plugin:generate-code` to scaffold CRUD code
- **API Reference**: Run the application and access `/doc.html` for API documentation

---

## Need Help?

- Check FastDDD framework documentation
- Review demo applications in the fastddd-studio-server module
- Refer to DDD best practices in CODING_STANDARDS.md
