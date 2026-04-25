<template>
  <div class="page-stack">
    <div class="section" v-if="order">
      <div class="section-head">
        <div class="section-title">工单详情</div>
        <van-tag type="primary">{{ repairOrderStatusText(order.status) }}</van-tag>
      </div>
      <van-cell-group inset>
        <van-cell title="工单号" :value="order.orderNo" />
        <van-cell title="报修类型" :value="order.repairTypeName" />
        <van-cell title="宿舍位置" :value="`${order.buildingName || ''} ${order.roomNo || ''}`" />
        <van-cell title="关联设施" :value="order.facilityName || '未关联'" />
        <van-cell title="期望时间" :value="order.expectTime || '未填写'" />
        <van-cell title="维修人员" :value="order.repairerName || '待分配'" />
        <van-cell title="维修结果" :value="order.resultDesc || '暂无'" />
      </van-cell-group>
      <div class="detail-block">
        <strong>标题</strong>
        <p>{{ order.title }}</p>
      </div>
      <div class="detail-block">
        <strong>描述</strong>
        <p>{{ order.description }}</p>
      </div>
      <div class="detail-block" v-if="order.rejectReason">
        <strong>驳回原因</strong>
        <p>{{ order.rejectReason }}</p>
      </div>
      <div class="detail-block" v-if="order.rating">
        <strong>服务评价</strong>
        <p>{{ order.rating.score }} 星 / {{ order.rating.content || '未填写评价内容' }}</p>
      </div>
      <div class="detail-block">
        <strong>图片记录</strong>
        <div v-if="order.images?.length" class="mobile-image-grid">
          <img v-for="img in order.images" :key="img.id" :src="fileUrl(img.file_path)" @click="previewImage(fileUrl(img.file_path))" />
        </div>
        <van-empty v-else description="暂无图片" />
      </div>
      <div class="detail-block">
        <strong>处理时间线</strong>
        <van-steps direction="vertical" :active="(order.flows || []).length - 1">
          <van-step v-for="item in order.flows || []" :key="item.id">
            <h4>{{ repairOrderStatusText(item.to_status) }}</h4>
            <p>{{ item.operator_name || '系统' }}：{{ item.remark }}</p>
            <p>{{ item.created_at }}</p>
          </van-step>
        </van-steps>
      </div>
      <van-button v-if="order.status === 'pending_rating'" type="success" block @click="ratingVisible = true">提交评价</van-button>
    </div>

    <van-popup v-model:show="ratingVisible" round position="bottom" :style="{ minHeight: '52%' }">
      <div style="padding: 16px">
        <div style="font-size: 20px; font-weight: 700; margin-bottom: 12px">服务评价</div>
        <div v-if="ratingIndicators.length" class="indicator-box">
          <div style="font-weight: 700; margin-bottom: 8px">评价参考</div>
          <div class="indicator-list">
            <span v-for="item in ratingIndicators" :key="item.id" class="indicator-chip">{{ item.dictName }}</span>
          </div>
        </div>
        <div style="margin: 16px 0 12px">
          <div style="font-weight: 700; margin-bottom: 8px">评分</div>
          <van-rate v-model="ratingForm.score" :count="5" />
        </div>
        <van-field v-model="ratingForm.content" label="评价内容" type="textarea" rows="4" placeholder="请填写本次维修服务评价" />
        <div style="margin-top: 16px; display: flex; gap: 12px">
          <van-button plain block @click="ratingVisible = false">取消</van-button>
          <van-button type="success" block @click="submitRating">提交</van-button>
        </div>
      </div>
    </van-popup>

    <van-image-preview v-model:show="previewVisible" :images="previewImages" :start-position="previewIndex" :close-on-click-image="true" :close-on-click-overlay="true" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import api from '../../api'
import { fileUrl } from '../../utils/file'
import { repairOrderStatusText } from '../../utils/status'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const ratingIndicators = ref([])
const ratingVisible = ref(false)
const previewVisible = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)
const ratingForm = reactive({ score: 5, content: '' })

async function loadDetail() {
  order.value = (await api.get(`/student/repair-orders/${route.params.id}`)).data.data
  ratingIndicators.value = (await api.get('/student/rating-indicators')).data.data
}

function previewImage(src) {
  previewImages.value = (order.value?.images || []).map((img) => fileUrl(img.file_path))
  previewIndex.value = Math.max(0, previewImages.value.indexOf(src))
  previewVisible.value = true
}

async function submitRating() {
  await api.post(`/student/repair-orders/${order.value.id}/rating`, { score: ratingForm.score, content: ratingForm.content })
  showToast('评价成功')
  ratingVisible.value = false
  await loadDetail()
  router.replace('/student/orders')
}

onMounted(loadDetail)
</script>
