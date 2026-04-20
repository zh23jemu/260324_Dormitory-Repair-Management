PRAGMA foreign_keys = OFF;

DROP TABLE IF EXISTS service_message;
DROP TABLE IF EXISTS forum_post;
DROP TABLE IF EXISTS repair_resource;
DROP TABLE IF EXISTS repair_rating;
DROP TABLE IF EXISTS repair_feedback;
DROP TABLE IF EXISTS material_usage;
DROP TABLE IF EXISTS repair_material;
DROP TABLE IF EXISTS repair_flow;
DROP TABLE IF EXISTS repair_order_image;
DROP TABLE IF EXISTS repair_order;
DROP TABLE IF EXISTS dorm_facility;
DROP TABLE IF EXISTS repair_type;
DROP TABLE IF EXISTS student_profile;
DROP TABLE IF EXISTS dorm_room;
DROP TABLE IF EXISTS dorm_building;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS sys_log;
DROP TABLE IF EXISTS sys_dict;
DROP TABLE IF EXISTS user;

PRAGMA foreign_keys = ON;

CREATE TABLE user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    real_name TEXT NOT NULL,
    phone TEXT,
    avatar TEXT,
    role TEXT NOT NULL,
    work_type_code TEXT,
    status TEXT NOT NULL DEFAULT 'enabled',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE dorm_building (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    building_name TEXT NOT NULL,
    building_code TEXT NOT NULL UNIQUE,
    gender_type TEXT,
    floor_count INTEGER DEFAULT 0,
    remark TEXT
);

CREATE TABLE dorm_room (
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

CREATE TABLE dorm_facility (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    room_id INTEGER NOT NULL,
    facility_name TEXT NOT NULL,
    facility_type TEXT NOT NULL,
    brand TEXT,
    model_number TEXT,
    purchase_date TEXT,
    status TEXT NOT NULL DEFAULT 'normal',
    remark TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (room_id) REFERENCES dorm_room(id)
);

CREATE TABLE student_profile (
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

CREATE TABLE repair_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type_name TEXT NOT NULL UNIQUE,
    sort_no INTEGER NOT NULL UNIQUE,
    status TEXT NOT NULL DEFAULT 'enabled'
);

CREATE TABLE repair_order (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no TEXT NOT NULL UNIQUE,
    student_id INTEGER NOT NULL,
    building_id INTEGER,
    room_id INTEGER,
    facility_id INTEGER,
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
    FOREIGN KEY (facility_id) REFERENCES dorm_facility(id),
    FOREIGN KEY (repair_type_id) REFERENCES repair_type(id),
    FOREIGN KEY (assigned_repairer_id) REFERENCES user(id)
);

CREATE TABLE repair_order_image (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    repair_order_id INTEGER NOT NULL,
    image_type TEXT NOT NULL,
    file_path TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (repair_order_id) REFERENCES repair_order(id)
);

CREATE TABLE repair_flow (
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

CREATE TABLE repair_feedback (
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

CREATE TABLE repair_material (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    material_name TEXT NOT NULL UNIQUE,
    material_type TEXT,
    unit TEXT NOT NULL,
    stock_qty REAL NOT NULL DEFAULT 0,
    warning_qty REAL NOT NULL DEFAULT 0,
    remark TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE material_usage (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    repair_order_id INTEGER NOT NULL,
    repairer_id INTEGER NOT NULL,
    material_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (repair_order_id) REFERENCES repair_order(id),
    FOREIGN KEY (repairer_id) REFERENCES user(id),
    FOREIGN KEY (material_id) REFERENCES repair_material(id)
);

CREATE TABLE repair_rating (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    repair_order_id INTEGER NOT NULL UNIQUE,
    student_id INTEGER NOT NULL,
    score INTEGER NOT NULL,
    content TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (repair_order_id) REFERENCES repair_order(id),
    FOREIGN KEY (student_id) REFERENCES user(id)
);

CREATE TABLE announcement (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    image_path TEXT,
    publisher_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'published',
    published_at TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (publisher_id) REFERENCES user(id)
);

CREATE TABLE forum_post (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    image_path TEXT,
    status TEXT NOT NULL DEFAULT 'published',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES user(id)
);

CREATE TABLE repair_resource (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    summary TEXT,
    content TEXT NOT NULL,
    cover_image TEXT,
    sort_no INTEGER NOT NULL DEFAULT 1,
    status TEXT NOT NULL DEFAULT 'published',
    publisher_id INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (publisher_id) REFERENCES user(id)
);

CREATE TABLE service_message (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    image_path TEXT,
    status TEXT NOT NULL DEFAULT 'pending',
    reply_content TEXT,
    replied_by INTEGER,
    replied_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES user(id),
    FOREIGN KEY (replied_by) REFERENCES user(id)
);

CREATE TABLE sys_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    module_name TEXT NOT NULL,
    operation_type TEXT NOT NULL,
    operation_desc TEXT NOT NULL,
    ip TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE sys_dict (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dict_type TEXT NOT NULL,
    dict_code TEXT NOT NULL,
    dict_name TEXT NOT NULL,
    sort_no INTEGER DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'enabled'
);

CREATE INDEX idx_user_role ON user(role);
CREATE INDEX idx_repair_order_status ON repair_order(status);
CREATE INDEX idx_repair_order_student_id ON repair_order(student_id);
CREATE INDEX idx_repair_order_repairer_id ON repair_order(assigned_repairer_id);
CREATE INDEX idx_repair_order_facility_id ON repair_order(facility_id);
CREATE INDEX idx_repair_flow_order_id ON repair_flow(repair_order_id);
CREATE INDEX idx_material_usage_order_id ON material_usage(repair_order_id);
CREATE INDEX idx_dorm_facility_room_id ON dorm_facility(room_id);
CREATE INDEX idx_resource_status ON repair_resource(status);
CREATE INDEX idx_service_message_student_id ON service_message(student_id);
CREATE INDEX idx_forum_post_status ON forum_post(status);
