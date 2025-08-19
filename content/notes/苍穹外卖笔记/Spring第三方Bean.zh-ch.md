---
title: Spring第三方Bean详解
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
tags: ["苍穹外卖","spring","Bean"]
images: []
---
# Spring第三方Bean详解
参考文章：
- https://zhuanlan.zhihu.com/p/437538432
![alt text](image.png)
在学苍穹外卖的过程中遇到了"Spring第三方Bean"这个知识点，搞不懂为什么有时候直接用Spring默认的Bean,有时候要自己注入。所以搜索相关文章，在下面说一下自己的见解。

## 第三方Bean的三种方式步骤及使用场景
不建议在启动类中定义Bean,这样做是为了保证启动类的纯粹性。
## 使用Bean的好处
交给IOC容器管理，减少开销。
