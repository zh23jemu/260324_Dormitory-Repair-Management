INSERT INTO user (id, username, password, real_name, phone, avatar, role, work_type_code, password_question, password_answer, status, created_at, updated_at) VALUES
(1, 'admin', '123456', '系统管理员', '13800000000', NULL, 'admin', NULL, NULL, NULL, 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(2, 'dorm01', '123456', '一号宿管', '13800000001', NULL, 'dorm_admin', NULL, '你的工号后四位是什么？', '0001', 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(3, 'repair01', '123456', '维修员张工', '13800000002', NULL, 'repairer', 'electrician', '你的工号后四位是什么？', '0002', 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(4, 'student01', '123456', '张三', '13800000003', NULL, 'student', NULL, '你的学号后四位是什么？', '0001', 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(5, 'repair02', '123456', '维修员李工', '13800000004', NULL, 'repairer', 'carpenter', '你的工号后四位是什么？', '0004', 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(6, 'student02', '123456', '李四', '13800000005', NULL, 'student', NULL, '你的学号后四位是什么？', '0002', 'enabled', '2026-03-25 00:00:00', '2026-03-25 00:00:00');

INSERT INTO dorm_building (id, building_name, building_code, gender_type, floor_count, remark) VALUES
(1, '一号楼', 'B1', 'male', 6, '男生宿舍'),
(2, '二号楼', 'B2', 'female', 6, '女生宿舍');

INSERT INTO dorm_room (id, building_id, room_no, capacity, current_count, facility_desc, status, remark) VALUES
(1, 1, '101', 4, 2, '空调、书桌、热水器', 'enabled', ''),
(2, 1, '102', 4, 1, '空调、书桌、热水器', 'enabled', ''),
(3, 2, '201', 4, 1, '空调、书桌、热水器', 'enabled', ''),
(4, 2, '202', 4, 0, '空调、书桌、热水器', 'enabled', '');

INSERT INTO dorm_facility (id, room_id, facility_name, facility_type, brand, model_number, purchase_date, status, remark, created_at, updated_at) VALUES
(1, 1, '卫生间水龙头', '水电设施', '九牧', 'FM-01', '2025-09-01', 'fault', '存在漏水情况', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(2, 1, '热水器', '卫浴设备', '海尔', 'RS-60', '2025-09-01', 'normal', '', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(3, 2, '书桌', '家具', '校园家具厂', 'Desk-A', '2025-09-01', 'normal', '', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(4, 3, '门锁', '门窗', '固特', 'Lock-11', '2025-09-01', 'fault', '开关卡顿', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(5, 3, '校园网面板', '网络设备', '华三', 'Net-P1', '2025-09-01', 'normal', '', '2026-03-25 00:00:00', '2026-03-25 00:00:00');

INSERT INTO student_profile (id, user_id, student_no, gender, college, major, class_name, building_id, room_id, bed_no, created_at, updated_at) VALUES
(1, 4, '20260001', 'male', '信息工程学院', '软件工程', '软工2201', 1, 1, '1', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(2, 6, '20260002', 'female', '信息工程学院', '计算机科学与技术', '计科2202', 2, 3, '2', '2026-03-25 00:00:00', '2026-03-25 00:00:00');

INSERT INTO repair_type (id, type_name, sort_no, status) VALUES
(1, '水电', 1, 'enabled'),
(2, '家具', 2, 'enabled'),
(3, '门窗', 3, 'enabled'),
(4, '网络', 4, 'enabled'),
(5, '其他', 5, 'enabled');

INSERT INTO announcement (id, title, content, image_path, publisher_id, status, published_at, created_at) VALUES
(1, '宿舍报修系统启用通知', '系统已上线，学生可直接通过手机端提交报修申请。', NULL, 1, 'published', '2026-03-25 09:00:00', '2026-03-25 09:00:00'),
(2, '本周水电巡检通知', '周三下午将对一号楼和二号楼开展统一巡检，请宿舍同学提前做好准备。', NULL, 2, 'published', '2026-03-25 10:00:00', '2026-03-25 10:00:00');

INSERT INTO repair_order (id, order_no, student_id, building_id, room_id, facility_id, repair_type_id, title, description, expect_time, status, reject_reason, assigned_repairer_id, submitted_at, assigned_at, completed_at, created_at, updated_at) VALUES
(1, 'BX202603250001', 4, 1, 1, 1, 1, '宿舍水龙头漏水', '卫生间水龙头持续滴水，需要尽快处理。', '2026-03-26 10:00:00', 'pending_review', NULL, NULL, '2026-03-25 09:30:00', NULL, NULL, '2026-03-25 09:30:00', '2026-03-25 09:30:00'),
(2, 'BX202603250002', 6, 2, 3, 5, 4, '宿舍网络不稳定', '晚上网络频繁断开，影响上网和学习。', '2026-03-26 18:00:00', 'rejected', '请补充具体出现问题的时间段和设备情况。', NULL, '2026-03-25 09:40:00', NULL, NULL, '2026-03-25 09:40:00', '2026-03-25 10:00:00'),
(3, 'BX202603250003', 4, 1, 2, 3, 2, '桌椅螺丝松动', '书桌晃动明显，怀疑螺丝脱落。', '2026-03-26 14:00:00', 'pending_accept', NULL, 3, '2026-03-25 10:10:00', '2026-03-25 10:30:00', NULL, '2026-03-25 10:10:00', '2026-03-25 10:30:00'),
(4, 'BX202603250004', 6, 2, 3, 4, 3, '宿舍门锁卡顿', '门锁开启困难，需要检查。', '2026-03-26 16:00:00', 'processing', NULL, 5, '2026-03-25 10:20:00', '2026-03-25 10:35:00', NULL, '2026-03-25 10:20:00', '2026-03-25 11:00:00'),
(5, 'BX202603250005', 4, 1, 1, 2, 1, '热水器不加热', '晚上洗澡时发现热水器没有热水。', '2026-03-26 20:00:00', 'pending_rating', NULL, 3, '2026-03-25 11:10:00', '2026-03-25 11:25:00', '2026-03-25 12:10:00', '2026-03-25 11:10:00', '2026-03-25 12:10:00'),
(6, 'BX202603250006', 6, 2, 3, NULL, 2, '衣柜柜门松动', '柜门铰链松动，开关时有异响。', '2026-03-26 09:00:00', 'completed', NULL, 5, '2026-03-25 08:50:00', '2026-03-25 09:10:00', '2026-03-25 10:15:00', '2026-03-25 08:50:00', '2026-03-25 10:40:00');

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

INSERT INTO repair_material (id, material_name, material_type, unit, stock_qty, warning_qty, remark, created_at, updated_at) VALUES
(1, '温控模块', '水电耗材', '个', 20, 5, '热水器维修常用', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(2, '绝缘胶带', '水电耗材', '卷', 50, 10, '线路绝缘处理', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(3, '螺丝', '家具耗材', '盒', 30, 5, '家具门窗维修常用', '2026-03-25 00:00:00', '2026-03-25 00:00:00'),
(4, '网线模块', '网络耗材', '个', 15, 3, '宿舍网络面板维护', '2026-03-25 00:00:00', '2026-03-25 00:00:00');

INSERT INTO repair_rating (id, repair_order_id, student_id, score, content, created_at) VALUES
(1, 6, 6, 5, '处理速度很快，维修后使用正常。', '2026-03-25 10:40:00');

INSERT INTO repair_resource (id, title, category, summary, content, cover_image, sort_no, status, publisher_id, created_at, updated_at) VALUES
(1, '宿舍水龙头漏水自助排查', '常见故障', '提供简单的水龙头漏水自查步骤与报修建议。', '1. 检查是否为阀芯松动；2. 关闭总阀避免继续漏水；3. 如持续漏水请拍照并提交报修。', NULL, 1, 'published', 1, '2026-03-25 09:20:00', '2026-03-25 09:20:00'),
(2, '热水器报修须知', '报修须知', '说明热水器报修前需要确认的内容。', '请先确认宿舍供电正常，再检查热水器开关状态；报修时建议填写故障出现时间、现象和宿舍号。', NULL, 2, 'published', 1, '2026-03-25 09:30:00', '2026-03-25 09:30:00'),
(3, '宿舍门锁使用与维护', '设施说明', '介绍门锁卡顿时的临时处理方式。', '可先检查门框是否有异物阻挡，避免强力扭转钥匙；若锁芯卡死，请立即申请报修。', NULL, 3, 'published', 2, '2026-03-25 10:10:00', '2026-03-25 10:10:00');

INSERT INTO service_message (id, student_id, title, content, image_path, status, reply_content, replied_by, replied_at, created_at, updated_at) VALUES
(1, 4, '建议增加维修进度提醒', '希望系统在维修员接单和维修完成时推送更明显的提示。', NULL, 'replied', '已记录，后续版本会继续优化消息提醒体验。', 1, '2026-03-25 12:30:00', '2026-03-25 12:00:00', '2026-03-25 12:30:00'),
(2, 6, '报修图片上传建议', '上传图片时希望支持更多张数和更清晰的预览。', NULL, 'pending', NULL, NULL, NULL, '2026-03-25 12:40:00', '2026-03-25 12:40:00');

INSERT INTO forum_post (id, student_id, title, content, image_path, status, created_at, updated_at) VALUES
(1, 4, '宿舍热水器报修体验分享', '昨天提交报修后，维修员当天就完成处理，整体进度在系统里看得比较清楚。', NULL, 'published', '2026-03-25 13:00:00', '2026-03-25 13:00:00'),
(2, 6, '建议报修时补充故障照片', '上传故障照片后，宿管和维修员判断问题会更快，建议大家尽量拍清楚。', NULL, 'published', '2026-03-25 13:30:00', '2026-03-25 13:30:00');

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
