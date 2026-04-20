<template>
  <el-dialog :model-value="modelValue" title="工单详情" width="860px" @update:model-value="emit('update:modelValue', $event)">
    <div v-if="order" class="detail-grid">
      <div><strong>工单号：</strong>{{ order.orderNo }}</div>
      <div><strong>状态：</strong>{{ repairOrderStatusText(order.status) }}</div>
      <div><strong>学生：</strong>{{ order.studentName }}</div>
      <div><strong>报修类型：</strong>{{ order.repairTypeName }}</div>
      <div><strong>宿舍：</strong>{{ order.buildingName }} {{ order.roomNo }}</div>
      <div><strong>关联设施：</strong>{{ order.facilityName || '未关联' }}</div>
      <div style="grid-column:1 / -1"><strong>描述：</strong>{{ order.description }}</div>
    </div>
  </el-dialog>
</template>

<script setup>
import { repairOrderStatusText } from '../../../utils/status'

defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue'])
</script>
