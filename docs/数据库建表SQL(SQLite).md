# 数据库建表 SQL（SQLite）

本文档给出高校宿舍报修管理系统的 `SQLite` 建表草案，适用于当前单体毕业设计项目。  
设计原则为结构简单、功能完整、便于实现和答辩演示。

## 1. 说明

- 数据库文件建议命名为 `dorm_repair.db`
- 主键统一使用 `INTEGER PRIMARY KEY AUTOINCREMENT`
- 时间字段统一使用 `TEXT` 存储，格式建议为 `yyyy-MM-dd HH:mm:ss`
- 状态字段使用 `TEXT` 或 `INTEGER` 均可，当前文档以 `TEXT` 为主，便于调试和查看
- 图片使用文件路径存储，不直接存储二进制内容

## 2. 建表 SQL

```sql
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    real_name TEXT NOT NULL,
    phone TEXT,
    role TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'enabled',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS dorm_building (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    building_name TEXT NOT NULL,
    building_code TEXT NOT NULL UNIQUE,
    gender_type TEXT,
    floor_count INTEGER DEFAULT 0,
    remark TEXT
);

CREATE TABLE IF NOT EXISTS dorm_room (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    building_id INTEGER NOT NULL,
    room_no TEXT NOT NULL,
    capacity INTEGER DEFAULT 0,
    current_count INTEGER DEFAULT 0,
    facility_desc TEXT,
    status TEXT NOT NULL DEFAULT 'enabled',
    remark TEXT,
    FOREIGN KEY (building_id) REFERENCES dorm_building(id)
);

CREATE TABLE IF NOT EXISTS student_profile (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    student_no TEXT NOT NULL UNIQUE,
    gender TEXT,
    college TEXT,
    major TEXT,
    class_name TEXT,
    building_id INTEGER,
    room_id INTEGER,
    bed_no TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (building_id) REFERENCES dorm_building(id),
    FOREIGN KEY (room_id) REFERENCES dorm_room(id)
);

CREATE TABLE IF NOT EXISTS repair_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type_name TEXT NOT NULL UNIQUE,
    sort_no INTEGER DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'enabled'
);

CREATE TABLE IF NOT EXISTS repair_order (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no TEXT NOT NULL UNIQUE,
    student_id INTEGER NOT NULL,
    building_id INTEGER,
    room_id INTEGER,
    repair_type_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    expect_time TEXT,
    status TEXT NOT NULL,
    reject_reason TEXT,
    assigned_repairer_id INTEGER,
    submitted_at TEXT NOT NULL,
    assigned_at TEXT,
    completed_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES user(id),
    FOREIGN KEY (building_id) REFERENCES dorm_building(id),
    FOREIGN KEY (room_id) REFERENCES dorm_room(id),
    FOREIGN KEY (repair_type_id) REFERENCES repair_type(id),
    FOREIGN KEY (assigned_repairer_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS repair_order_image (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    repair_order_id INTEGER NOT NULL,
    image_type TEXT NOT NULL,
    file_path TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (repair_order_id) REFERENCES repair_order(id)
);

CREATE TABLE IF NOT EXISTS repair_flow (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    repair_order_id INTEGER NOT NULL,
    operator_id INTEGER NOT NULL,
    from_status TEXT,
    to_status TEXT NOT NULL,
    remark TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (repair_order_id) REFERENCES repair_order(id),
    FOREIGN KEY (operator_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS repair_feedback (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    repair_order_id INTEGER NOT NULL UNIQUE,
    repairer_id INTEGER NOT NULL,
    result_desc TEXT NOT NULL,
    materials_used TEXT,
    finish_time TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (repair_order_id) REFERENCES repair_order(id),
    FOREIGN KEY (repairer_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS repair_rating (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    repair_order_id INTEGER NOT NULL UNIQUE,
    student_id INTEGER NOT NULL,
    score INTEGER NOT NULL,
    content TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (repair_order_id) REFERENCES repair_order(id),
    FOREIGN KEY (student_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS announcement (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    publisher_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'published',
    published_at TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (publisher_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS sys_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    module_name TEXT NOT NULL,
    operation_type TEXT NOT NULL,
    operation_desc TEXT NOT NULL,
    ip TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS sys_dict (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dict_type TEXT NOT NULL,
    dict_code TEXT NOT NULL,
    dict_name TEXT NOT NULL,
    sort_no INTEGER DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'enabled'
);
```

