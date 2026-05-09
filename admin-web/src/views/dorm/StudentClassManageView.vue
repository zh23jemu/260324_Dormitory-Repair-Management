<template>
  <el-card>
    <template #header>
      <div class="toolbar" style="margin-bottom:0">
        <span>学生班级管理</span>
        <el-text type="info">按行调整学生所属学院、专业和班级</el-text>
      </div>
    </template>

    <el-table :data="students" row-key="id">
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="realName" label="姓名" width="110" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="学院" min-width="170">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="editForm.college"
            placeholder="选择学院"
            filterable
            class="class-edit-control"
            @change="handleCollegeChange"
          >
            <el-option v-for="item in collegeOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <span v-else>{{ row.college || '未填写' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="专业" min-width="190">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="editForm.major"
            placeholder="选择专业"
            filterable
            class="class-edit-control"
            :disabled="!editForm.college"
            @change="handleMajorChange"
          >
            <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <span v-else>{{ row.major || '未填写' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="班级" min-width="150">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="editForm.className"
            placeholder="选择班级"
            filterable
            class="class-edit-control"
            :disabled="!editForm.major"
          >
            <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <span v-else>{{ row.className || '未填写' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="当前住宿" min-width="170">
        <template #default="{ row }">
          {{ formatStay(row) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <div v-if="isEditing(row)" class="class-actions">
            <el-button class="class-primary-btn" size="small" type="primary" :loading="saving" @click="saveRow(row)">
              保存
            </el-button>
            <el-button class="class-cancel-btn" size="small" :disabled="saving" @click="cancelEdit">取消</el-button>
          </div>
          <el-button v-else class="class-primary-btn" size="small" type="primary" @click="startEdit(row)">
            编辑班级
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page.pageNum"
      v-model:page-size="page.pageSize"
      :total="page.total"
      layout="total, sizes, prev, pager, next, jumper"
      class="table-pagination"
      @current-change="loadStudents"
      @size-change="loadStudents"
    />
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'
import { applyPageResult, createPageState, pageParams } from '../../utils/pagination'

const students = ref([])
const schoolOptions = reactive({ colleges: [], majors: [], classes: [] })
const page = reactive(createPageState())
const editingId = ref(null)
const saving = ref(false)

// 行内编辑表单一次性维护学院、专业、班级，交互方式和学生住宿管理保持一致。
const editForm = reactive({
  college: '',
  major: '',
  className: '',
})

const collegeOptions = computed(() => schoolOptions.colleges.map((item) => item.collegeName))
const majorOptions = computed(() => schoolOptions.majors.filter((item) => item.collegeName === editForm.college).map((item) => item.majorName))
const classOptions = computed(() => schoolOptions.classes.filter((item) => item.collegeName === editForm.college && item.majorName === editForm.major).map((item) => item.className))

async function loadAll() {
  const school = (await api.get('/dorm-admin/school-options')).data.data
  Object.assign(schoolOptions, {
    colleges: school.colleges || [],
    majors: school.majors || [],
    classes: school.classes || [],
  })
  await loadStudents()
}

async function loadStudents() {
  students.value = applyPageResult(page, (await api.get('/dorm-admin/students', { params: pageParams(page) })).data.data)
}

function isEditing(row) {
  return editingId.value === row.id
}

function startEdit(row) {
  editingId.value = row.id
  editForm.college = row.college || ''
  editForm.major = row.major || ''
  editForm.className = row.className || ''
}

function cancelEdit() {
  editingId.value = null
  editForm.college = ''
  editForm.major = ''
  editForm.className = ''
}

function handleCollegeChange() {
  // 切换学院后，原专业和班级可能不再属于新学院，需要清空重新选择。
  editForm.major = ''
  editForm.className = ''
}

function handleMajorChange() {
  // 切换专业后，班级范围随之变化，需要清空重新选择。
  editForm.className = ''
}

function formatStay(row) {
  const roomText = [row.buildingName, row.roomNo].filter(Boolean).join(' ')
  if (!roomText && !row.bedNo) return '未分配'
  return [roomText || '未分配宿舍', row.bedNo ? `${row.bedNo}床` : ''].filter(Boolean).join(' / ')
}

async function saveRow(row) {
  if (!editForm.college) return ElMessage.warning('请选择学院')
  if (!editForm.major) return ElMessage.warning('请选择专业')
  if (!editForm.className) return ElMessage.warning('请选择班级')

  saving.value = true
  try {
    await api.put(`/dorm-admin/students/${row.id}/class`, {
      college: editForm.college,
      major: editForm.major,
      className: editForm.className,
    })
    ElMessage.success('班级信息已更新')
    cancelEdit()
    await loadStudents()
  } finally {
    saving.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.class-edit-control {
  width: 100%;
}

.class-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: nowrap;
}

/* 与学生住宿管理保持同一套高对比按钮风格，避免后台背景图影响可读性。 */
.class-primary-btn {
  min-width: 82px;
  border: none !important;
  background: linear-gradient(135deg, #0f766e 0%, #2563eb 100%) !important;
  color: #ffffff !important;
  font-weight: 700;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.24);
}

.class-primary-btn :deep(span) {
  color: #ffffff !important;
}

.class-primary-btn:hover,
.class-primary-btn:focus {
  background: linear-gradient(135deg, #0d5f59 0%, #1d4ed8 100%) !important;
  color: #ffffff !important;
}

.class-cancel-btn {
  min-width: 64px;
  border-color: #cbd5e1 !important;
  background: #ffffff !important;
  color: #334155 !important;
  font-weight: 700;
}

.class-cancel-btn :deep(span) {
  color: #334155 !important;
}
</style>
