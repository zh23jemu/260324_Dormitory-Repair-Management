<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from '@/store'
import { updateUser } from '@/api/user'
import { upload } from '@/api/file'
import { getRepairRecordList, saveRepairRecord } from '@/api/repairRecord'
import { saveRepairRecordRating } from '@/api/repairRecordRating'
import { getCollectList, deleteCollect } from '@/api/collect'

const router = useRouter()
const userStore = useStore()
const userInfo = ref({})
const editMode = ref(false)
const loading = ref(false)
const avatarLoading = ref(false)

// 当前选中的标签页
const activeTab = ref('user-info')

// 表单数据
const form = reactive({
  id: '',
  name: '',
  username: '',
  phone: '',
  email: '',
  sex: '',
  avatar: ''
})

// 报修列表相关
const repairLoading = ref(false)
const repairList = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 5,
  userId: null
})

// 收藏列表相关
const collectLoading = ref(false)
const collectList = ref([])
const collectTotal = ref(0)
const collectQueryParams = ref({
  pageNum: 1,
  pageSize: 5
})

// 评价相关
const ratingDialogVisible = ref(false)
const ratingForm = reactive({
  repairRecordId: null,
  rating: 5,
  comment: ''
})
const ratingLoading = ref(false)

// 状态对应的标签类型
const statusTagType = {
  '待处理': '',
  '处理中': 'warning',
  '已完成': 'success',
  '已取消': 'info'
}

// 获取用户信息
const getUserInfo = async () => {
  if (userStore.token) {
    userInfo.value = await userStore.getUserInfo()
    // 复制数据到表单
    Object.keys(form).forEach(key => {
      form[key] = userInfo.value[key]
    })
    
    // 设置查询参数中的用户ID
    queryParams.value.userId = userInfo.value.id
    
    // 加载报修列表
    if (activeTab.value === 'repair-list') {
      fetchRepairList()
    }
    
    // 加载收藏列表
    if (activeTab.value === 'collect-list') {
      fetchCollectList()
    }
  }
}

// 进入编辑模式
const startEdit = () => {
  editMode.value = true
}

// 取消编辑
const cancelEdit = () => {
  editMode.value = false
  // 恢复原始数据
  Object.keys(form).forEach(key => {
    form[key] = userInfo.value[key]
  })
}

// 保存用户信息
const saveUserInfo = async () => {
  loading.value = true
  try {
    const res = await updateUser(form)
    if (res.code === 200) {
      ElMessage.success('信息更新成功')
      editMode.value = false
      // 重新获取用户信息
      await getUserInfo()
    } else {
      ElMessage.error(res.msg || '更新失败')
    }
  } catch (error) {
    console.error('更新用户信息失败', error)
    ElMessage.error('更新失败')
  } finally {
    loading.value = false
  }
}

// 上传头像
const handleAvatarUpload = async (options) => {
  const file = options.file
  if (!file) return
  
  // 验证文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return
  }
  
  // 验证文件大小（限制为2MB）
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }
  
  avatarLoading.value = true
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await upload(formData)
    if (res.code === 200) {
      form.avatar = res.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传头像失败', error)
    ElMessage.error('上传失败')
  } finally {
    avatarLoading.value = false
  }
}

// 上传头像前的钩子
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('请上传图片文件')
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
  }
  
  return isImage && isLt2M
}

// 获取报修列表
const fetchRepairList = async () => {
  if (!userInfo.value.id) return
  
  repairLoading.value = true
  try {
    const res = await getRepairRecordList(queryParams.value)
    if (res.code === 200) {
      repairList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '获取报修列表失败')
    }
  } catch (error) {
    console.error('获取报修列表失败', error)
    ElMessage.error('获取报修列表失败')
  } finally {
    repairLoading.value = false
  }
}

// 获取收藏列表
const fetchCollectList = async () => {
  if (!userInfo.value.id) return
  
  collectLoading.value = true
  try {
    const res = await getCollectList(collectQueryParams.value)
    if (res.code === 200) {
      collectList.value = res.data.records || []
      collectTotal.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '获取收藏列表失败')
    }
  } catch (error) {
    console.error('获取收藏列表失败', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    collectLoading.value = false
  }
}

