# 高校宿舍报修管理系统

## 项目结构

- `server`：Spring Boot 后端
- `admin-web`：统一前端，包含公共门户、学生端页面、宿管端、维修员端、管理员端
- `docs`：设计方案、数据库和接口文档

## 技术栈

- 后端：Spring Boot 3、Spring Security、JWT、SQLite、JdbcTemplate
- 前端：Vue 3、Vite、Vue Router、Element Plus、Vant、ECharts

## 启动方式

### 1. 启动后端

```powershell
cd C:\Coding\260324_Dormitory-Repair-Management\server
cmd /c "mvn -Dspring-boot.run.arguments=--server.port=2360 spring-boot:run"
```

后端地址：`http://localhost:2360`

### 2. 启动统一前端

```powershell
cd C:\Coding\260324_Dormitory-Repair-Management\admin-web
npm install
$env:VITE_API_BASE_URL='http://localhost:2360/api'
npm run dev
```

统一前端地址：`http://localhost:5183`

主要入口：

- 统一门户首页：`http://localhost:5183/home`
- 统一登录页：`http://localhost:5183/login`

## 一键启动

```powershell
cd C:\Coding\260324_Dormitory-Repair-Management
powershell -ExecutionPolicy Bypass -File .\scripts\start-dev.ps1
```

说明：

- 一键启动只会拉起后端和统一前端
- Windows `PowerShell` 下推荐使用 `cmd /c "mvn -Dspring-boot.run.arguments=--server.port=2360 spring-boot:run"`
- 不建议写成 `mvn spring-boot:run "-Dspring-boot.run.arguments=..."`，部分环境会出现参数解析异常

## 预置账号

- 管理员：`admin / 123456`
- 宿管：`dorm01 / 123456`
- 维修人员 1：`repair01 / 123456`
- 维修人员 2：`repair02 / 123456`
- 学生 1：`student01 / 123456`
- 学生 2：`student02 / 123456`

## 示例数据

后端启动时会自动导入一批示例数据，包括：

- 多个角色账号
- 多个楼栋和宿舍
- 多个公告
- 不同状态的工单：`待审核`、`已驳回`、`待接单`、`处理中`、`待评价`、`已完成`
- 维修结果、评价记录、系统日志

如果想重新生成示例数据，可删除项目根目录下的 `dorm_repair.db` 后重启后端。

## 已实现内容

- 公共门户：首页统一展示公告、报修记录、服务统计、维修排行和论坛内容
- 统一登录：学生、宿管、维修员、管理员共用一个登录页
- 学生功能：注册登录、找回密码、提交报修、查看工单、评价、留言、论坛、维修员信息、个人中心
- 宿管功能：工单审核、分配维修、宿舍与住宿管理、公告/论坛/留言/评价管理
- 维修员功能：接单处理、完工反馈、个人统计、个人信息与工种管理
- 管理员功能：统计概览、用户、基础配置、耗材、日志等后台能力
