---
# type: docs 
title: Spring事务
date: 2025-08-04T19:57:58+08:00
featured: false
draft: false
comment: true
toc: true
reward: true
pinned: false
carousel: false
series:
categories: [Java后端开发]
tags: ["事务","spring","springboot","Java"]
images: []
---
# Spring事务详解（基于苍穹外卖项目学习）

最近在学习苍穹外卖项目时，接触到了 Spring 中的事务（`@Transactional` 注解）。为加深理解，我查阅了一些高质量文章并进行总结，本文将以知识笔记的形式，系统性地讲解 Spring 中的事务机制，便于自己日后查阅与回顾。

参考文章：

- [https://javaguide.cn/system-design/framework/spring/spring-transaction.html](https://javaguide.cn/system-design/framework/spring/spring-transaction.html)
- [https://zhuanlan.zhihu.com/p/433276682](https://zhuanlan.zhihu.com/p/433276682)

---

## 一、什么是事务？

事务（Transaction）是指一组操作的集合，这些操作要么全部成功执行，要么全部不执行，是数据库操作中的基本单位。

## 二、事务的ACID特性

事务必须具备以下四个特性（ACID）：

| 特性                | 含义       | 说明                              |
| ----------------- | -------- | ------------------------------- |
| 原子性 (Atomicity)   | 不可分割     | 事务作为最小的执行单位，要么全部执行，要么全部回滚       |
| 一致性 (Consistency) | 数据保持一致   | 事务执行前后，数据库始终处于一致状态，例如转账时账户总金额不变 |
| 隔离性 (Isolation)   | 并发时互不影响  | 多个事务并发执行时，各自互不干扰                |
| 持久性 (Durability)  | 一旦提交永久生效 | 提交后的数据更改是永久性的，即使系统崩溃也不会丢失       |

**补充理解：** AID是实现手段，C是一致性目标，只有保障了原子性、隔离性、持久性，一致性才能得以实现。

---

## 三、Spring事务管理机制

Spring 提供了对事务管理的高度抽象和封装，核心接口如下：

- `PlatformTransactionManager`：事务管理器，定义了事务的基本操作接口。
- `TransactionDefinition`：事务的定义信息，包括隔离级别、传播行为、是否只读、超时时间等。
- `TransactionStatus`：事务的运行状态，包含事务是否新建、是否回滚、是否完成等信息。

### 1. PlatformTransactionManager

Spring 提供了多个事务管理器的实现，常见如下：

| 平台        | 实现类                            |
| --------- | ------------------------------ |
| JDBC      | `DataSourceTransactionManager` |
| Hibernate | `HibernateTransactionManager`  |
| JPA       | `JpaTransactionManager`        |

使用方式示例：

```java
TransactionStatus status = txManager.getTransaction(definition);
txManager.commit(status);
```

### 2. TransactionDefinition（事务属性）

- 传播行为（Propagation）
- 隔离级别（Isolation）
- 超时时间（timeout）
- 是否只读（readOnly）
- 异常回滚策略

### 3. TransactionStatus（事务状态）

```java
public interface TransactionStatus {
    boolean isNewTransaction();    // 是否为新事务
    boolean hasSavepoint();        // 是否有保存点
    void setRollbackOnly();       // 设置为回滚状态
    boolean isRollbackOnly();     // 是否处于回滚状态
    boolean isCompleted();        // 是否已完成
}
```

---

## 四、事务属性详解

### 1. 事务传播行为（Propagation）

| 类型             | 含义     | 行为描述                                    |
| -------------- | ------ | --------------------------------------- |
| REQUIRED       | 默认传播行为 | 如果当前没有事务，则新建事务；如果存在事务，则加入当前事务           |
| REQUIRES\_NEW  | 总是新建事务 | 如果当前存在事务，则挂起当前事务，创建一个新的事务               |
| NESTED         | 嵌套事务   | 如果当前存在事务，则在嵌套事务中执行（有保存点），否则表现为 REQUIRED |
| SUPPORTS       | 支持事务   | 如果当前有事务，则加入事务；如果没有事务，则以非事务方式执行          |
| NOT\_SUPPORTED | 不支持事务  | 总是以非事务方式执行；如果当前存在事务，则挂起当前事务             |
| NEVER          | 不允许事务  | 总是以非事务方式执行；如果当前存在事务，则抛出异常               |
| MANDATORY      | 必须存在事务 | 如果当前存在事务，则加入事务；如果没有事务，则抛出异常             |

### 2. 事务隔离级别（Isolation）

| 隔离级别             | 行为描述                        | 问题类型                | 性能            |
| ---------------- | --------------------------- | ------------------- | ------------- |
| READ UNCOMMITTED | 允许读取尚未提交的数据                 | 可能出现脏读、不可重复读、幻读     | 最好            |
| READ COMMITTED   | 只能读取已提交的数据                  | 可防止脏读，仍可能出现不可重复读、幻读 | 较好（Oracle 默认） |
| REPEATABLE READ  | 同一事务中多次读取结果一致，其他事务无法修改已读取数据 | 可防止脏读、不可重复读，可能有幻读   | 一般（MySQL 默认）  |
| SERIALIZABLE     | 完全串行化执行事务，防止一切并发问题          | 可防止所有问题，但性能最差       | 最差            |

> 虚读/幻读：指某事务在两次查询中，其他事务插入了新数据，导致查询结果数量不同。

### 3. 事务超时设置

事务超过指定时间未完成，会自动回滚。默认值为 `-1`，单位是秒。

```java
@Transactional(timeout = 10) // 最长执行时间10秒
```

### 4. 只读事务

适用于只读操作，数据库可能进行优化，提升查询性能。

```java
@Transactional(readOnly = true)
```

---

## 五、@Transactional 注解详解

Spring推荐使用 `@Transactional` 注解来声明式地管理事务。

- 作用范围：**类上**或**方法上**（推荐方法上）
- 不支持接口上使用
- 仅支持 `public` 方法上使用

### 常用属性

| 属性          | 说明     | 默认值              |
| ----------- | ------ | ---------------- |
| propagation | 传播行为   | REQUIRED         |
| isolation   | 隔离级别   | 默认数据库隔离级别        |
| timeout     | 超时时间   | -1（无限）           |
| readOnly    | 是否只读   | false            |
| rollbackFor | 回滚异常类型 | RuntimeException |

---

## 六、事务失效的几种常见情况

| 情况             | 说明                                 |
| -------------- | ---------------------------------- |
| 修饰方法非 `public` | Spring AOP 基于代理，仅对 public 方法有效     |
| 内部方法调用         | 类内部方法调用不会经过代理，事务不生效                |
| 未被 Spring 管理   | 如未使用 `@Component` 等注解注册 Bean，事务不生效 |
| 数据库不支持事务       | 如 MyISAM 引擎不支持事务                   |
| 配置错误           | 如未正确配置事务管理器或注解                     |

---

## 七、使用事务的最佳实践总结

- 避免在接口上使用 `@Transactional` 注解；
- 避免类内部调用事务方法，必要时通过注入方式调用；
- 指定 `rollbackFor`，保证异常正确触发回滚；
- 尽量控制事务范围，不要在事务中处理不必要的逻辑；
- 使用只读事务优化查询方法；
- 明确设置事务传播行为，避免传播嵌套问题；

---

## 八、Spring Boot 中使用事务示例

下面是一个使用 Spring Boot + MyBatis 的简单事务示例，展示了 `@Transactional` 的基本使用：

```java
@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Transactional(rollbackFor = Exception.class)
    public void transfer(String fromUser, String toUser, Integer amount) {
        // 减去转出用户的钱
        accountMapper.decrease(fromUser, amount);

        // 模拟异常：如除以0会触发事务回滚
        int error = 1 / 0;

        // 增加接收用户的钱
        accountMapper.increase(toUser, amount);
    }
}
```

> 注意：
>
> - 被注解方法必须为 public
> - 抛出的异常必须匹配 `rollbackFor`
> - 如果发生异常，所有数据库操作都会回滚

---

如果你也在学习 Spring 项目开发，建议结合实际代码、调试日志、数据库回滚等实际效果，多动手实验，才能真正掌握事务机制的运作细节。

