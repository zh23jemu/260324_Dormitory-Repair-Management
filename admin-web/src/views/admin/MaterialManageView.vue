<template>
  <el-card>
    <template #header>耗材管理</template>
    <el-form :model="form" inline>
      <el-form-item><el-input v-model="form.materialName" placeholder="耗材名称" /></el-form-item>
      <el-form-item><el-input v-model="form.materialType" placeholder="分类" /></el-form-item>
      <el-form-item><el-input v-model="form.unit" placeholder="单位" style="width:90px" /></el-form-item>
      <el-form-item><el-input-number v-model="form.stockQty" :min="0" :precision="2" placeholder="库存" /></el-form-item>
      <el-form-item><el-input-number v-model="form.warningQty" :min="0" :precision="2" placeholder="预警" /></el-form-item>
      <el-form-item><el-button type="primary" @click="saveMaterial">{{ form.id ? '保存' : '新增' }}</el-button></el-form-item>
    </el-form>
    <el-table :data="materials" style="margin-top:12px">
      <el-table-column prop="materialName" label="耗材名称" />
      <el-table-column prop="materialType" label="分类" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="stockQty" label="库存" width="100" />
      <el-table-column prop="warningQty" label="预警值" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="Number(row.stockQty) <= Number(row.warningQty) ? 'danger' : 'success'">
            {{ Number(row.stockQty) <= Number(row.warningQty) ? '库存预警' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" plain @click="editMaterial(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="removeMaterial(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const materials = ref([])
const form = reactive({ id: null, materialName: '', materialType: '', unit: '个', stockQty: 0, warningQty: 0, remark: '' })

async function loadMaterials() {
  materials.value = (await api.get('/admin/materials')).data.data
}

function resetForm() {
  Object.assign(form, { id: null, materialName: '', materialType: '', unit: '个', stockQty: 0, warningQty: 0, remark: '' })
}

function editMaterial(row) {
  Object.assign(form, row)
}

async function saveMaterial() {
  const payload = { ...form }
  if (form.id) await api.put(`/admin/materials/${form.id}`, payload)
  else await api.post('/admin/materials', payload)
  ElMessage.success('耗材已保存')
  resetForm()
  await loadMaterials()
}

async function removeMaterial(row) {
  await ElMessageBox.confirm(`确认删除耗材 ${row.materialName} 吗？`, '提示', { type: 'warning' })
  await api.delete(`/admin/materials/${row.id}`)
  ElMessage.success('耗材已删除')
  await loadMaterials()
}

onMounted(loadMaterials)
</script>