// 取消收藏
const cancelCollect = (collect) => {
  ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteCollect(collect.id)
      if (res.code === 200) {
        ElMessage.success('取消收藏成功')
        fetchCollectList()
      } else {
        ElMessage.error(res.msg || '取消收藏失败')
      }
    } catch (error) {
      console.error('取消收藏失败', error)
      ElMessage.error('取消收藏失败')
    }
  }).catch(() => {})
}

// 查看文章详情
const viewPostDetail = (id) => {
  router.push(`/post/detail/${id}`)
}

// 取消报修单
const cancelRepair = (repair) => {
  ElMessageBox.confirm('确定要取消该报修单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const data = {
        ...repair,
        status: '已取消'
      }
      
      const res = await saveRepairRecord(data)
      if (res.code === 200) {
        ElMessage.success('报修单已取消')
        fetchRepairList()
      } else {
        ElMessage.error(res.msg || '取消失败')
      }
    } catch (error) {
      console.error('取消报修单失败', error)
      ElMessage.error('取消失败')
    }
  }).catch(() => {})
}

// 查看报修详情
const viewRepairDetail = (id) => {
  router.push(`/repair/detail/${id}`)
}

// 创建新报修单
const createRepair = () => {
  router.push('/repair/create')
}

// 打开评价对话框
const openRatingDialog = (repair) => {
  ratingForm.repairRecordId = repair.id
  ratingForm.rating = 5
  ratingForm.comment = ''
  ratingDialogVisible.value = true
}

// 提交评价
const submitRating = async () => {
  ratingLoading.value = true
  try {
    const res = await saveRepairRecordRating(ratingForm)
    if (res.code === 200) {
      ElMessage.success('评价提交成功')
      ratingDialogVisible.value = false
      fetchRepairList()
    } else {
      ElMessage.error(res.msg || '评价提交失败')
    }
  } catch (error) {
    console.error('提交评价失败', error)
    ElMessage.error('评价提交失败')
  } finally {
    ratingLoading.value = false
  }
}

// 处理报修分页变化
const handleRepairCurrentChange = (page) => {
  queryParams.value.pageNum = page
  fetchRepairList()
}

// 处理报修每页显示数量变化
const handleRepairSizeChange = (size) => {
  queryParams.value.pageSize = size
  queryParams.value.pageNum = 1
  fetchRepairList()
}

// 处理收藏分页变化
const handleCollectCurrentChange = (page) => {
  collectQueryParams.value.pageNum = page
  fetchCollectList()
}

