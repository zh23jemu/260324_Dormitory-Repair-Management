INSERT INTO user (id, username, password, real_name, phone, role, work_type_code, status, created_at, updated_at) VALUES
(1, 'admin', '123456', '系统管理员', '13800000000', 'admin', NULL, 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(2, 'dorm01', '123456', '一号宿管', '13800000001', 'dorm_admin', NULL, 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(3, 'repair01', '123456', '维修员张工', '13800000002', 'repairer', 'electrician', 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(4, 'student01', '123456', '张三', '13800000003', 'student', NULL, 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(5, 'repair02', '123456', '维修员李工', '13800000004', 'repairer', 'carpenter', 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(6, 'student02', '123456', '李四', '13800000005', 'student', NULL, 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00');

INSERT INTO dorm_building (id, building_name, building_code, gender_type, floor_count, remark) VALUES
(1, '一号楼', 'B1', 'male', 6, '男生宿舍'),
(2, '二号楼', 'B2', 'female', 6, '女生宿舍');

INSERT INTO dorm_room (id, building_id, room_no, capacity, current_count, facility_desc, status, remark) VALUES
(1, 1, '101', 4, 2, '空调、书桌、热水器', 'enabled', ''),
(2, 1, '102', 4, 1, '空调、书桌、热水器', 'enabled', ''),
(3, 2, '201', 4, 1, '空调、书桌、热水器', 'enabled', ''),
(4, 2, '202', 4, 0, '空调、书桌、热水器', 'enabled', '');

INSERT INTO student_profile (id, user_id, student_no, gender, college, major, class_name, building_id, room_id, bed_no, created_at, updated_at) VALUES
(1, 4, '20260001', 'male', '信息工程学院', '软件工程', '软工2201', 1, 1, '1', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(2, 6, '20260002', 'female', '信息工程学院', '计算机科学与技术', '计科2202', 2, 3, '2', '2026-03-25 00:00:00', '2026-03-25 00:00:00');

INSERT INTO repair_type (id, type_name, sort_no, status) VALUES
(1, '水电', 1, 'enabled'),
(2, '家具', 2, 'enabled'),
(3, '门窗', 3, 'enabled'),
(4, '网络', 4, 'enabled'),
(5, '其他', 5, 'enabled');

INSERT INTO announcement (id, title, content, publisher_id, status, published_at, created_at) VALUES
(1, '宿舍报修系统启用通知', '系统已上线，学生可直接通过手机端提交报修申请。', 1, 'published', '2026-03-25 09:00:00', '2026-03-25 09:00:00'),
(2, '本周水电巡检通知', '周三下午将对一号楼和二号楼开展统一巡检，请宿舍同学提前做好准备。', 2, 'published', '2026-03-25 10:00:00', '2026-03-25 10:00:00');

INSERT INTO repair_order (id, order_no, student_id, building_id, room_id, repair_type_id, title, description, expect_time, status, reject_reason, assigned_repairer_id, submitted_at, assigned_at, completed_at, created_at, updated_at) VALUES
(1, 'BX202603250001', 4, 1, 1, 1, '宿舍水龙头漏水', '卫生间水龙头持续滴水，需要尽快处理。', '2026-03-26 10:00:00', 'pending_review', NULL, NULL, '2026-03-25 09:30:00', NULL, NULL, '2026-03-25 09:30:00', '2026-03-25 09:30:00'),
(2, 'BX202603250002', 6, 2, 3, 4, '宿舍网络不稳定', '晚上网络频繁断开，影响上网和学习。', '2026-03-26 18:00:00', 'rejected', '请补充具体出现问题的时间段和设备情况。', NULL, '2026-03-25 09:40:00', NULL, NULL, '2026-03-25 09:40:00', '2026-03-25 10:00:00'),
(3, 'BX202603250003', 4, 1, 2, 2, '桌椅螺丝松动', '书桌晃动明显，怀疑螺丝脱落。', '2026-03-26 14:00:00', 'pending_accept', NULL, 3, '2026-03-25 10:10:00', '2026-03-25 10:30:00', NULL, '2026-03-25 10:10:00', '2026-03-25 10:30:00'),
(4, 'BX202603250004', 6, 2, 3, 3, '宿舍门锁卡顿', '门锁开启困难，需要检查。', '2026-03-26 16:00:00', 'processing', NULL, 5, '2026-03-25 10:20:00', '2026-03-25 10:35:00', NULL, '2026-03-25 10:20:00', '2026-03-25 11:00:00'),
(5, 'BX202603250005', 4, 1, 1, 1, '热水器不加热', '晚上洗澡时发现热水器没有热水。', '2026-03-26 20:00:00', 'pending_rating', NULL, 3, '2026-03-25 11:10:00', '2026-03-25 11:25:00', '2026-03-25 12:10:00', '2026-03-25 11:10:00', '2026-03-25 12:10:00'),
(6, 'BX202603250006', 6, 2, 3, 2, '衣柜柜门松动', '柜门铰链松动，开关时有异响。', '2026-03-26 09:00:00', 'completed', NULL, 5, '2026-03-25 08:50:00', '2026-03-25 09:10:00', '2026-03-25 10:15:00', '2026-03-25 08:50:00', '2026-03-25 10:40:00');

INSERT INTO repair_order_image (id, repair_order_id, image_type, file_path, created_at) VALUES
(1, 5, 'result', '/uploads/demo-fix-1.jpg', '2026-03-25 12:10:00'),
(2, 6, 'result', '/uploads/demo-fix-2.jpg', '2026-03-25 10:15:00');

INSERT INTO repair_flow (id, repair_order_id, operator_id, from_status, to_status, remark, created_at) VALUES
(1, 1, 4, NULL, 'pending_review', '学生提交报修申请', '2026-03-25 09:30:00'),
(2, 2, 6, NULL, 'pending_review', '学生提交报修申请', '2026-03-25 09:40:00'),
(3, 2, 2, 'pending_review', 'rejected', '请补充具体出现问题的时间段和设备情况。', '2026-03-25 10:00:00'),
(4, 3, 4, NULL, 'pending_review', '学生提交报修申请', '2026-03-25 10:10:00'),
(5, 3, 2, 'pending_review', 'pending_accept', '宿管分配维修人员', '2026-03-25 10:30:00'),
(6, 4, 6, NULL, 'pending_review', '学生提交报修申请', '2026-03-25 10:20:00'),
(7, 4, 2, 'pending_review', 'pending_accept', '宿管分配维修人员', '2026-03-25 10:35:00'),
(8, 4, 5, 'pending_accept', 'processing', '维修人员接单', '2026-03-25 11:00:00'),
(9, 5, 4, NULL, 'pending_review', '学生提交报修申请', '2026-03-25 11:10:00'),
(10, 5, 2, 'pending_review', 'pending_accept', '宿管分配维修人员', '2026-03-25 11:25:00'),
(11, 5, 3, 'pending_accept', 'processing', '维修人员接单', '2026-03-25 11:40:00'),
(12, 5, 3, 'processing', 'pending_rating', '维修完成，等待评价', '2026-03-25 12:10:00'),
(13, 6, 6, NULL, 'pending_review', '学生提交报修申请', '2026-03-25 08:50:00'),
(14, 6, 2, 'pending_review', 'pending_accept', '宿管分配维修人员', '2026-03-25 09:10:00'),
(15, 6, 5, 'pending_accept', 'processing', '维修人员接单', '2026-03-25 09:25:00'),
(16, 6, 5, 'processing', 'pending_rating', '维修完成，等待评价', '2026-03-25 10:15:00'),
(17, 6, 6, 'pending_rating', 'completed', '学生完成评价', '2026-03-25 10:40:00');

INSERT INTO repair_feedback (id, repair_order_id, repairer_id, result_desc, materials_used, finish_time, created_at) VALUES
(1, 5, 3, '已更换热水器温控模块，设备恢复正常加热。', '温控模块、绝缘胶带', '2026-03-25 12:10:00', '2026-03-25 12:10:00'),
(2, 6, 5, '已加固衣柜铰链并重新校正柜门。', '螺丝、铰链垫片', '2026-03-25 10:15:00', '2026-03-25 10:15:00');

INSERT INTO repair_rating (id, repair_order_id, student_id, score, content, created_at) VALUES
(1, 6, 6, 5, '处理速度很快，维修后使用正常。', '2026-03-25 10:40:00');

INSERT INTO sys_dict (id, dict_type, dict_code, dict_name, sort_no, status) VALUES
(1, 'repair_priority', 'normal', '普通', 1, 'enabled'),
(2, 'repair_priority', 'urgent', '紧急', 2, 'enabled'),
(3, 'announcement_type', 'notice', '通知公告', 1, 'enabled'),
(4, 'repair_work_type', 'electrician', '水电维修', 1, 'enabled'),
(5, 'repair_work_type', 'carpenter', '家具维修', 2, 'enabled'),
(6, 'repair_work_type', 'network', '网络维修', 3, 'enabled'),
(7, 'rating_indicator', 'response_speed', '响应速度', 1, 'enabled'),
(8, 'rating_indicator', 'service_attitude', '服务态度', 2, 'enabled'),
(9, 'rating_indicator', 'repair_quality', '维修质量', 3, 'enabled');

INSERT INTO sys_log (id, user_id, module_name, operation_type, operation_desc, ip, created_at) VALUES
(1, 1, '系统', '初始化', '系统完成基础数据初始化', '127.0.0.1', '2026-03-25 09:00:00'),
(2, 4, '报修申请', '新增', '提交报修工单: 1', '127.0.0.1', '2026-03-25 09:30:00'),
(3, 2, '工单管理', '驳回', '驳回工单: 2', '127.0.0.1', '2026-03-25 10:00:00'),
(4, 2, '工单管理', '分配', '分配工单给维修人员: 3', '127.0.0.1', '2026-03-25 10:30:00'),
(5, 5, '维修处理', '接单', '接单工单: 4', '127.0.0.1', '2026-03-25 11:00:00'),
(6, 3, '维修处理', '完成', '提交维修结果: 5', '127.0.0.1', '2026-03-25 12:10:00'),
(7, 6, '工单评价', '新增', '评价工单: 6', '127.0.0.1', '2026-03-25 10:40:00');
