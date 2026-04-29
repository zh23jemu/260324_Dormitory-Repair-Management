<template>
  <div class="grid-two">
    <el-card>
      <template #header>
        <div class="toolbar" style="margin-bottom:0">
          <span>报修类型管理</span>
          <el-button type="primary" size="small" @click="editRepairType()">新增</el-button>
        </div>
      </template>
      <el-table :data="repairTypes">
        <el-table-column prop="typeName" label="名称" />
        <el-table-column prop="sortNo" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="editRepairType(row)">编辑</el-button>
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
          <el-button type="primary" size="small" @click="editDict()">新增</el-button>
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
              <el-button size="small" type="primary" plain @click="editDict(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeDict(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const repairTypes = ref([])
const dicts = ref([])

async function loadAll() {
  repairTypes.value = (await api.get('/admin/repair-types')).data.data
  dicts.value = (await api.get('/admin/dicts')).data.data
}

async function editRepairType(row) {
  const typeName = window.prompt('报修类型名称', row?.typeName || '')
  if (!typeName) return
  const sortNo = window.prompt('排序号', row?.sortNo || '1')
  if (!sortNo) return
  const status = window.prompt('状态(enabled/disabled)', row?.status || 'enabled')
  const payload = { typeName: typeName.trim(), sortNo: Number(sortNo), status: status || 'enabled' }
  if (row?.id) await api.put(`/admin/repair-types/${row.id}`, payload)
  else await api.post('/admin/repair-types', payload)
  ElMessage.success('报修类型已保存')
  await loadAll()
}

async function removeRepairType(row) {
  await ElMessageBox.confirm(`确认删除报修类型 ${row.typeName} 吗？`, '提示', { type: 'warning' })
  await api.delete(`/admin/repair-types/${row.id}`)
  ElMessage.success('报修类型已删除')
  await loadAll()
}

async function editDict(row) {
  const dictType = window.prompt('字典类型', row?.dictType || 'repair_work_type')
  if (!dictType) return
  const dictCode = window.prompt('字典编码', row?.dictCode || '')
  if (!dictCode) return
  const dictName = window.prompt('字典名称', row?.dictName || '')
  if (!dictName) return
  const sortNo = window.prompt('排序号', row?.sortNo || '1')
  if (!sortNo) return
  const payload = { dictType, dictCode, dictName, sortNo: Number(sortNo), status: row?.status || 'enabled' }
  if (row?.id) await api.put(`/admin/dicts/${row.id}`, payload)
  else await api.post('/admin/dicts', payload)
  ElMessage.success('字典项已保存')
  await loadAll()
}

async function removeDict(row) {
  await ElMessageBox.confirm(`确认删除字典项 ${row.dictName} 吗？`, '提示', { type: 'warning' })
  await api.delete(`/admin/dicts/${row.id}`)
  ElMessage.success('字典项已删除')
  await loadAll()
}

onMounted(loadAll)
</script>
