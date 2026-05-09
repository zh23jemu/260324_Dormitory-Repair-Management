<template>
  <div class="grid-two">
    <el-card>
      <template #header>
        <div class="toolbar" style="margin-bottom:0">
          <span>报修类型管理</span>
          <el-button type="primary" size="small" @click="openRepairTypeDialog()">新增</el-button>
        </div>
      </template>
      <el-table :data="pagedRepairTypes.records">
        <el-table-column prop="typeName" label="名称" />
        <el-table-column prop="sortNo" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="openRepairTypeDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeRepairType(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="repairTypePage.pageNum"
        v-model:page-size="repairTypePage.pageSize"
        :total="pagedRepairTypes.total"
        layout="total, sizes, prev, pager, next"
        class="table-pagination"
      />
    </el-card>

    <el-card class="school-config-card">
      <template #header>
        <div class="toolbar" style="margin-bottom:0">
          <span>学院专业班级</span>
          <el-space wrap>
            <el-button type="primary" size="small" @click="openCollegeDialog()">新增学院</el-button>
            <el-button type="primary" size="small" :disabled="!colleges.length" @click="openMajorDialog()">新增专业</el-button>
            <el-button type="primary" size="small" :disabled="!majors.length" @click="openClassDialog()">新增班级</el-button>
          </el-space>
        </div>
      </template>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="维护顺序：先新增学院，再在学院下新增专业，最后在专业下新增班级。学生注册时只能选择这里启用的数据。"
        style="margin-bottom: 14px"
      />
      <el-tabs v-model="schoolTab">
        <el-tab-pane label="学院" name="college">
          <el-table :data="pagedColleges.records">
            <el-table-column prop="collegeName" label="学院名称" />
            <el-table-column prop="sortNo" label="排序" width="80" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button size="small" type="primary" plain @click="openCollegeDialog(row)">编辑</el-button>
                  <el-button size="small" type="danger" plain @click="removeCollege(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="collegePage.pageNum" v-model:page-size="collegePage.pageSize" :total="pagedColleges.total" layout="total, sizes, prev, pager, next" class="table-pagination" />
        </el-tab-pane>
        <el-tab-pane label="专业" name="major">
          <el-table :data="pagedMajors.records">
            <el-table-column prop="collegeName" label="所属学院" min-width="140" />
            <el-table-column prop="majorName" label="专业名称" min-width="160" />
            <el-table-column prop="sortNo" label="排序" width="80" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button size="small" type="primary" plain @click="openMajorDialog(row)">编辑</el-button>
                  <el-button size="small" type="danger" plain @click="removeMajor(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="majorPage.pageNum" v-model:page-size="majorPage.pageSize" :total="pagedMajors.total" layout="total, sizes, prev, pager, next" class="table-pagination" />
        </el-tab-pane>
        <el-tab-pane label="班级" name="class">
          <el-table :data="pagedClasses.records">
            <el-table-column prop="collegeName" label="所属学院" min-width="140" />
            <el-table-column prop="majorName" label="所属专业" min-width="160" />
            <el-table-column prop="className" label="班级名称" min-width="140" />
            <el-table-column prop="sortNo" label="排序" width="80" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button size="small" type="primary" plain @click="openClassDialog(row)">编辑</el-button>
                  <el-button size="small" type="danger" plain @click="removeClass(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="classPage.pageNum" v-model:page-size="classPage.pageSize" :total="pagedClasses.total" layout="total, sizes, prev, pager, next" class="table-pagination" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card>
      <template #header>
        <div class="toolbar" style="margin-bottom:0">
          <span>字典配置</span>
          <el-button type="primary" size="small" @click="openDictDialog()">新增</el-button>
        </div>
      </template>
      <el-table :data="pagedDicts.records">
        <el-table-column prop="dictType" label="类型" width="150" />
        <el-table-column prop="dictCode" label="编码" width="140" />
        <el-table-column prop="dictName" label="名称" />
        <el-table-column prop="sortNo" label="排序" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="openDictDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeDict(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="dictPage.pageNum"
        v-model:page-size="dictPage.pageSize"
        :total="pagedDicts.total"
        layout="total, sizes, prev, pager, next"
        class="table-pagination"
      />
    </el-card>

    <el-dialog v-model="repairTypeDialogVisible" :title="repairTypeForm.id ? '编辑报修类型' : '新增报修类型'" width="520px">
      <el-form :model="repairTypeForm" label-width="90px">
        <el-form-item label="类型名称">
          <el-input v-model="repairTypeForm.typeName" placeholder="如 水电、家具、网络" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="repairTypeForm.sortNo" :min="1" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="repairTypeForm.status" style="width: 100%">
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="repairTypeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRepairType">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dictDialogVisible" :title="dictForm.id ? '编辑字典项' : '新增字典项'" width="560px">
      <el-form :model="dictForm" label-width="90px">
        <el-form-item label="字典类型">
          <el-input v-model="dictForm.dictType" placeholder="如 repair_work_type、rating_indicator" />
        </el-form-item>
        <el-form-item label="字典编码">
          <el-input v-model="dictForm.dictCode" placeholder="如 electrician、repair_quality" />
        </el-form-item>
        <el-form-item label="字典名称">
          <el-input v-model="dictForm.dictName" placeholder="页面展示名称" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="dictForm.sortNo" :min="1" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="dictForm.status" style="width: 100%">
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDict">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="collegeDialogVisible" :title="collegeForm.id ? '编辑学院' : '新增学院'" width="520px">
      <el-form :model="collegeForm" label-width="90px">
        <el-form-item label="学院名称"><el-input v-model="collegeForm.collegeName" placeholder="如 信息工程学院" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="collegeForm.sortNo" :min="1" :precision="0" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="collegeForm.status" style="width: 100%">
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="collegeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCollege">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="majorDialogVisible" :title="majorForm.id ? '编辑专业' : '新增专业'" width="560px">
      <el-form :model="majorForm" label-width="90px">
        <el-form-item label="所属学院">
          <el-select v-model="majorForm.collegeId" placeholder="请选择学院" filterable style="width: 100%">
            <el-option v-for="item in colleges" :key="item.id" :label="item.collegeName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业名称"><el-input v-model="majorForm.majorName" placeholder="如 软件工程" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="majorForm.sortNo" :min="1" :precision="0" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="majorForm.status" style="width: 100%">
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="majorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMajor">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="classDialogVisible" :title="classForm.id ? '编辑班级' : '新增班级'" width="560px">
      <el-form :model="classForm" label-width="90px">
        <el-form-item label="所属专业">
          <el-select v-model="classForm.majorId" placeholder="请选择专业" filterable style="width: 100%">
            <el-option v-for="item in majors" :key="item.id" :label="`${item.collegeName} / ${item.majorName}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级名称"><el-input v-model="classForm.className" placeholder="如 软工2201" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="classForm.sortNo" :min="1" :precision="0" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="classForm.status" style="width: 100%">
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveClass">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'
import { createPageState, paginateClient } from '../../utils/pagination'

