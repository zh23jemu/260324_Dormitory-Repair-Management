# 高校宿舍报修管理系统

## 项目结构

- `server`：Spring Boot 后端
- `admin-web`：PC 管理端
- `student-web`：移动端 Web 学生端
- `docs`：设计方案、数据库和接口文档

## 技术栈

- 后端：Spring Boot 3、Spring Security、JWT、SQLite、JdbcTemplate
- 管理端：Vue 3、Vite、Element Plus、ECharts
- 学生端：Vue 3、Vite、Vant

## 启动方式

### 1. 启动后端

```powershell
cd C:\Coding\260324_Dormitory-Repair-Management\server
cmd /c "mvn -Dspring-boot.run.arguments=--server.port=2360 spring-boot:run"
```

后端默认地址：`http://localhost:2360`

### 2. 启动管理端

```powershell
cd C:\Coding\260324_Dormitory-Repair-Management\admin-web
npm install
$env:VITE_API_BASE_URL='http://localhost:2360/api'
npm run dev
```

管理端地址：`http://localhost:5183`
未显式设置 `VITE_API_BASE_URL` 时，前端默认也会请求 `http://localhost:2360/api`

### 3. 启动学生端

```powershell
cd C:\Coding\260324_Dormitory-Repair-Management\student-web
npm install
$env:VITE_API_BASE_URL='http://localhost:2360/api'
npm run dev
```

学生端地址：`http://localhost:5184`

## 一键启动

```powershell
cd C:\Coding\260324_Dormitory-Repair-Management
powershell -ExecutionPolicy Bypass -File .\scripts\start-dev.ps1
```

说明：

- Windows `PowerShell` 下推荐使用 `cmd /c "mvn -Dspring-boot.run.arguments=--server.port=2360 spring-boot:run"`
- 如果仍想直接在 `PowerShell` 调 Maven，也可以使用 `mvn --% -Dspring-boot.run.arguments=--server.port=2360 spring-boot:run`
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

如果想重新生成示例数据，删除项目根目录下的 `dorm_repair.db` 后重启后端即可。

## 已实现内容

- 学生注册登录、提交报修、查看报修、评价工单、查看公告
- 学生端故障图片上传和工单图片预览
- 宿管审核工单、分配维修人员、驳回工单、管理宿舍、发布公告
- 维修人员接单、提交维修结果、上传维修图片、查看个人统计
- 管理员查看统计概览、图表、用户列表、日志列表
- 管理端 CSV 导出、工单筛选、图片预览
- SQLite 自动建表和初始化数据
