---
# type: docs 
title: Springboot-公共字段填充(自定义注解+Java反射+AOP面向切面编程+动态代理)
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
tags: ["自定义注解","AOP","反射",“动态代理”"spring","springboot","Java"]
images: []
---
# Springboot-公共字段填充(Java反射+动态代理+AOP面向切面编程+自定义注解)
我猜你是学黑马的苍穹外卖学到这里了，对不？想了解公共字段填充相关知识？是的，我也是。于是我花了一天时间，查阅相关文档，自己总结了一下。（刚开始写博客，希望大佬多给出建议）
其实这块主要涉及下面四个知识：
- 反射
- 动态代理
- AOP
- 自定义注解
前面三个是层层递进的关系，AOP是基于动态代理实现的，动态代理又用到了反射。


## 参考文章：
- https://www.cnblogs.com/chanshuyi/p/head_first_of_reflection.html
- https://www.cnblogs.com/gonjan-blog/p/6685611.html
- https://blog.csdn.net/qq_21187515/article/details/109643130
## 苍穹外卖公共字段自动注入实现
### 1.自定义注解，被注解的方法作为链接点
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    OperationType value();
  }
```
### 2.定义切面类（AOP）
通过定义切面类来指定前后处理逻辑和连接点。
```java
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..))&&@annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {
        // This method is empty because it serves as a pointcut for the aspect.
        // The actual logic will be implemented in the advice methods.
    }
    @Before("autoFillPointcut()")
    public void beforeAutoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 在执行被 @AutoFill 注解的方法之前执行的逻辑
        // 这里可以添加自动填充的逻辑，比如设置创建时间、更新时间等
        Long suerId= BaseContext.getCurrentId();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFillAnnotation = methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFillAnnotation.value();
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId(); // 获取当前用户ID
        Object entity = args[0]; // 假设第一个参数是需要填充的实体对象
        if (operationType == OperationType.INSERT) {
            // 设置创建时间、创建人等
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setCreateTime.invoke(entity, now);
            setUpdateTime.invoke(entity, now);
            setCreateUser.invoke(entity, currentId);
            setUpdateUser.invoke(entity, currentId);
        }else if(operationType == OperationType.UPDATE) {
            // 设置更新时间、更新人等
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);
        } else {
            log.warn("Unsupported operation type: {}", operationType);
        }
    }
}
```
### 3.在需要自动注入的方法上加上注解
```java
    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);
     //添加员工
    @AutoFill(OperationType.INSERT)
    @Insert("insert into employee(name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user)VALUES " +
            "(#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void save(Employee employee);

```
这样，就会在执行修改和增加操作时自动注入创建/修改的用户/时间了
## 一、反射

一句话理解：反射是 Java 在运行期“检查并操作类型”的能力。它允许在不知道具体类的情况下，通过字符串名称加载类、构造对象、读取/写入字段、调用方法，甚至读取注解信息。

### 先理解一个最小示例
下面用一个最小的示例说明“普通调用 vs 反射调用”的区别与意义。

首先是一个很简单的 POJO：
```java
public class Apple {
    private int price;

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
}
```
上面只是定义了数据结构和 getter/setter，没任何“魔法”。

接着，分两段演示：先普通方式，再反射方式。

1) 普通方式（编译期就确定了类型和方法调用）
```java
Apple apple = new Apple();
apple.setPrice(5);
System.out.println("Apple Price: " + apple.getPrice());
```
这段代码直观、性能最佳，但“灵活性”有限。

2) 反射方式（运行期再决定要操作哪个类、哪个方法）
```java
Class<?> clz = Class.forName("com.chenshuyi.api.Apple");
Constructor<?> ctor = clz.getConstructor();
Object appleObj = ctor.newInstance();

Method setPrice = clz.getMethod("setPrice", int.class);
setPrice.invoke(appleObj, 14);

Method getPrice = clz.getMethod("getPrice");
System.out.println("Apple Price: " + getPrice.invoke(appleObj));
```
反射的“动态性”让我们可以在运行期按名称查找类与方法，并进行调用——这正是很多“框架能力”（如自动注入、对象转换、ORM 映射）得以实现的基础。

### 两种调用方式的取舍
| 维度 | 普通调用 | 反射调用 |
|---|---|---|
| 调用时机 | 编译期确定 | 运行期决定 |
| 性能 | 高 | 较低（方法查找与校验开销） |
| 安全 | 编译器检查完善 | 可能突破访问限制，需要谨慎（利用反射访问private成员） |
| 灵活性 | 一般 | 很高（按字符串、配置驱动） |
| 典型场景 | 业务代码 | 框架、动态代理、IDE工具 |

建议：在业务代码中优先普通调用；在“通用能力/框架层”使用反射换取灵活性。

### 常用反射操作（聚焦公共字段填充会用到的）
- 获取 Class 对象：
```java
Class<?> c1 = Person.class;           // 已知类型
Class<?> c2 = obj.getClass();         // 已有实例
Class<?> c3 = Class.forName("x.y.Person"); // 运行期按类名加载
```
- 构造与实例化：
```java
Object target = c3.getDeclaredConstructor().newInstance();
```
- 读写字段（含私有字段）：
```java
Field f = c3.getDeclaredField("createTime");
f.setAccessible(true);
f.set(target, LocalDateTime.now());
```
- 调用方法：
```java
Method m = c3.getMethod("setUpdateUser", Long.class);
m.invoke(target, 1001L);
```
- 获取类信息（调试/治理时常用）
```java
String name = c3.getName();         // 全限定名
Class<?> sup = c3.getSuperclass();  // 父类
Class<?>[] ifs = c3.getInterfaces();// 接口
```
以上就是“自动填充 createTime/updateTime/updateUser”等公共字段时最常用的反射操作。

## 二、动态代理
动态代理的“动态”，正是建立在反射之上。它允许我们在不修改原有类的前提下，统一地在方法前后织入逻辑（日志、鉴权、自动填充等）。

### 先对比一下静态代理（为理解而简化）
- 共同接口：
```java
public interface HouseSeller {
    void showHouse();
    void negotiatePrice();
    void signContract();
}
```
- 被代理对象（房主）：
```java
public class RealOwner implements HouseSeller {
    private final String name;
    public RealOwner(String name) { this.name = name; }
    public void showHouse() { System.out.println(name + " 开门带看"); }
    public void negotiatePrice() { System.out.println(name + " 底价 500 万"); }
    public void signContract() { System.out.println(name + " 签合同"); }
}
```
- 静态代理（中介固定写死在代码里）：
```java
public class PropertyAgent implements HouseSeller {
    private final HouseSeller seller;
    public PropertyAgent(HouseSeller seller) { this.seller = seller; }
    public void showHouse() { System.out.println("中介做清洁"); seller.showHouse(); }
    public void negotiatePrice() { System.out.println("中介筛选买家"); seller.negotiatePrice(); }
    public void signContract() { System.out.println("中介准备合同"); seller.signContract(); }
}
```
静态代理的问题是：每加一个新方法都要在代理类里重复一遍，维护成本高。

### 动态代理（运行期生成代理类）
1) 定义接口（与被代理对象一致）
```java
interface HouseSeller {
    void sellHouse();
    void setPrice(float price);
}
```
2) 真实对象（房主）
```java
class RealOwner implements HouseSeller {
    private float price;
    public void sellHouse() { System.out.println("以 " + price + " 万成交"); }
    public void setPrice(float price) { this.price = price; }
}
```
3) 代理的核心：InvocationHandler,并实现invoke方法
```java
class AgentHandler implements InvocationHandler {
    private final HouseSeller owner;
    public AgentHandler(HouseSeller owner) { this.owner = owner; }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("[中介] 统一前置逻辑");
        if ("setPrice".equals(method.getName())) {
            float original = (float) args[0];
            args[0] = original * 1.1f; // 统一加价 10%
        }
        Object rst = method.invoke(owner, args);
        System.out.println("[中介] 统一后置逻辑");
        return rst;
    }
}
```
4) 运行期创建代理并使用
```java
HouseSeller owner = new RealOwner();
HouseSeller agent = (HouseSeller) Proxy.newProxyInstance(
        owner.getClass().getClassLoader(),
        owner.getClass().getInterfaces(),
        new AgentHandler(owner)
);
agent.setPrice(500);
agent.sellHouse();
```
### 动态性的底层原理（JDK 动态代理）
- 运行期由 Proxy 生成一个实现了目标接口的代理类（典型命名 com.sun.proxy.$Proxy0）。
- 该代理类内部持有一个 InvocationHandler 引用。
- 代理类为每个接口方法生成同名方法，方法体统一转发到 handler.invoke(...)。

需要注意的是：
- JDK 动态代理只代理接口。它在运行期生成一个实现了相同接口的代理类，包含“同名同签名的接口方法”，但这些方法只是转发器，并不会复制真实对象的方法体。
- 每次调用都会转到 InvocationHandler.invoke(proxy, method, args)。你可以在这里改参、加前后置逻辑，最后决定是否调用 method.invoke(真实对象, 新参数)。

通过“一个 Handler 处理所有方法”，我们可以把横切逻辑集中在一起管理，这就是 AOP 的雏形。

## 三、AOP（面向切面编程）
AOP 的核心目标：在不改变业务方法代码的前提下，把“横切关注点”（日志、鉴权、事务、公共字段填充）抽取成切面进行统一管理。

### 核心术语快速图解
- 切面（Aspect）：承载横切逻辑的类
- 切入点（Pointcut）：挑选“哪些方法需要被增强”的条件，为切面类方法的参数"JoinPoint"
- 通知（Advice）：增强代码本体（前置/后置/异常/环绕）
- 连接点（JoinPoint）：程序执行到的可被织入的点（在 Spring AOP 中通常就是方法执行）

表格版速查：

| 术语 | 含义 | 示例/备注 |
|---|---|---|
| 切面 Aspect | 横切关注点的载体 | 一个类上用 @Aspect 标注 |
| 切入点 Pointcut | 选中需要增强的方法集合的条件 | execution(...) 或 @annotation(...) |
| 通知 Advice | 在切入点处执行的增强代码 | @Before/@After/@Around 等 |
| 连接点 JoinPoint | 实际被拦截到的“点” | 在 Spring AOP 中一般是方法执行 |

### 切入点表达式两种常用写法（附参数说明）

1) execution 表达式：按“方法签名”匹配

表达式模板：

```
execution(
   访问修饰符（可省略） 返回值 包名（不建议但可省略）.类名.方法名(参数) throws 异常（可省略）
)
```

各部分参数说明：

| 部分 | 必填 | 说明 | 示例 |
|---|---|---|---|
| 访问修饰符 | 否 | public/protected/...，常省略 | public |
| 返回值 | 是 | 可用通配符 * 表示任意返回值 | * 或 void/String |
| 包名 | 否 | 完整包名或.. 通配任意层级 | com.sky.mapper.. |
| 类名 | 是 | 具体类名或 * 通配 | *Mapper |
| 方法名 | 是 | 具体方法或 * 通配 | save* / * |
| 参数 | 是 | () 无参；(..) 任意；(Type,..) 指定参数 | (..)/(Long,*) |
| 异常 | 否 | throws 后可指定异常类型 | throws Exception |

常见示例：

```java
// 任意包下以 Mapper 结尾类的任意方法
execution(* *..*Mapper.*(..))
// 仅匹配 com.sky.mapper 包下所有方法
execution(* com.sky.mapper.*.*(..))
// 精确到方法与参数
execution(public void com.sky.mapper.UserMapper.save(com.sky.User))
```

2) @annotation 表达式：按“方法上标注的注解”匹配

表达式模板：

```
@annotation(注解全名或已导入的简单名)
```

说明与示例：

```java
// 匹配所有被 @AutoFill 标注的方法
@annotation(com.sky.annotation.AutoFill)