const repairTypes = ref([])
const dicts = ref([])
const colleges = ref([])
const majors = ref([])
const classes = ref([])
const repairTypePage = reactive(createPageState())
const dictPage = reactive(createPageState())
const collegePage = reactive(createPageState())
const majorPage = reactive(createPageState())
const classPage = reactive(createPageState())
const repairTypeDialogVisible = ref(false)
const dictDialogVisible = ref(false)
const collegeDialogVisible = ref(false)
const majorDialogVisible = ref(false)
const classDialogVisible = ref(false)
const schoolTab = ref('college')

const repairTypeForm = reactive({ id: null, typeName: '', sortNo: 1, status: 'enabled' })
const dictForm = reactive({ id: null, dictType: 'repair_work_type', dictCode: '', dictName: '', sortNo: 1, status: 'enabled' })
const collegeForm = reactive({ id: null, collegeName: '', sortNo: 1, status: 'enabled' })
const majorForm = reactive({ id: null, collegeId: null, majorName: '', sortNo: 1, status: 'enabled' })
const classForm = reactive({ id: null, majorId: null, className: '', sortNo: 1, status: 'enabled' })
const pagedRepairTypes = computed(() => paginateClient(repairTypes.value, repairTypePage))
const pagedDicts = computed(() => paginateClient(dicts.value, dictPage))
const pagedColleges = computed(() => paginateClient(colleges.value, collegePage))
const pagedMajors = computed(() => paginateClient(majors.value, majorPage))
const pagedClasses = computed(() => paginateClient(classes.value, classPage))

