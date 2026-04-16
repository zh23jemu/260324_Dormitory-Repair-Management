# Project Summary

## Overview

本项目是一个高校宿舍报修管理系统，定位为小型单体毕业设计项目，采用前后端分离架构：

- `server`：Spring Boot 后端
- `admin-web`：PC 管理端，供宿管、维修人员、管理员使用
- `student-web`：移动端 Web 学生端
- `docs`：系统设计、接口、数据库、运行说明、论文成稿等文档
- `scripts`：开发辅助脚本
- `uploads`：上传文件目录

系统核心业务闭环为：

1. 学生提交报修
2. 宿管审核并分配维修人员
3. 维修人员接单并提交维修结果
4. 学生评价
5. 管理员查看统计与日志

## Repository Layout

### Root

- `README.md`：项目概览、启动方式、预置账号、已实现功能
- `PROJECT_SUMMARY.md`：当前文件，面向协作的结构与约定总结
- `dorm_repair.db`：SQLite 数据库文件，位于项目根目录
- `package.json`：根目录 npm 元信息，业务逻辑基本不在此处
- `毕业lw（设计）撰写规范.docx`、`任务书.docx`、`参考文献和论文要求.txt`、`三明学院学生宿舍报修管理系统的研究与分析_王剑.pdf`：论文和课题资料

### `server`

后端为 Spring Boot 3 项目，核心目录如下：

- `src/main/java/com/dormrepair/controller`：接口层
- `src/main/java/com/dormrepair/service`：业务层
- `src/main/java/com/dormrepair/config`：安全与 Web 配置
- `src/main/java/com/dormrepair/security`：JWT 认证相关实现
- `src/main/java/com/dormrepair/dto`：请求 DTO
- `src/main/java/com/dormrepair/common`：统一返回、异常处理
- `src/main/java/com/dormrepair/util`：工具类
- `src/main/resources/application.yml`：应用配置
- `src/main/resources/schema.sql`：建表脚本
- `src/main/resources/data.sql`：初始化示例数据

### `admin-web`

PC 管理端为 Vite + Vue 3 项目：

- `src/App.vue`：主页面，当前主要业务逻辑集中在一个单文件组件中
- `src/api.js`：Axios 实例与 Token 注入
- `src/main.js`：前端入口
- `src/styles.css`：页面样式

### `student-web`

学生端为 Vite + Vue 3 项目：

- `src/App.vue`：主页面，登录注册、公告、报修、工单、评价等逻辑集中于此
- `src/api.js`：Axios 实例与 Token 注入
- `src/main.js`：前端入口
- `src/styles.css`：页面样式

### `docs`

文档目录目前包含：

- `系统设计方案.md`
- `接口设计清单.md`
- `数据库建表SQL(SQLite).md`
- `运行说明.md`
- `高校宿舍报修管理系统的设计与实现_论文成稿.md`

### `scripts`

- `start-dev.ps1`：一键拉起后端、管理端、学生端三个开发进程

## Runtime and Ports

统一开发启动方式：

```powershell
.\scripts\start-dev.ps1
```

该脚本会分别打开三个 PowerShell 窗口并执行：

- `server`：`mvn spring-boot:run`
- `admin-web`：`npm run dev`
- `student-web`：`npm run dev`

默认地址与端口：

- 后端：`http://localhost:2360`
- 管理端：`http://localhost:5183`
- 学生端：`http://localhost:5184`

前端已切换为 Vue Router 结构，分别通过独立路由视图组织管理员端与学生端页面。

## Tech Stack

### Backend

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- SQLite
- `JdbcTemplate`

备注：

- `application.yml` 中保留了 `mybatis.mapper-locations` 和 `map-underscore-to-camel-case` 配置
- 但当前业务实现主要依赖 `JdbcTemplate` 和手写 SQL，不是典型的 MyBatis Mapper 驱动项目

### Frontend

- Vue 3
- Vite
- Axios
- 管理端 UI：Element Plus + ECharts
- 学生端 UI：Vant

## Key Domain Model

系统核心表包括：

- `user`
- `student_profile`
- `dorm_building`
- `dorm_room`
- `repair_type`
- `repair_order`
- `repair_order_image`
- `repair_flow`
- `repair_feedback`
- `repair_rating`
- `announcement`
- `sys_log`
- `sys_dict`

关键关系：

- 学生档案通过 `student_profile.user_id` 关联 `user`
- 工单通过 `student_id` 关联学生，通过 `assigned_repairer_id` 关联维修人员
- 工单图片、流转记录、反馈、评价均围绕 `repair_order` 组织

## Roles and Clients

系统角色固定为四类：

- `student`
- `dorm_admin`
- `repairer`
- `admin`

终端划分：

- `student` 使用 `student-web`
- `dorm_admin`、`repairer`、`admin` 使用 `admin-web`

管理端不是按前端路由拆页面，而是单页内根据当前用户角色切换可见功能。

## API Structure

后端接口统一以 `/api` 为前缀，按角色分组：