// 与 execution 组合进一步缩小范围
execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)
```

对公共字段填充而言，建议“execution 限定范围 + @annotation 声明意图”组合，既安全又直观。

### 通知类型一览（表格）

| 注解 | 触发时机 | 是否可拿到返回值/异常 | 典型用途 |
|---|---|---|---|
| @Before | 目标方法执行前 | 无返回值；无异常对象 | 参数校验、上下文准备、公共字段预填 |
| @After | 目标方法执行后（无论是否异常） | 无返回值；可知已结束 | 资源清理、审计日志收尾 |
| @AfterReturning | 目标方法正常返回后 | 可拿到返回值 | 结果增强、缓存写入 |
| @AfterThrowing | 目标方法抛异常时 | 可拿到异常对象 | 统一异常日志、告警 |
| @Around | 环绕整个调用 | 可控制执行、获取返回值/异常 | 事务、性能统计、权限、限流 |

### 一个与公共字段填充相关的切面轮廓
切点挑选“Mapper 包下、且标注了 @AutoFill 的方法”：
```java
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {}
}
```
在执行前织入“自动填充”逻辑（仅展示结构）：
```java
@Before("autoFillPointcut()")
public void beforeAutoFill(JoinPoint jp) {
    // 1) 取出方法参数（如 DTO/Entity）
    // 2) 通过反射填充 createTime/updateTime/updateUser 等字段
    // 3) 日志与异常处理
    log.info("auto-fill before method: {}", jp.getSignature());
}
```
## 四、自定义注解
公共字段填充通常配合“自定义注解”来声明意图：
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    OperationType value() default INSERT;
}
```
- 这个注解的含义是：标记的方法需要自动填充；
- value 可指示“操作类型”（如 INSERT/UPDATE），切面可据此决定填充哪些字段。

