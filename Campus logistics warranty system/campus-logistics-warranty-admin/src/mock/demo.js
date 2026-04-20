// 演示模式开关：仅当启动命令中设置 VITE_DEMO_MODE=true 时启用。
// 该文件用于在不启动后端和 MySQL 的情况下，为页面提供一组稳定的模拟数据。
export const isDemoMode = import.meta.env.VITE_DEMO_MODE === 'true'

const demoImage = 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=800&q=80'

const users = [
  { id: 1, name: '系统管理员', username: 'admin', phone: '13800005678', avatar: demoImage, gender: '男', role: 'admin', tags: '', email: 'admin@example.com', status: 1, createTime: '2026-04-01 09:00:00' },
  { id: 2, name: '张同学', username: 'student01', phone: '13800001234', avatar: demoImage, gender: '男', role: 'user', tags: '', email: 'student@example.com', status: 1, createTime: '2026-04-02 10:00:00' },
  { id: 3, name: '王师傅', username: 'worker01', phone: '13800009876', avatar: demoImage, gender: '男', role: 'worker', tags: '水电维修,空调维修,门窗维修', email: 'worker@example.com', status: 1, createTime: '2026-04-03 11:00:00' },
  { id: 4, name: '李师傅', username: 'worker02', phone: '13900008888', avatar: demoImage, gender: '女', role: 'worker', tags: '网络维修,家具维修', email: 'worker2@example.com', status: 1, createTime: '2026-04-04 11:30:00' }
]

const repairTypes = [
  { id: 1, name: '水电维修', repairTypeName: '水电维修', description: '水管、灯具、插座等维修', createTime: '2026-04-01 09:10:00' },
  { id: 2, name: '空调/风扇维修', repairTypeName: '空调/风扇维修', description: '空调、风扇、遥控器故障', createTime: '2026-04-01 09:20:00' },
  { id: 3, name: '网络维修', repairTypeName: '网络维修', description: '宿舍网络、网口故障', createTime: '2026-04-01 09:30:00' }
]

const repairRecords = [
  { id: 1, userId: 2, userName: '张同学', name: '宿舍灯管不亮', typeId: 1, description: '寝室主灯闪烁后无法正常点亮，需要尽快检修。', location: '1号楼 202', phone: '13800001234', images: demoImage, img: demoImage, status: '待处理', workerId: 3, workerName: '王师傅', isCharge: 0, price: 0, remark: '优先安排晚间维修', createTime: '2026-04-10 09:20:00' },
  { id: 2, userId: 2, userName: '张同学', name: '空调制冷效果差', typeId: 2, description: '空调运行正常但基本不制冷，可能需要清洗或加氟。', location: '2号楼 305', phone: '13800001234', images: demoImage, img: demoImage, status: '处理中', workerId: 3, workerName: '王师傅', isCharge: 1, price: 35, remark: '已派单', createTime: '2026-04-11 14:35:00' },
  { id: 3, userId: 2, userName: '张同学', name: '网口无法连接', typeId: 3, description: '电脑插网线后无法获取 IP，疑似墙面网口故障。', location: '3号楼 418', phone: '13800001234', images: demoImage, img: demoImage, status: '已完成', workerId: 4, workerName: '李师傅', isCharge: 0, price: 0, remark: '已更换模块', createTime: '2026-04-12 16:10:00' }
]

const notices = [
  { id: 1, title: '宿舍维修服务时间调整通知', content: '本周起宿舍维修服务时间调整为每日 8:30-21:00，紧急故障可联系宿管登记。', img: demoImage, status: 1, createTime: '2026-04-09 09:00:00' },
  { id: 2, title: '春季用电安全提醒', content: '请同学们注意宿舍用电安全，禁止使用大功率电器，发现线路异常请及时报修。', img: demoImage, status: 1, createTime: '2026-04-10 10:00:00' }
]

const resources = [
  { id: 1, title: '宿舍漏水如何快速处理', name: '宿舍漏水如何快速处理', category: '常见故障', content: '发现漏水后请先关闭附近阀门，拍照记录现场，再提交报修申请。', cover: demoImage, img: demoImage, createTime: '2026-04-08 08:30:00' },
  { id: 2, title: '空调遥控器无响应排查', name: '空调遥控器无响应排查', category: '自助排查', content: '可先检查电池、模式设置和空调电源，再联系维修人员处理。', cover: demoImage, img: demoImage, createTime: '2026-04-08 09:30:00' }
]

const posts = [
  { id: 1, title: '报修后一般多久处理？', content: '分享一下最近几次宿舍报修的处理速度。', userName: '张同学', img: demoImage, createTime: '2026-04-12 12:00:00' }
]

const messages = [
  { id: 1, title: '建议增加夜间紧急维修入口', content: '夜间突发漏水时希望能有更明显的紧急入口。', userName: '张同学', reply: '已收到，后续会优化入口展示。', createTime: '2026-04-13 12:30:00' }
]

const dormBuildings = [
  { id: 1, name: '1号楼', buildingName: '1号楼', address: '东区', remark: '男生宿舍' },
  { id: 2, name: '2号楼', buildingName: '2号楼', address: '西区', remark: '女生宿舍' }
]