// 处理收藏每页显示数量变化
const handleCollectSizeChange = (size) => {
  collectQueryParams.value.pageSize = size
  collectQueryParams.value.pageNum = 1
  fetchCollectList()
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 监听标签页切换
const handleTabChange = (tab) => {
  if (tab === 'repair-list') {
    fetchRepairList()
  } else if (tab === 'collect-list') {
    fetchCollectList()
  }
}

onMounted(() => {
  getUserInfo()
})
</script>

<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>
      
      <div class="user-info">
        <div class="avatar-container">
          <el-avatar v-if="!editMode" :size="80" :src="userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          
          <el-upload
            v-else
            class="avatar-uploader"
            :show-file-list="false"
            action="#"
            :http-request="handleAvatarUpload"
            :before-upload="beforeAvatarUpload">
            <el-avatar 
              v-if="form.avatar" 
              :size="80" 
              :src="form.avatar" 
              class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            <el-icon v-if="avatarLoading" class="loading-icon"><Loading /></el-icon>
          </el-upload>
        </div>
        <h2>{{ userInfo.name || userInfo.username || '未登录' }}</h2>
      </div>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 个人信息标签页 -->
        <el-tab-pane label="个人资料" name="user-info">
          <div class="tab-header">
            <div></div>
            <div>
              <el-button v-if="!editMode" type="primary" @click="startEdit">编辑资料</el-button>
              <template v-else>
                <el-button @click="cancelEdit">取消</el-button>
                <el-button type="primary" :loading="loading" @click="saveUserInfo">保存</el-button>
              </template>
            </div>
          </div>
          
          <!-- 查看模式 -->
          <el-descriptions v-if="!editMode" title="个人信息" :column="1" border>
            <el-descriptions-item label="姓名">{{ userInfo.name || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ userInfo.username || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ userInfo.sex || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userInfo.phone || '暂无' }}</el-descriptions-item>
          </el-descriptions>
          
          <!-- 编辑模式 -->
          <el-form v-else label-width="80px" class="edit-form">
            <el-form-item label="姓名">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="form.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="form.sex">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 我的报修单标签页 -->
        <el-tab-pane label="我的报修单" name="repair-list">
          <div class="tab-header">
            <div></div>
            <el-button type="primary" @click="createRepair">
              <el-icon><Plus /></el-icon> 新建报修
            </el-button>
          </div>
          
          <div v-loading="repairLoading" class="repair-list">
            <el-empty v-if="repairList.length === 0" description="暂无报修记录">
              <el-button type="primary" @click="createRepair">立即报修</el-button>
            </el-empty>
            
            <template v-else>
              <el-table :data="repairList" style="width: 100%">
                <el-table-column prop="name" label="报修标题" show-overflow-tooltip />
                <el-table-column prop="location" label="维修地点" show-overflow-tooltip />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="statusTagType[row.status]" size="small">
                      {{ row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="收费" width="80">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.isCharge ? 'danger' : 'success'">
                      {{ row.isCharge ? '收费' : '免费' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="创建时间" width="150">
                  <template #default="{ row }">
                    {{ formatDateTime(row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <el-button
                      link
                      type="primary"
                      size="small"
                      @click="viewRepairDetail(row.id)"
                    >
                      查看
                    </el-button>
                    <el-button
                      v-if="row.status === '待处理'"
                      link
                      type="danger"
                      size="small"
                      @click="cancelRepair(row)"
                    >
                      取消
                    </el-button>
                    <el-button
                      v-if="row.status === '已完成'"
                      link
                      type="success"
                      size="small"
                      @click="openRatingDialog(row)"
                    >
                      评价
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="pagination">
                <el-pagination
                  v-model:current-page="queryParams.pageNum"
                  v-model:page-size="queryParams.pageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  :total="total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleRepairSizeChange"
                  @current-change="handleRepairCurrentChange"
                />
              </div>
            </template>
          </div>
        </el-tab-pane>
        
        <!-- 我的收藏标签页 -->
        <el-tab-pane label="我的收藏" name="collect-list">
          <div v-loading="collectLoading" class="collect-list">
            <el-empty v-if="collectList.length === 0" description="暂无收藏内容">
              <el-button type="primary" @click="router.push('/forum')">浏览论坛</el-button>
            </el-empty>
            
            <template v-else>
              <el-table :data="collectList" style="width: 100%">
                <el-table-column prop="postTitle" label="文章标题" show-overflow-tooltip />
                <el-table-column label="收藏时间" width="180">
                  <template #default="{ row }">
                    {{ formatDateTime(row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <el-button
                      link
                      type="primary"
                      size="small"
                      @click="viewPostDetail(row.postId)"
                    >
                      查看
                    </el-button>
                    <el-button
                      link
                      type="danger"
                      size="small"
                      @click="cancelCollect(row)"
                    >
                      取消收藏
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="pagination">
                <el-pagination
                  v-model:current-page="collectQueryParams.pageNum"
                  v-model:page-size="collectQueryParams.pageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  :total="collectTotal"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleCollectSizeChange"
                  @current-change="handleCollectCurrentChange"
                />
              </div>
            </template>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 评价对话框 -->
    <el-dialog
      v-model="ratingDialogVisible"
      title="评价报修服务"
      width="500px"
    >
      <el-form>
        <el-form-item label="服务评分">
          <el-rate v-model="ratingForm.rating" :max="5" show-text :texts="['差', '一般', '满意', '很满意', '非常满意']" />
        </el-form-item>
        
        <el-form-item label="评价内容">
          <el-input
            v-model="ratingForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入您对本次服务的评价..."
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="ratingDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ratingLoading" @click="submitRating">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 20px 0 30px;
}

.user-info h2 {
  margin-top: 10px;
  font-size: 22px;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 80px;
  height: 80px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loading-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 20px;
  color: #409EFF;
}

.avatar-container {
  position: relative;
}

.edit-form {
  margin-top: 20px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.repair-list, .collect-list {
  min-height: 300px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 