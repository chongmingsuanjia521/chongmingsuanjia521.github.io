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
我

## 参考文章：
- https://www.cnblogs.com/chanshuyi/p/head_first_of_reflection.html
- https://www.cnblogs.com/gonjan-blog/p/6685611.html
- https://blog.csdn.net/qq_21187515/article/details/109643130
## 一、反射
### 什么是反射？
简单来说反射就是在运行时才知道要操作的类是什么，并且可以在运行时获取类的完整构造，并调用对应的方法。

举一个简单的例子:
```java
public class Apple {

    private int price;

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public static void main(String[] args) throws Exception{
        //正常的调用
        Apple apple = new Apple();
        apple.setPrice(5);
        System.out.println("Apple Price:" + apple.getPrice());
        //使用反射调用
        Class clz = Class.forName("com.chenshuyi.api.Apple");
        Method setPriceMethod = clz.getMethod("setPrice", int.class);
        Constructor appleConstructor = clz.getConstructor();
        Object appleObj = appleConstructor.newInstance();
        setPriceMethod.invoke(appleObj, 14);
        Method getPriceMethod = clz.getMethod("getPrice");
        System.out.println("Apple Price:" + getPriceMethod.invoke(appleObj));
    }
}
```
上面代码输出的结果为：
```shell
Apple Price:5
Apple Price:14
```
不难看出，我们调用了两次Apple类的"setPrice"。第一次调用，是直接指定类，并实例化，是编译时静态绑定，可以理解为“正射”；第二次调用则是通过Class.forName(),再运行时动态绑定，为“反射”。

主要区别：

### Java反射调用与普通调用对比

| 对比维度          | 普通调用                                     | 反射调用                                     |
|-------------------|--------------------------------------------|--------------------------------------------|
| **调用时机**       | 编译时确定                                 | 运行时动态确定                             |
| **性能**          | ⭐⭐⭐⭐⭐ (直接调用)                     | ⭐⭐ (需查找类信息，慢3-10倍)            |
| **安全性**        | 受编译器检查                               | 可突破访问限制(用`setAccessible(true)`来访问私有方法)      |
| **灵活性**        |   ⭐⭐⭐(较差)                                 | ⭐⭐⭐⭐⭐ (动态加载/调用)                 |
| **代码复杂度**     | 简单直接                                   | 复杂(需处理异常/获取元数据)                |
| **编译检查**       | 有                                         | 无(运行时才报错)                           |
| **典型场景**       | 常规业务代码                               | 框架/动态代理/IDE工具                      |
| **访问控制**       | 遵守修饰符                                 | 可访问private成员                          |
| **代码示例**       | `obj.method();`                           | `clazz.getMethod("method").invoke(obj);`  |
| **JVM优化**       | 可深度优化                                 | 优化受限                                   |
| **异常处理**       | 无需反射异常处理                           | 需处理`ReflectiveOperationException`      |
>反射调用缺点：
> - 获得动态性/灵活性
> - 损失性能/安全性
> - 增加代码复杂度
> 
> **最佳实践**：框架级开发才推荐使用反射，业务代码应优先使用普通调用
### 反射常用API
#### 1.获取反射中的Class对象
```java
// 1. 通过类名.class(只适合在编译前就知道操作的 Class)
Class<?> clazz1 = String.class;

// 2. 通过对象.getClass()
String str = "Hello";
Class<?> clazz2 = str.getClass();

// 3. 通过Class.forName() (最常用)
Class<?> clazz3 = Class.forName("java.lang.String");//需要处理异常，因为类名可能找不到
```
#### 2.通过反射创建类对象
步骤如下：
1. 获取Class对象
2. 获取其构造方法
3. 创建实例对象
```java
// 无参构造
Class<?> clazz = Class.forName("com.example.Person");
Object obj = clazz.newInstance(); // 已废弃，Java9+
Object obj = clazz.getDeclaredConstructor().newInstance(); // 推荐

// 带参构造
Constructor<?> constructor = clazz.getConstructor(String.class, int.class);
Object obj = constructor.newInstance("张三", 25);
```
#### 3.获取类属性、方法、构造器
```java
// 获取字段
Field publicField = clazz.getField("name"); // 仅公共字段
Field[]fields=clazz.getFields();//非私有字段
Field anyField = clazz.getDeclaredField("age"); // 所有字段

// 获取/设置字段值
Object value = publicField.get(obj); // 获取
publicField.set(obj, "李四"); // 设置

// 突破private限制
Field privateField = clazz.getDeclaredField("secret");
privateField.setAccessible(true); // 关键步骤
Object secretValue = privateField.get(obj);
```
#### 4.调用方法
```java
// 获取方法(两种)
Method publicMethod = clazz.getMethod("sayHello", String.class);
Method privateMethod = clazz.getDeclaredMethod("privateMethod");

// 调用方法
Object result = publicMethod.invoke(obj, "世界"); // 调用实例方法

// 调用静态方法
Method staticMethod = clazz.getMethod("staticMethod");
staticMethod.invoke(null); // 第一个参数为null

// 调用private方法
privateMethod.setAccessible(true);
privateMethod.invoke(obj);
```
>注意：如果调用的是静态方法，第一个参数为null
#### 获取类信息
```java
// 获取类名
String className = clazz.getName(); // 全限定名
String simpleName = clazz.getSimpleName(); // 简单类名

// 获取修饰符
int modifiers = clazz.getModifiers();
Modifier.isPublic(modifiers); // 检查是否为public
Modifier.isAbstract(modifiers); // 检查是否为abstract

// 获取父类和接口
Class<?> superClass = clazz.getSuperclass();
Class<?>[] interfaces = clazz.getInterfaces();

// 获取注解
Annotation[] annotations = clazz.getAnnotations();
```
## 二、动态代理
了解了反射，接下来就可以说动态代理了，前面说了，反射具有动态性，所以动态代理的“动态”用到了反射。

