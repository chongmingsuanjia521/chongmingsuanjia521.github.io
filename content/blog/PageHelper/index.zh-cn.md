---
# type: docs 
title: Pagehelper在Springboot项目中的使用方法
date: 2025-08-02T09:31:09+08:00
featured: false
draft: false
comment: true
toc: true
reward: true
pinned: false
carousel: false
series:
categories: ["Java后端开发"]
tags: ["springboot","mybatis","pagehelper"]
images: []
---
这两天在学习苍穹外卖，发现之前学的PageHelper用法忘了，所以阅读了官方文档和相关技术博客，自己再做一个复习。
# 1.引入依赖
如果用的是maven，导入以下依赖：
```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
</dependency>
```
# 2.相关配置
## application配置
完整配置参考 https://github.com/abel533/MyBatis-Spring-Boot/blob/master/src/main/resources/application-old.yml

以下是和Mybatis相关配置
```yml
mybatis:
    type-aliases-package: tk.mybatis.springboot.model
    mapper-locations: classpath:mapper/*.xml

mapper:
    mappers:
        - tk.mybatis.springboot.util.MyMapper
    not-empty: false
    identity: MYSQL

pagehelper:
    helperDialect: mysql
    reasonable: true
    supportMethodsArguments: true
    params: count=countSql
```
# 3.在代码中使用
下面演示以下苍穹外卖员工分页查询（根据name模糊查询）相关代码:
## Controller层

```java
//controller层
    @GetMapping("/page")
    @ApiOperation("分页查询员工")//swagger接口标签
    public Result<PageResult> PageQuery(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("分页查询,{}",employeePageQueryDTO);
        PageResult pageResult= employeeService.pagequery(employeePageQueryDTO);
        return Result.success(pageResult);
    }
```
## 定义PageResult,用来封装查询记录总数和分页查询结果,如下：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult implements Serializable {

    private long total; //总记录数

    private List records; //当前页数据集合

}
```
## Service层
```java
//Service层
 public PageResult pagequery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page =employeeMapper.pagequery(employeePageQueryDTO);
        PageResult pageResult=new PageResult();
        pageResult.setRecords(page.getResult());
        pageResult.setTotal(page.getTotal());
        return pageResult;
    }
```
### 在Service层用到PageHelper的核心用法：

1. 开始分页：调用静态方法：
    ```java
    PageHelper.startPage(page,pageSize)；
    ```
    其中，page为起始页码，pageSize为页面大小
2. 调用Mapper方法查询所需数据列表,类型为Page<Employee>(或为所需类型)
        
    #### 注：Page<T> 属性（继承自 ArrayList<T>）

    | 属性名     | 类型       | 描述                         |
    |------------|------------|------------------------------|
    | pageNum    | int        | 当前页码                     |
    | pageSize   | int        | 每页记录数                   |
    | startRow   | int        | 当前页的起始行（包含）       |
    | endRow     | int        | 当前页的结束行（包含）       |
    | total      | long       | 总记录数                     |
    | pages      | int        | 总页数                       |
    | result     | List<T>    | 当前页的数据列表（等同于本身）|
    | reasonable | boolean    | 是否启用合理化分页           |
3. 用getResult()方法获取页面结果，getTotal获取总记录数
    ```java
    //mapper层
    Page<Employee> pagequery(EmployeePageQueryDTO employeePageQueryDTO);
        //employeeMapper.xml
        <select id="pagequery" resultType="com.sky.entity.Employee">

                    SELECT * FROM employee
                    <where>
                        <if test="name != null and name != ''">
                            AND name LIKE CONCAT('%', #{name}, '%')
                        </if>
                    </where>
                    ORDER BY create_time DESC

            </select>
    ```