### 创建注解要点回顾
- 访问修饰符：必须是 public
- 关键字：@interface
- 元素定义：以“无参方法”形式声明参数，可通过 default 指定默认值

### 元注解（给注解“加注解”）
- @Target：注解的使用范围（比如只能标在方法上）
- @Retention：注解的生命周期（源码/类文件/运行期）
- @Inherited：注解是否可被子类继承
- @Documented：是否生成到 Javadoc

@Target 常用取值：
| 可选值 | 适用目标 |
|---|---|
| TYPE | 类、接口、枚举 |
| FIELD | 字段 |
| METHOD | 方法 |
| PARAMETER | 方法参数 |
| CONSTRUCTOR | 构造器 |
| LOCAL_VARIABLE | 局部变量 |
| ANNOTATION_TYPE | 注解类型 |
| PACKAGE | 包 |
| TYPE_PARAMETER | 类型参数（Java 8） |
| TYPE_USE | 类型使用（Java 8） |

@Retention 常用取值：
| 生命周期 | 描述 |
|---|---|
| SOURCE | 只存在于源码，编译即丢弃 |
| CLASS | 编译进 class，但运行期不可见（默认） |
| RUNTIME | 运行期可见（切面/反射最常用） |

# 小结
- 反射提供“动态读写/调用”的能力，是公共字段自动填充的底层基础；
- 动态代理把“统一前后置逻辑”抽离出来，避免在每个方法里重复粘贴；
- AOP 以注解+切点的形式把“横切关注点”模块化管理；
- 自定义注解则是“声明意图”的方式，让切面只处理该处理的点。

把以上四者串起来，你就能在 SpringBoot 中优雅地实现“创建/更新人、时间”等公共字段的自动填充，既减少重复代码，又提升一致性与可维护性。

OK,快去敲代码吧！