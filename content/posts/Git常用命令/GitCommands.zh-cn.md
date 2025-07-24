---
# type: docs 
title: Git常用命令
date: 2025-07-24T20:21:17+08:00
featured: false
draft: false
comment: true
toc: true
reward: true
pinned: false
carousel: false
series:
categories: []
tags: []
images: [images/ArticlesCover/git.png]
---

# Git 常用命令

## 1. 配置命令

```bash
# 设置全局用户名和邮箱
git config --global user.name "你的用户名"
git config --global user.email "你的邮箱"

# 查看配置信息
git config --list
```

## 2. 初始化和基本操作

```bash
# 初始化仓库
git init

# 克隆远程仓库
git clone <repository-url>

# 添加文件到暂存区
git add <file-name>    # 添加指定文件
git add .              # 添加所有文件

# 提交更改
git commit -m "提交说明"
git commit -am "提交说明"  # 合并 add 和 commit
```

## 3. 分支操作

```bash
# 查看分支
git branch          # 查看本地分支
git branch -r       # 查看远程分支
git branch -a       # 查看所有分支

# 创建分支
git branch <branch-name>

# 切换分支
git checkout <branch-name>
git switch <branch-name>    # Git 2.23+ 新命令

# 创建并切换分支
git checkout -b <branch-name>
git switch -c <branch-name>

# 删除分支
git branch -d <branch-name>    # 删除本地分支
git branch -D <branch-name>    # 强制删除本地分支
git push origin --delete <branch-name>  # 删除远程分支
```

## 4. 远程仓库操作

```bash
# 添加远程仓库
git remote add origin <repository-url>

# 查看远程仓库
git remote -v

# 推送到远程
git push origin <branch-name>
git push -u origin <branch-name>  # 首次推送并设置上游分支

# 拉取更新
git pull origin <branch-name>
git fetch origin              # 获取远程更新但不合并
```

## 5. 状态和差异查看

```bash
# 查看状态
git status

# 查看差异
git diff              # 工作区与暂存区的差异
git diff --staged     # 暂存区与最后一次提交的差异
git diff <commit1> <commit2>  # 两个提交之间的差异
```

## 6. 历史记录

```bash
# 查看提交历史
git log
git log --oneline    # 简洁模式
git log --graph      # 图形模式显示
git reflog          # 查看操作历史
```

## 7. 撤销和重置

```bash
# 撤销工作区修改
git checkout -- <file-name>
git restore <file-name>    # Git 2.23+ 新命令

# 取消暂存
git reset HEAD <file-name>
git restore --staged <file-name>

# 重置到指定提交
git reset --soft <commit>    # 保留工作区和暂存区修改
git reset --mixed <commit>   # 保留工作区修改（默认）
git reset --hard <commit>    # 清除所有修改
```

## 8. 暂存和合并

```bash
# 暂存当前修改
git stash
git stash save "说明文字"

# 查看暂存列表
git stash list

# 应用暂存
git stash apply    # 应用最近的暂存（不删除暂存记录）
git stash pop      # 应用最近的暂存（删除暂存记录）

# 合并分支
git merge <branch-name>
git rebase <branch-name>    # 变基
```

## 9. 标签管理

```bash
# 创建标签
git tag <tag-name>
git tag -a <tag-name> -m "说明文字"

# 查看标签
git tag
git show <tag-name>

# 推送标签
git push origin <tag-name>
git push origin --tags    # 推送所有标签
```

## 10. 高级技巧

```bash
# 清理仓库
git clean -f    # 删除未跟踪文件
git clean -fd   # 删除未跟踪文件和目录

# 修改最后一次提交
git commit --amend

# 查找内容
git grep "查找的内容"

# 锁定文件
git update-index --assume-unchanged <file>
git update-index --no-assume-unchanged <file>
```

## 最佳实践建议

1. 经常性地提交代码，保持提交粒度适中
2. 编写清晰的提交信息
3. 定期从远程仓库拉取更新
4. 使用分支进行功能开发
5. 及时处理合并冲突
6. 慎用 `git reset --hard` 命令
7. 重要操作前先创建备份分支

## 常见问题解决

1. 提交到错误分支：
   ```bash
   git checkout correct-branch
   git cherry-pick wrong-branch
   ```

2. 撤销已推送的提交：
   ```bash
   git revert <commit>
   ```

3. 修改提交信息：
   ```bash
   git commit --amend
   ```

4. 解决冲突：
   ```bash
   git merge --abort    # 取消合并
   git reset --merge    # 重置合并
   ```

记住：使用 Git 最重要的是理解其工作流程和各个命令的作用，多加练习和实践才能熟练运用。