const dormRooms = [
  { id: 1, buildingId: 1, buildingName: '1号楼', name: '202', roomNo: '202', capacity: 4, currentCount: 4, remark: '四人间' },
  { id: 2, buildingId: 2, buildingName: '2号楼', name: '305', roomNo: '305', capacity: 4, currentCount: 3, remark: '四人间' }
]

const dormFacilities = [
  { id: 1, buildingName: '1号楼', roomNo: '202', name: 'LED 主灯', facilityName: 'LED 主灯', type: '照明', status: '正常', remark: '2026 年采购' },
  { id: 2, buildingName: '2号楼', roomNo: '305', name: '壁挂空调', facilityName: '壁挂空调', type: '空调', status: '待检修', remark: '制冷效果下降' }
]

function page(records) {
  return {
    records,
    total: records.length,
    size: 10,
    current: 1,
    pages: 1
  }
}

function success(data = true) {
  return { code: 200, message: '演示模式请求成功', data }
}

function findById(records, url) {
  const id = Number(String(url).split('/').pop())
  return records.find((item) => Number(item.id) === id) || records[0] || {}
}

// 根据接口地址返回模拟数据。只覆盖页面展示需要的核心接口，保存/删除类接口统一返回成功。
export function mockRequest(config) {
  const url = config.url || ''
  const method = String(config.method || 'get').toLowerCase()

  if (url.includes('/login')) return success('demo-token')
  if (url.includes('/user/getByToken')) return success(users[0])
  if (url.includes('/home/getCountData')) return success({ userCount: users.length, resourceCount: resources.length, repairRecordCount: repairRecords.length, postCount: posts.length })
  if (url.includes('/home/getProportion')) return success([
    { repairTypeName: '水电维修', recordCount: 8 },
    { repairTypeName: '空调/风扇维修', recordCount: 5 },
    { repairTypeName: '网络维修', recordCount: 3 }
  ])
  if (url.includes('/statistics/totalRepairCount')) return success({ total: repairRecords.length })
  if (url.includes('/statistics/repairStatusStatistics')) return success([{ name: '待处理', value: 1 }, { name: '处理中', value: 1 }, { name: '已完成', value: 1 }])
  if (url.includes('/statistics/repairTypeStatistics')) return success([{ name: '水电维修', value: 8 }, { name: '空调/风扇维修', value: 5 }, { name: '网络维修', value: 3 }])
  if (url.includes('/statistics/repairLocationStatistics')) return success([{ name: '1号楼', value: 6 }, { name: '2号楼', value: 4 }, { name: '3号楼', value: 2 }])

  if (url.includes('/user/list')) return success(page(users))
  if (url.includes('/user/getById')) return success(findById(users, url))
  if (url.includes('/repairType/list')) return success(page(repairTypes))
  if (url.includes('/repairType/getById')) return success(findById(repairTypes, url))
  if (url.includes('/repairRecord/list')) return success(page(repairRecords))
  if (url.includes('/repairRecord/getById')) return success(findById(repairRecords, url))
  if (url.includes('/repairRecordRating/list')) return success(page([{ id: 1, repairRecordId: 3, userName: '张同学', score: 5, content: '维修及时，服务态度很好。', createTime: '2026-04-13 18:00:00' }]))
  if (url.includes('/resource/list')) return success(page(resources))
  if (url.includes('/resource/getById')) return success(findById(resources, url))
  if (url.includes('/post/list')) return success(page(posts))
  if (url.includes('/post/getById')) return success(findById(posts, url))
  if (url.includes('/comment/list') || url.includes('/comment/getByPostId')) return success(page([{ id: 1, postId: 1, userName: '李同学', content: '我上次报修第二天就处理了。', createTime: '2026-04-12 13:00:00' }]))
  if (url.includes('/notice/list')) return success(page(notices))
  if (url.includes('/notice/getById')) return success(findById(notices, url))
  if (url.includes('/message/list')) return success(page(messages))
  if (url.includes('/message/getById')) return success(findById(messages, url))
  if (url.includes('/dormBuilding/list') || url.includes('/dormBuilding/all')) return success(url.includes('/all') ? dormBuildings : page(dormBuildings))
  if (url.includes('/dormBuilding/getById')) return success(findById(dormBuildings, url))
  if (url.includes('/dormRoom/list') || url.includes('/dormRoom/all')) return success(url.includes('/all') ? dormRooms : page(dormRooms))
  if (url.includes('/dormRoom/getById')) return success(findById(dormRooms, url))
  if (url.includes('/dormFacility/list') || url.includes('/dormFacility/all')) return success(url.includes('/all') ? dormFacilities : page(dormFacilities))
  if (url.includes('/dormFacility/getById')) return success(findById(dormFacilities, url))
  if (url.includes('/file/upload')) return success(demoImage)

  if (['post', 'put', 'delete'].includes(method) || url.includes('/save') || url.includes('/delete') || url.includes('/update')) {
    return success(true)
  }
  return success(page([]))
}
