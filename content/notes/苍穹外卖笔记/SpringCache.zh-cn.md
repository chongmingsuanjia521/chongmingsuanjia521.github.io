---
title: SpringCache笔记
type: docs
date: 2025-08-14T10:52:55+08:00
featured: false
draft: false
comment: true
toc: true
reward: true
pinned: false
carousel: false
series:
categories: [苍穹外卖笔记]
tags: ["苍穹外卖","spring"]
images: []
---
# Spirng Cache
## 分类
- RedisCache
- EhCache
- ConcurrentMapCache
## 导入依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```
## 四个注解
1. @EnableCaching
    凯琪缓存注解功能，通常加在启动类application.java上
    ```java
    @Slf4j
    @SpringBootApplication
    @EnableCaching
    public class CacheDemoApplication {
        public static void main(String[] args) {
            SpringApplication.run(CacheDemoApplication.class,args);
            log.info("项目启动成功...");
        }
    }
    ```
2. @CachePut(cacheNames,key)

    将参数存入reids中，键名为"cacheNames::key" ,":"冒号表示树形结构，相当于java.lang的".”,key用的是EL表达式.
    常用于修改。
    ```java
    @Insert("insert into user(name,age) values (#{name},#{age})")
        //返回主键
        @Options(useGeneratedKeys = true,keyProperty = "id")
        @CachePut(cacheNames = "userCache", key = "#user.id")//userCache
        void insert(User user);
    ```
3. Cacheable

    在方法执行前先查询有无数据，若有，则直接返回缓存数据；若无，则调用方法并将方法返回值存入缓存。常用于查询。
    ```java
     @Cacheable(cacheNames = "userCache" ,key="#id")
    public User getById(Long id){
        User user = userMapper.getById(id);
        return user;
    }
    ```
4. @CacheEvict

    将一条或多条数据删除,一般用在增/删/改方法中,allEntries为删除满足"cacheNames"前缀的所有记录。
    ```java
    	@DeleteMapping("/delAll")
    @CacheEvict(cacheNames = "userCache",allEntries = true)
    public void deleteAll(){
        userMapper.deleteAll();
    }

    ```
    
>注意：Cacheable是在调用方法之前，Spring的代理对象会在数据库中查询数据，若存在，则不调用方法直接返回；若不存在则调用方法。和CachePut不同，CachePut是无论查询到，都会调用方法。