代理模式是常用的java设计模式，他的特征是代理类与委托类有同样的接口，代理类主要负责为委托类预处理消息、过滤消息、把消息转发给委托类，以及事后处理消息等。代理类与委托类之间通常会存在关联关系，一个代理类的对象与一个委托类的对象关联，代理类的对象本身并不真正实现服务，而是通过调用委托类的对象的相关方法，来提供特定的服务。简单的说就是，我们在访问实际对象时，是通过代理对象来访问的，代理模式就是在访问实际对象时引入一定程度的间接性，因为这种间接性，可以附加多种用途。
### 静态代理
要想理解动态代理，先理解什么是静态代理：
#### 定义
由程序员创建或特定工具自动生成源代码，也就是在编译时就已经将接口，被代理类，代理类等确定下来。在程序运行之前，代理类的.class文件就已经生成。
#### 简单实现
让我们用一个生活中的房产中介例子来理解静态代理模式：
场景描述
假设你要卖房子，但不想直接跟买家打交道（太麻烦），于是你找了房产中介帮你处理所有事务。
下面简单实现了一个静态代理
1. 定义业务接口
```java
/**
 * 卖房接口 - 定义卖房的基本能力
 * 就像"能提供卖房服务"这个抽象概念
 */
public interface HouseSeller {
    void showHouse();      // 展示房子
    void negotiatePrice(); // 价格谈判
    void signContract();   // 签合同
}
```
2. 生活中的静态代理例子：房产中介
让我们用一个生活中的房产中介例子来理解静态代理模式：

场景描述
假设你要卖房子，但不想直接跟买家打交道（太麻烦），于是你找了房产中介帮你处理所有事务。

1. 定义共同接口（卖房能力）
```java
/**
 * 卖房接口 - 定义卖房的基本能力
 * 就像"能提供卖房服务"这个抽象概念
 */
public interface HouseSeller {
    void showHouse();      // 展示房子
    void negotiatePrice(); // 价格谈判
    void signContract();   // 签合同
}
```
1. 真实房主（被代理对象）
```java
/**
 * 真实的房主 
 * 确实有卖房能力，但不想亲自处理所有事情
 */
public class RealOwner implements HouseSeller {
    private String name;
    
    public RealOwner(String name) {
        this.name = name;
    }
    
    @Override
    public void showHouse() {
        System.out.println(name + "打开房门让买家看实际的房子");
    }
    
    @Override
    public void negotiatePrice() {
        System.out.println(name + "私下表示最低心理价位是500万");
    }
    
    @Override
    public void signContract() {
        System.out.println(name + "在合同上签字");
    }
}
```
3. 房产中介（代理对象）
把房主作为成员变量。
```java
/**
 * 房产中介 - 你的静态代理
 * 也实现了卖房接口，但会添加额外服务
 */
public class PropertyAgent implements HouseSeller {
    private HouseSeller seller;  // 持有真实房主引用
    private String companyName;
    
    public PropertyAgent(HouseSeller seller, String companyName) {
        this.seller = seller;
        this.companyName = companyName;
    }
    
    @Override
    public void showHouse() {
        System.out.println(companyName + "中介提前打扫房屋");
        System.out.println("中介用专业话术介绍房子优势");
        seller.showHouse();  // 最后还是需要房主开门
    }
    
    @Override
    public void negotiatePrice() {
        System.out.println(companyName + "中介先过滤掉出价过低的买家");
        seller.negotiatePrice();
        System.out.println("中介帮您把价格谈到520万成交");
    }
    
    @Override
    public void signContract() {
        System.out.println("中介准备所有法律文件");
        seller.signContract();
        System.out.println("中介协助完成过户手续");
    }
    
    // 中介的增值服务
    public void advertise() {
        System.out.println(companyName + "在各大平台推广您的房源");
    }
}
```
4. 使用代理
创建代理对象，将房主作为参数传入
```java
public class ProxyPatternDemo {
    public static void main(String[] args) {
        // 真实房主（你）
        HouseSeller realOwner = new RealOwner("张先生");
        
        // 房产中介（你的代理）
        PropertyAgent agent = new PropertyAgent(realOwner, "我爱我家");
        
        // 中介开始工作（你完全不用出面）
        agent.advertise();
        agent.showHouse();
        agent.negotiatePrice();
        agent.signContract();
    }
}
```
### 动态代理
#### 定义

