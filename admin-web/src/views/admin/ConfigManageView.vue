<template>
  <div class="grid-two">
    <el-card>
      <template #header>
        <div class="toolbar" style="margin-bottom:0">
          <span>报修类型管理</span>
          <el-button type="primary" size="small" @click="openRepairTypeDialog()">新增</el-button>
        </div>
      </template>
      <el-table :data="repairTypes">
        <el-table-column prop="typeName" label="名称" />
        <el-table-column prop="sortNo" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="openRepairTypeDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeRepairType(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card>
      <template #header>
        <div class="toolbar" style="margin-bottom:0">
          <span>字典配置</span>
          <el-button type="primary" size="small" @click="openDictDialog()">新增</el-button>
        </div>
      </template>
      <el-table :data="dicts">
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const repairTypes = ref([])
const dicts = ref([])
const repairTypeDialogVisible = ref(false)
const dictDialogVisible = ref(false)

const repairTypeForm = reactive({ id: null, typeName: '', sortNo: 1, status: 'enabled' })
const dictForm = reactive({ id: null, dictType: 'repair_work_type', dictCode: '', dictName: '', sortNo: 1, status: 'enabled' })

async function loadAll() {
  repairTypes.value = (await api.get('/admin/repair-types')).data.data
  dicts.value = (await api.get('/admin/dicts')).data.data
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

function nextSortNo(list) {
  return Math.max(0, ...list.map((item) => Number(item.sortNo || 0))) + 1
}

onMounted(loadAll)
</script>