## 3. 建议索引

```sql
CREATE INDEX IF NOT EXISTS idx_user_role ON user(role);
CREATE INDEX IF NOT EXISTS idx_student_profile_room_id ON student_profile(room_id);
CREATE INDEX IF NOT EXISTS idx_dorm_room_building_id ON dorm_room(building_id);
CREATE INDEX IF NOT EXISTS idx_repair_order_student_id ON repair_order(student_id);
CREATE INDEX IF NOT EXISTS idx_repair_order_status ON repair_order(status);
CREATE INDEX IF NOT EXISTS idx_repair_order_repairer_id ON repair_order(assigned_repairer_id);
CREATE INDEX IF NOT EXISTS idx_repair_order_type_id ON repair_order(repair_type_id);
CREATE INDEX IF NOT EXISTS idx_repair_flow_order_id ON repair_flow(repair_order_id);
CREATE INDEX IF NOT EXISTS idx_repair_order_image_order_id ON repair_order_image(repair_order_id);
CREATE INDEX IF NOT EXISTS idx_announcement_status ON announcement(status);
CREATE INDEX IF NOT EXISTS idx_sys_log_user_id ON sys_log(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_dict_type ON sys_dict(dict_type);
```

## 4. 初始化建议数据

### 4.1 用户角色

当前项目为简化设计，角色直接保存在 `user.role` 字段中，推荐固定值如下：

- `student`
- `dorm_admin`
- `repairer`
- `admin`

### 4.2 工单状态

推荐固定状态值如下：

- `pending_review`
- `rejected`
- `pending_accept`
- `processing`
- `pending_rating`
- `completed`

如果前端需要中文展示，可在前端做映射：

- `pending_review` -> 待审核
- `rejected` -> 已驳回
- `pending_accept` -> 待接单
- `processing` -> 处理中
- `pending_rating` -> 待评价
- `completed` -> 已完成

### 4.3 报修类型初始化

```sql
INSERT INTO repair_type (type_name, sort_no, status) VALUES
('水电', 1, 'enabled'),
('家具', 2, 'enabled'),
('门窗', 3, 'enabled'),
('网络', 4, 'enabled'),
('其他', 5, 'enabled');
```

### 4.4 基础字典初始化示例

```sql
INSERT INTO sys_dict (dict_type, dict_code, dict_name, sort_no, status) VALUES
('repair_priority', 'normal', '普通', 1, 'enabled'),
('repair_priority', 'urgent', '紧急', 2, 'enabled'),
('announcement_type', 'notice', '通知公告', 1, 'enabled');
```

## 5. 初始管理员建议

```sql
INSERT INTO user (username, password, real_name, phone, role, status, created_at, updated_at)
VALUES ('admin', '123456', '系统管理员', '13800000000', 'admin', 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00');
```

说明：

- 真实项目中密码应加密存储
- 作为毕业设计初版，可以先使用明文或简单加密，后续再替换为哈希方案

## 6. 表关系说明

- `user` 与 `student_profile`：一对一
- `dorm_building` 与 `dorm_room`：一对多
- `dorm_room` 与 `student_profile`：一对多
- `repair_order` 与 `repair_order_image`：一对多
- `repair_order` 与 `repair_flow`：一对多
- `repair_order` 与 `repair_feedback`：一对一
- `repair_order` 与 `repair_rating`：一对一

## 7. 实现建议

- `repair_order`、`repair_flow`、`repair_feedback`、`repair_rating` 是核心业务表，应优先实现
- 首版如需进一步简化，可暂不实现 `sys_dict` 的通用化后台管理，仅保留基础数据表
- 图片上传后仅保存文件路径，不在数据库中存储二进制内容
- 导出功能首版可以先导出为 CSV，避免引入复杂 Excel 依赖
