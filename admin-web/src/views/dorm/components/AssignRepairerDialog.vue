<template>
  <el-dialog :model-value="modelValue" title="分配维修人员" width="760px" @update:model-value="emit('update:modelValue', $event)">
    <el-table :data="repairers" size="small">
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="workTypeName" label="工种" width="120" />
      <el-table-column prop="phone" label="联系方式" width="130" />
      <el-table-column prop="totalCount" label="累计工单" width="90" />
      <el-table-column prop="completedCount" label="已完成" width="90" />
      <el-table-column prop="avgScore" label="平均评分" width="90" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="emit('assign', row)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  repairers: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'assign'])
</script>
