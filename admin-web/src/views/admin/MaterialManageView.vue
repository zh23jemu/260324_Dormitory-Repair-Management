<template>
  <el-card>
    <template #header>
      <div class="material-card-header">
        <span>耗材管理</span>
        <el-button type="primary" @click="openCreateDialog">新增耗材</el-button>
      </div>
    </template>
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
            <el-button size="small" type="primary" plain @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="removeMaterial(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" append-to-body destroy-on-close>
      <el-form :model="form" label-width="96px">
        <el-form-item label="耗材名称">
          <el-input v-model="form.materialName" placeholder="请输入耗材名称，例如：温控模块" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.materialType" placeholder="请选择耗材分类" style="width: 100%" filterable>
            <el-option v-for="item in materialTypeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="请输入计量单位，例如：个、卷、盒" />
        </el-form-item>
        <el-form-item label="库存数量">
          <el-input-number v-model="form.stockQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预警值">
          <el-input-number v-model="form.warningQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可填写规格型号、存放位置或采购说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMaterial">{{ submitText }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const materials = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, materialName: '', materialType: '', unit: '个', stockQty: 0, warningQty: 0, remark: '' })
const defaultMaterialTypes = ['水电耗材', '家具耗材', '网络耗材', '门窗耗材', '卫浴耗材', '公共设施耗材', '其他耗材']
const dialogTitle = computed(() => (form.id ? '编辑耗材信息' : '新增耗材信息'))
const submitText = computed(() => (form.id ? '保存修改' : '确认新增'))
const materialTypeOptions = computed(() => {
  /*
   * 分类下拉默认提供常见宿舍维修耗材类型，同时合并数据库中已有分类。
   * 这样既能避免新增时随意手输导致分类分散，也不会让历史数据里的分类在编辑时丢失。
   */
  const existedTypes = materials.value.map((item) => item.materialType).filter(Boolean)
  return Array.from(new Set([...defaultMaterialTypes, ...existedTypes]))
})

async function loadMaterials() {
  materials.value = (await api.get('/admin/materials')).data.data
}

function resetForm() {
  Object.assign(form, { id: null, materialName: '', materialType: '', unit: '个', stockQty: 0, warningQty: 0, remark: '' })
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  Object.assign(form, {
    id: row.id,
    materialName: row.materialName || '',
    materialType: row.materialType || '',
    unit: row.unit || '个',
    stockQty: Number(row.stockQty || 0),
    warningQty: Number(row.warningQty || 0),
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function saveMaterial() {
  const payload = { ...form }
  if (form.id) await api.put(`/admin/materials/${form.id}`, payload)
  else await api.post('/admin/materials', payload)
  ElMessage.success('耗材已保存')
  dialogVisible.value = false
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

<style scoped>
.material-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
</style>
