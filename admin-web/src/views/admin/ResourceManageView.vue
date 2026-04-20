<template>
  <div class="stack-section">
    <el-card>
      <template #header>
        <div class="toolbar" style="margin-bottom:0">
          <span>报修知识库</span>
          <el-button type="primary" @click="openDialog()">新增内容</el-button>
        </div>
      </template>
      <el-table :data="resources">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="summary" label="摘要" min-width="220" />
        <el-table-column prop="sortNo" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="publisherName" label="发布人" width="120" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="removeResource(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑知识内容' : '新增知识内容'" width="680px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" rows="2" /></el-form-item>
        <el-form-item label="正文"><el-input v-model="form.content" type="textarea" rows="8" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortNo" :min="1" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="发布" value="published" />
            <el-option label="下线" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveResource">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const resources = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, title: '', category: '', summary: '', content: '', sortNo: 1, status: 'published' })

async function loadAll() {
  resources.value = (await api.get('/admin/resources')).data.data
}

function openDialog(row) {
  form.id = row?.id || null
  form.title = row?.title || ''
  form.category = row?.category || ''
  form.summary = row?.summary || ''
  form.content = row?.content || ''
  form.sortNo = row?.sortNo || 1
  form.status = row?.status || 'published'
  dialogVisible.value = true
}

async function saveResource() {
  const payload = { title: form.title, category: form.category, summary: form.summary, content: form.content, coverImage: '', sortNo: Number(form.sortNo || 1), status: form.status }
  if (form.id) await api.put(`/admin/resources/${form.id}`, payload)
  else await api.post('/admin/resources', payload)
  ElMessage.success('知识内容已保存')
  dialogVisible.value = false
  await loadAll()
}

async function removeResource(row) {
  await ElMessageBox.confirm(`确认删除《${row.title}》吗？`, '提示', { type: 'warning' })
  await api.delete(`/admin/resources/${row.id}`)
  ElMessage.success('知识内容已删除')
  await loadAll()
}

onMounted(loadAll)
</script>