async function loadAll() {
  repairTypes.value = (await api.get('/admin/repair-types')).data.data
  dicts.value = (await api.get('/admin/dicts')).data.data
  const school = (await api.get('/admin/school-options')).data.data
  colleges.value = school.colleges || []
  majors.value = school.majors || []
  classes.value = school.classes || []
}

function openRepairTypeDialog(row) {
  // 弹窗表单一次性承载报修类型的所有可维护字段，避免 prompt 逐项输入导致漏填。
  repairTypeForm.id = row?.id || null
  repairTypeForm.typeName = row?.typeName || ''
  repairTypeForm.sortNo = Number(row?.sortNo || nextSortNo(repairTypes.value))
  repairTypeForm.status = row?.status || 'enabled'
  repairTypeDialogVisible.value = true
}

async function saveRepairType() {
  if (!repairTypeForm.typeName.trim()) return ElMessage.warning('请输入报修类型名称')
  const payload = {
    typeName: repairTypeForm.typeName.trim(),
    sortNo: Number(repairTypeForm.sortNo || 1),
    status: repairTypeForm.status || 'enabled',
  }
  if (repairTypeForm.id) await api.put(`/admin/repair-types/${repairTypeForm.id}`, payload)
  else await api.post('/admin/repair-types', payload)
  ElMessage.success('报修类型已保存')
  repairTypeDialogVisible.value = false
  await loadAll()
}

async function removeRepairType(row) {
  await ElMessageBox.confirm(`确认删除报修类型 ${row.typeName} 吗？`, '提示', { type: 'warning' })
  await api.delete(`/admin/repair-types/${row.id}`)
  ElMessage.success('报修类型已删除')
  await loadAll()
}

function openDictDialog(row) {
  // 字典项新增/编辑统一使用结构化表单，类型、编码、名称、排序、状态可以一次确认。
  dictForm.id = row?.id || null
  dictForm.dictType = row?.dictType || 'repair_work_type'
  dictForm.dictCode = row?.dictCode || ''
  dictForm.dictName = row?.dictName || ''
  dictForm.sortNo = Number(row?.sortNo || nextSortNo(dicts.value))
  dictForm.status = row?.status || 'enabled'
  dictDialogVisible.value = true
}

async function saveDict() {
  if (!dictForm.dictType.trim()) return ElMessage.warning('请输入字典类型')
  if (!dictForm.dictCode.trim()) return ElMessage.warning('请输入字典编码')
  if (!dictForm.dictName.trim()) return ElMessage.warning('请输入字典名称')
  const payload = {
    dictType: dictForm.dictType.trim(),
    dictCode: dictForm.dictCode.trim(),
    dictName: dictForm.dictName.trim(),
    sortNo: Number(dictForm.sortNo || 1),
    status: dictForm.status || 'enabled',
  }
  if (dictForm.id) await api.put(`/admin/dicts/${dictForm.id}`, payload)
  else await api.post('/admin/dicts', payload)
  ElMessage.success('字典项已保存')
  dictDialogVisible.value = false
  await loadAll()
}

async function removeDict(row) {
  await ElMessageBox.confirm(`确认删除字典项 ${row.dictName} 吗？`, '提示', { type: 'warning' })
  await api.delete(`/admin/dicts/${row.id}`)
  ElMessage.success('字典项已删除')
  await loadAll()
}

function openCollegeDialog(row) {
  // 学院是学校组织结构的第一级，新增专业前必须先维护学院。
  collegeForm.id = row?.id || null
  collegeForm.collegeName = row?.collegeName || ''
  collegeForm.sortNo = Number(row?.sortNo || nextSortNo(colleges.value))
  collegeForm.status = row?.status || 'enabled'
  collegeDialogVisible.value = true
}

async function saveCollege() {
  if (!collegeForm.collegeName.trim()) return ElMessage.warning('请输入学院名称')
  const payload = { collegeName: collegeForm.collegeName.trim(), sortNo: Number(collegeForm.sortNo || 1), status: collegeForm.status || 'enabled' }
  if (collegeForm.id) await api.put(`/admin/school-colleges/${collegeForm.id}`, payload)
  else await api.post('/admin/school-colleges', payload)
  ElMessage.success('学院已保存')
  collegeDialogVisible.value = false
  await loadAll()
}

