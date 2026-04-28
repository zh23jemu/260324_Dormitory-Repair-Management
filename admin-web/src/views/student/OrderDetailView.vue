<template>
  <div class="student-page">
    <section class="student-hero" v-if="order">
      <div class="student-hero__copy">
        <div class="student-hero__eyebrow">工单详情</div>
        <h1>{{ order.title }}</h1>
        <p>查看报修工单的当前状态、流转记录、维修结果和图片资料。</p>
      </div>
      <div class="student-hero__metrics">
        <div class="student-hero__metric">
          <span>当前状态</span>
          <strong>{{ repairOrderStatusText(order.status) }}</strong>
        </div>
      </div>
    </section>

    <section class="student-layout" v-if="order">
      <div class="student-main">
        <div class="student-card student-card--accent">
          <div class="student-card__header">
            <div>
              <h2>处理摘要</h2>
              <p>从提交到完结的关键信息汇总，方便快速了解当前处理进度。</p>
            </div>
          </div>
          <div class="student-highlight-grid">
            <div class="student-highlight-item">
              <span>当前状态</span>
              <strong>{{ repairOrderStatusText(order.status) }}</strong>
            </div>
            <div class="student-highlight-item">
              <span>维修人员</span>
              <strong>{{ order.repairerName || '待分配' }}</strong>
            </div>
            <div class="student-highlight-item">
              <span>期望上门</span>
              <strong>{{ order.expectTime || '未填写' }}</strong>
            </div>
          </div>
        </div>

        <div class="student-card">
          <div class="student-card__header">
            <div>
              <h2>基础信息</h2>
              <p>工单号、位置、维修员和结果等关键信息汇总。</p>
            </div>
          </div>
          <div class="student-info-grid">
            <div class="student-info-item"><span>工单号</span><strong>{{ order.orderNo }}</strong></div>
            <div class="student-info-item"><span>报修类型</span><strong>{{ order.repairTypeName }}</strong></div>
            <div class="student-info-item"><span>宿舍位置</span><strong>{{ `${order.buildingName || ''} ${order.roomNo || ''}` }}</strong></div>
            <div class="student-info-item"><span>关联设施</span><strong>{{ order.facilityName || '未关联' }}</strong></div>
            <div class="student-info-item"><span>期望时间</span><strong>{{ order.expectTime || '未填写' }}</strong></div>
            <div class="student-info-item"><span>维修人员</span><strong>{{ order.repairerName || '待分配' }}</strong></div>
            <div class="student-info-item student-info-item--full"><span>维修结果</span><strong>{{ order.resultDesc || '暂无' }}</strong></div>
          </div>
        </div>

        <div class="student-card">
          <div class="student-card__header">
            <div>
              <h2>报修内容</h2>
            </div>
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
        </div>

        <div class="student-card">
          <div class="student-card__header">
            <div>
              <h2>图片记录</h2>
              <p>展示学生提交的现场图片，便于回溯问题场景。</p>
            </div>
          </div>
          <div v-if="order.images?.length" class="mobile-image-grid">
            <img v-for="img in order.images" :key="img.id" :src="fileUrl(img.file_path)" @click="previewImage(fileUrl(img.file_path))" />
          </div>
          <van-empty v-else description="暂无图片" />
        </div>

        <div class="student-card">
          <div class="student-card__header">
            <div>
              <h2>处理时间线</h2>
              <p>按状态流转顺序查看每一次处理动作和备注说明。</p>
            </div>
          </div>
          <van-steps direction="vertical" :active="(order.flows || []).length - 1" class="student-timeline">
            <van-step v-for="item in order.flows || []" :key="item.id">
              <h4>{{ repairOrderStatusText(item.to_status) }}</h4>
              <p>{{ item.operator_name || '系统' }}：{{ item.remark }}</p>
              <p>{{ item.created_at }}</p>
            </van-step>
          </van-steps>
          <van-button v-if="order.status === 'pending_rating'" type="success" block @click="ratingVisible = true">提交评价</van-button>
        </div>
      </div>
    </section>

    <van-popup v-model:show="ratingVisible" round position="bottom" :style="{ minHeight: '52%' }">
      <div style="padding: 16px">
        <div class="student-popup-title">服务评价</div>
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