- `/api/auth`：登录、注册、当前用户、修改密码、重置密码
- `/api/student`：学生公告、报修、工单、评价、个人信息
- `/api/dorm-admin`：宿管工单、维修人员、楼栋、宿舍、学生住宿、公告
- `/api/repairer`：维修工单、接单、反馈、统计
- `/api/admin`：用户、统计、日志、字典
- `/api/common`：通用上传

上传资源访问路径：

- `/uploads/**`

统一返回结构约定：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## Authentication and Authorization

认证方式：

- 登录成功后后端返回 JWT Token
- 管理端将 Token 保存在 `localStorage.admin_token`
- 学生端将 Token 保存在 `localStorage.student_token`
- 前端通过 Axios 请求拦截器自动注入 `Authorization: Bearer <token>`

权限控制方式：

- Spring Security 负责全局认证入口
- 业务层通过 `SecurityUtils.requireRole(...)` 做角色限制

公开访问路径：

- `/api/auth/**`
- `/api/common/upload`
- `/uploads/**`

当前权限模型是“按角色字符串控制”的简化方案，不是复杂 RBAC。

## Core Workflow and Statuses

工单状态使用英文枚举值：

- `pending_review`
- `rejected`
- `pending_accept`
- `processing`
- `pending_rating`
- `completed`

状态流转固定为：

1. 学生提交报修 -> `pending_review`
2. 宿管驳回 -> `rejected`
3. 宿管分配维修人员 -> `pending_accept`
4. 维修人员接单 -> `processing`
5. 维修完成提交反馈 -> `pending_rating`
6. 学生评价 -> `completed`

每次关键状态变化都会向 `repair_flow` 写入记录。

## Data and File Conventions

### Database

- 数据库文件固定为项目根目录下的 `dorm_repair.db`
- `spring.sql.init.mode=always`
- 服务启动时会执行 `schema.sql` 和 `data.sql`

重要影响：

- 当前配置会在后端启动时重新建表并导入示例数据
- 如果需要保留手工录入数据，必须先确认是否要调整初始化策略

### Time and Formatting

- Jackson 时间格式：`yyyy-MM-dd HH:mm:ss`
- 时区：`Asia/Shanghai`
- 数据库中的时间字段当前主要以 `TEXT` 存储

### Uploads

- 上传目录配置为 `uploads`
- 文件存本地磁盘，数据库只保存路径
- 静态访问通过 `WebConfig` 映射 `/uploads/**`

### Seed Data

默认预置账号：

- 管理员：`admin / 123456`
- 宿管：`dorm01 / 123456`
- 维修人员 1：`repair01 / 123456`
- 维修人员 2：`repair02 / 123456`
- 学生 1：`student01 / 123456`
- 学生 2：`student02 / 123456`

预置数据还包括：

- 楼栋与宿舍
- 公告
- 不同状态的工单
- 维修结果
- 评价记录
- 系统日志

## Frontend Implementation Notes

当前两个前端都有几个重要约定：

- 已按角色拆分为多个视图组件，并通过 Vue Router 组织页面
- 前端默认 API 地址为 `http://localhost:2360/api`，也可通过 `VITE_API_BASE_URL` 覆盖
- 管理端默认账号占位是 `admin`
- 学生端默认账号占位是 `student01`

管理端关键实现事实：

- 同一个页面承载宿管、维修员、管理员三类角色功能
- 管理员能看统计卡片和图表
- 宿管能看工单、宿舍、公告
- 维修员能看“我的工单”并提交维修反馈
- 支持 CSV 导出

学生端关键实现事实：

- 首页集成登录注册
- 报修图片通过上传接口先传文件，再把路径带到提交报修接口
- 评价动作在工单状态为 `pending_rating` 时触发

## Notable Constraints and Caveats

- 当前项目更像“毕业设计演示型单体系统”，不是生产级工程化结构
- 后端虽然配置了 MyBatis 路径，但主要使用 `JdbcTemplate`
- 前端页面未做模块化拆分，后续功能扩展会优先面临 `App.vue` 体积过大问题
- 管理端和学生端的 `baseURL`、端口、上传资源地址均写死为本地开发环境
- 密码当前为明文示例值，不适合作为真实生产方案
- 上传目录和数据库文件都位于项目工作区，本地运行方便，但部署和持久化策略较弱
- 一些文档中存在“设计目标”大于“当前实际实现”的情况，协作时应以代码为准

## Recommended Collaboration Rules

后续修改时建议默认遵守以下约定：

- 任何关于业务能力的说明，以当前代码实现和接口事实为准，不以任务书理想目标为准
- 新增接口时保持 `/api/<role-or-domain>` 的路径分组风格
- 新增工单状态时必须同步更新：
  - 后端状态流转逻辑
  - 前端状态展示与筛选
  - 示例数据
  - 文档说明
- 涉及上传逻辑时，优先保持“文件落地本地目录，数据库存路径”的当前模型，除非明确进行存储方案升级
- 修改初始化策略前，先确认是否仍需要每次启动自动重建示例数据
- 如要增强前端功能，优先考虑先把 `App.vue` 拆分组件，再继续堆叠业务