代理类在程序运行时创建的代理方式被成为动态代理。 我们上面静态代理的例子中，代理类(studentProxy)是自己定义好的，在程序运行之前就已经编译完成。然而动态代理，代理类并不是在Java代码中定义的，而是在运行时根据我们在Java代码中的“指示”动态生成的。相比于静态代理， 动态代理的优势在于可以很方便的对代理类的函数进行统一的处理，而不用修改每个代理类中的方法。 比如说，想要在每个代理的方法前都加上一个处理方法.Java中的AOP(面向切面编程就是动态代理原理)
#### 实现步骤
动态代理的关键是实现 InvocationHandler接口中的invoke方法，位于java.lang.reflect包下。
```java
import java.lang.reflect.*;

// 1. 业务接口（卖房能力）
interface HouseSeller {
    void sellHouse();
    void setPrice(float price);
}

// 2. 真实房主
class RealOwner implements HouseSeller {
    private float price;
    
    @Override
    public void sellHouse() {
        System.out.println("房主以" + price + "万元价格签字售房");
    }
    
    @Override
    public void setPrice(float price) {
        this.price = price;
    }
}

// 3. 中介处理器（核心逻辑）
class AgentHandler implements InvocationHandler {
    private final HouseSeller owner;
    
    public AgentHandler(HouseSeller owner) {
        this.owner = owner;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("【中介】挂牌房源");
        
        if (method.getName().equals("setPrice")) {
            float originalPrice = (float) args[0];
            args[0] = originalPrice * 1.1f; // 中介加价10%
            System.out.println("【中介】将报价从" + originalPrice + "提高到" + args[0]);
        }
        
        Object result = method.invoke(owner, args);
        
        System.out.println("【中介】完成交易，收取佣金");
        return result;
    }
}

// 4. 使用示例
public class DynamicProxyDemo {
    public static void main(String[] args) {
        // 创建真实房主
        HouseSeller owner = new RealOwner();
        
        // 创建动态代理中介
        HouseSeller agent = (HouseSeller) Proxy.newProxyInstance(
            owner.getClass().getClassLoader(),
            owner.getClass().getInterfaces(),
            new AgentHandler(owner)
        );
        
        // 通过代理操作
        agent.setPrice(500);  // 房主想卖500万
        agent.sellHouse();    // 实际售价550万
    }
}
```
## 三、AOP（面向注解编程）
### 什么是AOP？
AOP(Aspect Orient Programming),和OOP（Object Orented Programming）一样，是一种编程思想。它可以实现在不改变源代码的情况下，在程序执行之前或之后执行自定义操作。它是基于代理模式实现的，这也就是为什么前面要讲动态代理。
### 核心术语
1. 切面(Aspect)

    切面是AOP的核心，它是指横切关注点的模块化表现。它通常包含通知和切入点。常见的切面包括日志记录、事务管理等
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
    }

    ```
2. 连接点(JoinPoint)
    表示该切面所控制的方法，也就是执行过程中能够插入切面的地方，在Spring AOPO中，连接点通常是方法的执行，每个连接点都对应着程序的某个位置，AOP通过在这些连接点插入额外的行为来增强功能。
    实例代码如下，连接点为切面类里方法的参数JoinPoint
    ```java
    @Before("autoFillPointcut()")
    public void beforeAutoFill(JoinPoint joinPoint)  {
    }
    ```

3. 通知(Advice)

    通知是AOP中增强功能的具体实现，它定义了在连接点发生时，应该做什么。通知分为不同类型，比如前置通知、后置通知、异常通知等。通知与切点配合使用，决定了在程序的哪个阶段执行增强行为。

    - 前置通知（@Before）：在目标方法执行之前执行通知。
    - 后置通知（@After）：在目标方法执行之后执行通知，无论方法是否抛出异常。
    - 返回通知（@AfterReturning）：在目标方法正常执行后执行通知。
    - 异常通知（@AfterThrowing）：在目标方法抛出异常时执行通知。
    - 环绕通知（@Around）：它可以控制目标方法的执行，既可以在目标方法之前执行代码，也可以在目标方法之后执行代码。
```java
@Before("autoFillPointcut()")
    public void beforeAutoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 在执行被 @AutoFill 注解的方法之前执行的逻辑
        // 这里可以添加自动填充的逻辑，比如设置创建时间、更新时间等
        log.info("Executing auto-fill logic before method execution");
    }
