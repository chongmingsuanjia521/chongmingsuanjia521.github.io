---
title: SpringTask笔记
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
# Spring Task
## 一、使用步骤
1. 导入Maven坐标（一般包含在starter里面）
2. 启动类添加注解@EnableScheduling
3. 自定义定时任务类（要加@Component注解）

## 二、使用示例
```java

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    @Scheduled(cron="0 * * * * ? ")
    public void processTimeoutOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        LocalDateTime dueTime=LocalDateTime.now().minusMinutes(15);
        List<Orders> timeOutOrders= orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,dueTime);
        if(timeOutOrders!=null && !timeOutOrders.isEmpty()){
            for(Orders order :timeOutOrders){
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder(){
        log.info("自动查询派送中的订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().minusMinutes(60);

        List<Orders> timeOutOrders= orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,time);
        if(timeOutOrders!=null && !timeOutOrders.isEmpty()){
            for(Orders order :timeOutOrders){
                order.setStatus(Orders.COMPLETED);
                order.setCancelReason("长时间处于派送中，自动完成");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }
}

```
## 三、注意事项
