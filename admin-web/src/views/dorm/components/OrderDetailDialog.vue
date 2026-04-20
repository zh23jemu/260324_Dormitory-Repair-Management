<template>
  <el-dialog :model-value="modelValue" title="工单详情" width="900px" @update:model-value="emit('update:modelValue', $event)">
    <div v-if="order" class="detail-grid">
      <div><strong>工单号：</strong>{{ order.orderNo }}</div>
      <div><strong>状态：</strong>{{ order.status }}</div>
      <div><strong>学生：</strong>{{ order.studentName }}</div>
      <div><strong>维修人员：</strong>{{ order.repairerName || '待分配' }}</div>
      <div><strong>报修类型：</strong>{{ order.repairTypeName }}</div>
      <div><strong>宿舍：</strong>{{ order.buildingName }} {{ order.roomNo }}</div>
      <div><strong>关联设施：</strong>{{ order.facilityName || '未关联' }}</div>
      <div><strong>期望时间：</strong>{{ order.expectTime || '未填写' }}</div>
      <div style="grid-column:1 / -1"><strong>标题：</strong>{{ order.title }}</div>
      <div style="grid-column:1 / -1"><strong>描述：</strong>{{ order.description }}</div>
    </div>

    <div class="section-title" style="margin-top:18px">处理时间线</div>
    <el-timeline>
      <el-timeline-item v-for="item in order?.flows || []" :key="item.id" :timestamp="item.created_at">
        {{ item.operator_name || '系统' }}：{{ item.remark }}
      </el-timeline-item>
    </el-timeline>

    <div class="section-title">图片记录</div>
    <div v-if="order?.images?.length" class="image-grid">
      <div v-for="img in order.images" :key="img.id" class="image-card">
        <div class="image-tag">{{ img.image_type === 'result' ? '维修结果' : '故障图片' }}</div>
        <img :src="fileUrl(img.file_path)" @click="openPreview(img.file_path)" />
      </div>
    </div>
    <div v-else class="empty-text">暂无图片</div>

    <el-dialog v-model="previewVisible" title="图片预览" width="760px">
      <div class="preview-wrap">
        <img v-if="previewUrl" :src="previewUrl" class="preview-image" @click="previewVisible = false" />
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { fileUrl } from '../../../utils/file'

defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue'])
const previewVisible = ref(false)
const previewUrl = ref('')

function openPreview(path) {
  // 图片预览统一在详情弹窗内部处理，避免父页面继续堆叠预览相关状态。
  previewUrl.value = fileUrl(path)
  previewVisible.value = true
}
</script>