```

4. 切入点(Pointcut)

    切入点定义了通知在哪些连接点上执行，通常是通过表达式来定义的。切入点是一个条件，指定了哪些方法需要被拦截。它与通知配合，决定了通知在哪些方法上生效。
    常见的切入点表达式有(execution和Annotation)：
    
    ```java
    execution(
       访问修饰符（可省略）返回值 包名（不建议但可省略）.类名.方法名(参数) throw 异常(可省略)
    )

    ```
    ```java
    @annotation(注解名)
    ```
    >注意，可以用&&使用多个切入点表达式

## 四、自定义注解
如果要用annotation标记切入点，那就要用到自定义注解了。下面是苍穹外卖中的自动注入注解。
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    OperationType value() default INSERT;
  }
  ```
### 创建注解的要点：
- 修饰符：访问修饰符必须为public，不写默认为public
- 关键字：关键字为@interface
- 注解名称：如上面的AutoFill,定义了注解的名称
- 注解类型元素：为注解中的参数，通过方法指定，方法的返回类型为注解的参数类型，方法名为参数名，还可以用default来设置默认值。
### 元注解
元注解的作用就是负责注解其他注解。Java5.0定义了4个标准的元注解类型，它们被用来提供对其它注解类型作标志操作（可以理解为最小的注解，基础注解）
#### 1.@Target
用于描述注解的使用范围，表示该注解可以用在什么地方，如上面代码，表示该注解用在方法上。
| 可选值(以ElementType为前缀)            | 适用目标                          | 引入版本 |
|-------------------|-----------------------------------|----------|
| `TYPE`           | 类、接口(包括注解)、枚举           | Java 5   |
| `FIELD`          | 字段(包括枚举常量)                 | Java 5   |
| `METHOD`         | 方法                              | Java 5   |
| `PARAMETER`      | 方法参数                          | Java 5   |
| `CONSTRUCTOR`    | 构造器                            | Java 5   |
| `LOCAL_VARIABLE` | 局部变量                          | Java 5   |
| `ANNOTATION_TYPE`| 注解类型(用于元注解)               | Java 5   |
| `PACKAGE`        | 包                                | Java 5   |
| `TYPE_PARAMETER` | 类型参数(泛型声明中的参数)         | Java 8   |
| `TYPE_USE`       | 类型使用(任何使用类型的地方)       | Java 8   |
#### 2.@Retention
|生命周期类型(以RetentionPolicy为前缀)|描述|
|---|---|
|SOURCE|编译时被丢弃，不包含在类文件中|
|CLASS|JVM加载时被丢弃，包含在类文件中，默认值|
|RUNTIME|由JVM 加载，包含在类文件中，在运行时可以被获取到(最常用)|
#### 3.@Inherited：
是一个标记注解，@Inherited阐述了某个被标注的类型是被继承的，无参数。如果一个使用了@Inherited修饰的annotation类型被用于一个class，则这个annotation将被用于该class的子类。
#### 4.Documented
表明该注解标记的元素可以被Javadoc 或类似的工具文档化(我暂且没有体会到这一功能)

# 小结
暂时想写的就这么多，对Java和Spring底层的原理并没有了解太多，这些后续有能力再做了解。理解归理解，多敲代码、会用才是王道。