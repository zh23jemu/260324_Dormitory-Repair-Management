<template>
  <div class="repair-record-rating-container">
    <div class="header">
      <div class="title">维修评价管理</div>
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item>
          <el-input v-model="queryParams.keyword" placeholder="搜索关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <!-- <el-button type="success" @click="handleAdd">新增评价</el-button> -->
        </el-form-item>
      </el-form>
    </div>

    <div class="main">
      <el-table :data="ratingList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="repairRecordName" label="维修记录" />
        <el-table-column prop="userName" label="评价用户" />
        <el-table-column prop="rating" label="评分">
          <template #default="scope">
            <el-rate v-model="scope.row.rating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评价内容" show-overflow-tooltip />
        <el-table-column prop="createTime" label="评价时间" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
<!--            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>-->
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 评价弹窗 -->
    <el-dialog v-model="dialogVisible" title="评价维修记录" width="500px">
      <el-form :model="ratingForm" label-width="100px" ref="ratingFormRef" :rules="rules">
        <el-form-item label="维修记录" prop="repairRecordId">
          <el-select v-model="ratingForm.repairRecordId" placeholder="请选择维修记录" style="width: 100%">
            <el-option
              v-for="item in repairRecordOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="ratingForm.rating" :max="5" :allow-half="false" />
        </el-form-item>
        <el-form-item label="评价内容" prop="comment">
          <el-input v-model="ratingForm.comment" type="textarea" :rows="4" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRating" :loading="submitLoading">提交</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 评价详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="评价详情" width="500px">
      <div class="rating-detail">
        <div class="detail-item">
          <span class="label">维修记录：</span>
          <span>{{ detailInfo.repairRecordName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">评价用户：</span>
          <span>{{ detailInfo.userName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">评分：</span>
          <span><el-rate v-model="detailInfo.rating" disabled /></span>
        </div>
        <div class="detail-item">
          <span class="label">评价内容：</span>
          <span>{{ detailInfo.comment }}</span>
        </div>
        <div class="detail-item">
          <span class="label">评价时间：</span>
          <span>{{ detailInfo.createTime }}</span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRepairRecordRatingList, deleteRepairRecordRating, saveRepairRecordRating } from '@/api/repairRecordRating'
import { getRepairRecordList } from '@/api/repairRecord'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const loading = ref(false)
const ratingList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const submitLoading = ref(false)
const ratingFormRef = ref(null)
const repairRecordOptions = ref([])
const detailInfo = reactive({
  id: '',
  repairRecordName: '',
  userName: '',
  rating: 0,
  comment: '',
  createTime: ''
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

const ratingForm = reactive({
  repairRecordId: '',
  rating: 0,
  comment: ''
})

const rules = {
  repairRecordId: [{ required: true, message: '请选择维修记录', trigger: 'change' }],
  rating: [{ 
    required: true, 
    message: '请进行评分', 
    trigger: 'change', 
    validator: (rule, value, callback) => {
      if (value <= 0) {
        callback(new Error('请进行评分'));
      } else {
        callback();
      }
    }
  }],
  comment: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

// 加载评价列表数据
const loadRatingList = async () => {
  loading.value = true
  try {
    const res = await getRepairRecordRatingList(queryParams)
    ratingList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取评价列表失败', error)
    ElMessage.error('获取评价列表失败')
  } finally {
    loading.value = false
  }
}

// 加载维修记录选项
const loadRepairRecordOptions = async () => {
  try {
    const res = await getRepairRecordList({
      pageNum: 1,
      pageSize: 100,
      status: 'COMPLETED' // 只显示已完成的维修记录
    })
    repairRecordOptions.value = res.data.records
  } catch (error) {
    console.error('获取维修记录列表失败', error)
    ElMessage.error('获取维修记录列表失败')
  }
}

// 查询按钮点击
const handleQuery = () => {
  queryParams.pageNum = 1
  loadRatingList()
}

// 重置查询
const resetQuery = () => {
  queryParams.pageNum = 1
  queryParams.keyword = ''
  loadRatingList()
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  loadRatingList()
}

// 页码改变
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadRatingList()
}

// 打开评价弹窗
const handleAdd = () => {
  ratingForm.repairRecordId = ''
  ratingForm.rating = 0
  ratingForm.comment = ''
  loadRepairRecordOptions()
  dialogVisible.value = true
}

// 查看评价
const handleView = (row) => {
  Object.assign(detailInfo, row)
  viewDialogVisible.value = true
}

// 提交评价
const submitRating = async () => {
  if (!ratingFormRef.value) return
  
  await ratingFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      await saveRepairRecordRating(ratingForm)
      ElMessage.success('评价提交成功')
      dialogVisible.value = false
      loadRatingList()
    } catch (error) {
      console.error('提交评价失败', error)
      ElMessage.error(error.response?.data?.message || '提交评价失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 删除评价
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除此评价记录?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRepairRecordRating(row.id)
      ElMessage.success('删除成功')
      loadRatingList()
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  // 获取用户信息
  if (!userStore.userInfo) {
    userStore.getUserInfo()
  }
  // 加载评价列表
  loadRatingList()
})
</script>

<style lang="less" scoped>
.repair-record-rating-container {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .title {
      font-size: 20px;
      font-weight: bold;
    }
  }

  .main {
    background-color: #fff;
    padding: 20px;
    border-radius: 5px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
  }
  
  .rating-detail {
    padding: 10px 0;
    
    .detail-item {
      margin-bottom: 15px;
      display: flex;
      
      .label {
        font-weight: bold;
        width: 100px;
      }
    }
  }
}
</style> 