async function removeCollege(row) {
  await ElMessageBox.confirm(`确认删除学院 ${row.collegeName} 吗？若已存在专业或学生引用将无法删除。`, '提示', { type: 'warning' })
  await api.delete(`/admin/school-colleges/${row.id}`)
  ElMessage.success('学院已删除')
  await loadAll()
}

function openMajorDialog(row) {
  if (!colleges.value.length) {
    ElMessage.warning('请先新增学院，再新增专业')
    return
  }
  // 专业必须绑定学院，弹窗中一次完成学院和专业信息维护。
  majorForm.id = row?.id || null
  majorForm.collegeId = row?.collegeId || colleges.value[0]?.id || null
  majorForm.majorName = row?.majorName || ''
  majorForm.sortNo = Number(row?.sortNo || nextSortNo(majors.value.filter((item) => item.collegeId === majorForm.collegeId)))
  majorForm.status = row?.status || 'enabled'
  majorDialogVisible.value = true
}

async function saveMajor() {
  if (!majorForm.collegeId) return ElMessage.warning('请选择所属学院')
  if (!majorForm.majorName.trim()) return ElMessage.warning('请输入专业名称')
  const payload = { collegeId: majorForm.collegeId, majorName: majorForm.majorName.trim(), sortNo: Number(majorForm.sortNo || 1), status: majorForm.status || 'enabled' }
  if (majorForm.id) await api.put(`/admin/school-majors/${majorForm.id}`, payload)
  else await api.post('/admin/school-majors', payload)
  ElMessage.success('专业已保存')
  majorDialogVisible.value = false
  await loadAll()
}

async function removeMajor(row) {
  await ElMessageBox.confirm(`确认删除专业 ${row.majorName} 吗？若已存在班级或学生引用将无法删除。`, '提示', { type: 'warning' })
  await api.delete(`/admin/school-majors/${row.id}`)
  ElMessage.success('专业已删除')
  await loadAll()
}

function openClassDialog(row) {
  if (!majors.value.length) {
    ElMessage.warning('请先新增专业，再新增班级')
    return
  }
  // 班级必须绑定专业，学生注册最终选择的班级来自这里。
  classForm.id = row?.id || null
  classForm.majorId = row?.majorId || majors.value[0]?.id || null
  classForm.className = row?.className || ''
  classForm.sortNo = Number(row?.sortNo || nextSortNo(classes.value.filter((item) => item.majorId === classForm.majorId)))
  classForm.status = row?.status || 'enabled'
  classDialogVisible.value = true
}

async function saveClass() {
  if (!classForm.majorId) return ElMessage.warning('请选择所属专业')
  if (!classForm.className.trim()) return ElMessage.warning('请输入班级名称')
  const payload = { majorId: classForm.majorId, className: classForm.className.trim(), sortNo: Number(classForm.sortNo || 1), status: classForm.status || 'enabled' }
  if (classForm.id) await api.put(`/admin/school-classes/${classForm.id}`, payload)
  else await api.post('/admin/school-classes', payload)
  ElMessage.success('班级已保存')
  classDialogVisible.value = false
  await loadAll()
}

async function removeClass(row) {
  await ElMessageBox.confirm(`确认删除班级 ${row.className} 吗？若已有学生引用将无法删除。`, '提示', { type: 'warning' })
  await api.delete(`/admin/school-classes/${row.id}`)
  ElMessage.success('班级已删除')
  await loadAll()
}

function nextSortNo(list) {
  return Math.max(0, ...list.map((item) => Number(item.sortNo || 0))) + 1
}

function statusText(status) {
  // 后端仍然保存 enabled/disabled，前端统一转成中文，避免表格里暴露技术状态值。
  const statusMap = {
    enabled: '启用',
    disabled: '禁用'
  }
  return statusMap[status] || status || '-'
}

function statusTagType(status) {
  return status === 'disabled' ? 'info' : 'success'
}

onMounted(loadAll)
</script>
