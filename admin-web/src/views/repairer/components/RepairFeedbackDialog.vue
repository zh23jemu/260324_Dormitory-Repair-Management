<template>
  <el-dialog :model-value="modelValue" title="提交维修结果" width="560px" @update:model-value="emit('update:modelValue', $event)">
    <el-form :model="form" label-width="90px">
      <el-form-item label="处理结果"><el-input v-model="form.resultDesc" type="textarea" rows="4" /></el-form-item>
      <el-form-item label="使用材料"><el-input v-model="form.materialsUsed" /></el-form-item>
      <el-form-item label="耗材上报">
        <div class="material-usage-list">
          <div v-for="(item, index) in form.materialUsages" :key="index" class="material-usage-row">
            <el-select v-model="item.materialId" placeholder="选择耗材" style="width:220px">
              <el-option v-for="m in materials" :key="m.id" :label="`${m.materialName}（库存 ${m.stockQty}${m.unit}）`" :value="m.id" />
            </el-select>
            <el-input-number v-model="item.quantity" :min="0" :precision="2" placeholder="数量" />
            <el-button type="danger" plain @click="form.materialUsages.splice(index, 1)">删除</el-button>
          </div>
          <el-button plain @click="form.materialUsages.push({ materialId: null, quantity: 1 })">新增耗材</el-button>
        </div>
      </el-form-item>
      <el-form-item label="完成时间"><el-input v-model="form.finishTime" placeholder="2026-04-17 18:00:00" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="emit('submit')">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  form: { type: Object, required: true },
  materials: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'submit'])
</script>

<style scoped>
.material-usage-list {
  display: grid;
  gap: 10px;
  width: 100%;
}

.material-usage-row {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
