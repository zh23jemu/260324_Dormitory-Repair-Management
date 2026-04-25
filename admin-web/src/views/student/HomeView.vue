<template>
  <div class="portal-home">
    <section class="portal-banner">
      <div class="portal-banner__copy">
        <div class="portal-banner__eyebrow">高校后勤一站式服务平台</div>
        <h1>报修服务平台</h1>
        <p>统一浏览全部报修记录、服务统计、维修排行、论坛动态与公告信息。学生、宿管、维修员、管理员均从同一主页进入系统。</p>
        <div class="portal-banner__cta">
          <el-button type="primary" size="large" @click="goProtected('/student/repair/create')">我要报修</el-button>
          <el-button size="large" @click="$router.push('/login')">角色登录</el-button>
        </div>
      </div>
      <div class="portal-banner__visual">
        <div class="portal-illustration-card">
          <div class="portal-illustration-card__title">统一门户演示入口</div>
          <div class="portal-illustration-card__list">
            <span>全部报修记录公开浏览</span>
            <span>服务统计与维修排行</span>
            <span>论坛与公告统一展示</span>
            <span>按角色进入不同工作台</span>
          </div>
        </div>
      </div>
    </section>

    <section class="portal-main">
      <div class="portal-feed">
        <div class="portal-panel portal-feed__panel">
          <div class="portal-feed__toolbar">
            <div class="portal-filter-row">
              <el-select v-model="selectedBuilding" placeholder="全部报修区域" clearable class="portal-filter-select">
                <el-option label="全部报修区域" value="all" />
                <el-option v-for="item in buildingOptions" :key="item" :label="item" :value="item" />
              </el-select>
              <el-select v-model="selectedType" placeholder="全部报修项目" clearable class="portal-filter-select">
                <el-option label="全部报修项目" value="all" />
                <el-option v-for="item in repairTypeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </div>
            <el-input v-model="keyword" placeholder="单号/报修人/内容" clearable />
          </div>

          <div v-if="filteredOrders.length" class="portal-order-list">
            <article v-for="item in filteredOrders" :key="item.id" class="portal-order-card">
              <div class="portal-order-card__head">
                <div class="portal-order-card__title-row">
                  <el-tag type="warning" effect="plain">{{ repairOrderStatusText(item.status) }}</el-tag>
                  <strong>{{ orderTitle(item) }}</strong>
                </div>
                <span class="portal-order-card__no">单号：{{ item.orderNo }}</span>
              </div>
              <div class="portal-order-card__meta">
                <span>报修人：{{ item.studentUsername || item.studentName || '未知用户' }}</span>
                <span>时间：{{ item.submittedAt }}</span>
                <span>位置：{{ orderLocation(item) }}</span>
              </div>
              <p class="portal-order-card__desc">{{ item.description || '暂无报修描述' }}</p>
              <div class="portal-order-card__foot">
                <span>类型：{{ item.repairTypeName || '未分类' }}</span>
                <span v-if="item.repairerName">维修员：{{ item.repairerName }}</span>
              </div>
            </article>
          </div>
          <el-empty v-else description="没有匹配的报修记录" />
        </div>
      </div>

      <aside class="portal-sidebar">
        <div class="portal-panel portal-entry-card" @click="goProtected('/student/repair/create')">
          <div class="portal-entry-card__icon">🔧</div>
          <div>
            <strong>我要报修</strong>
            <span>登录后提交宿舍报修申请</span>
          </div>
        </div>

        <div class="portal-panel portal-stats-card">
          <div class="portal-side-title">维修服务统计</div>
          <div class="portal-stats-card__block">
            <div class="portal-stats-card__caption">工单处理情况</div>
            <div class="portal-kpi-grid">
              <div class="portal-kpi-item">
                <span>工单总数</span>
                <strong>{{ statistics.totalCount || 0 }}</strong>
              </div>
              <div class="portal-kpi-item">
                <span>已维修</span>
                <strong class="is-success">{{ statistics.completedCount || 0 }}</strong>
              </div>
              <div class="portal-kpi-item">
                <span>维修中</span>
                <strong class="is-warn">{{ statistics.processingCount || 0 }}</strong>
              </div>
            </div>
          </div>
          <div class="portal-stats-card__block">
            <div class="portal-stats-card__caption">维修单位排行榜</div>
            <div class="portal-mini-rank">
              <div v-for="(item, index) in repairerRanking.slice(0, 5)" :key="item.id" class="portal-mini-rank__item">
                <span>#{{ index + 1 }} {{ item.realName }}</span>
                <strong>{{ item.completedCount || 0 }} 单</strong>
              </div>
            </div>
          </div>
        </div>

        <div class="portal-panel portal-stats-card">
          <div class="portal-side-title">系统功能</div>
          <div class="portal-feature-list">
            <div class="portal-feature-list__item">
              <strong>公告浏览</strong>
              <span>首页直接查看发布时间与发布者</span>
            </div>
            <div class="portal-feature-list__item">
              <strong>论坛交流</strong>
              <span>公开浏览帖子，登录后发帖和评论</span>
            </div>
            <div class="portal-feature-list__item">
              <strong>服务透明</strong>
              <span>展示历史报修记录、评分和维修排行</span>
            </div>
          </div>
        </div>
      </aside>
    </section>

    <section class="portal-bottom">
      <div class="portal-panel">
        <div class="portal-section-title">公告信息</div>
        <div v-if="announcements.length" class="portal-announcement-list">
          <article v-for="item in announcements.slice(0, 4)" :key="item.id" class="portal-announcement-card">
            <img v-if="item.imagePath" :src="fileUrl(item.imagePath)" alt="公告配图" />
            <div class="portal-announcement-card__content">
              <strong>{{ item.title }}</strong>
              <p>{{ item.content }}</p>
              <span>发布者：{{ item.publisherUsername || item.publisherName || '系统' }} · 发布时间：{{ item.publishedAt || item.createdAt }}</span>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无公告" />
      </div>

      <div class="portal-panel">
        <div class="portal-section-title">公开论坛</div>
        <div v-if="forumPosts.length" class="portal-forum-list">
          <article v-for="post in forumPosts.slice(0, 6)" :key="post.id" class="portal-forum-item">
            <div class="portal-forum-item__author">
              <el-avatar :src="fileUrl(post.avatar) || defaultAvatar" :size="42" />
              <div>
                <strong>{{ post.username || post.studentName || '同学' }}</strong>
                <span>发帖时间：{{ post.createdAt }}</span>
              </div>
            </div>
            <h3>{{ post.title }}</h3>
            <p>{{ post.content }}</p>
            <img v-if="post.imagePath" :src="fileUrl(post.imagePath)" alt="论坛帖子配图" />
            <div class="portal-forum-item__comments">
              <div v-for="comment in (post.comments || []).slice(0, 3)" :key="comment.id" class="portal-comment-item">
                <el-avatar :src="fileUrl(comment.avatar) || defaultAvatar" :size="26" />
                <div>
                  <strong>{{ comment.username || comment.realName || '用户' }}</strong>
                  <span>{{ comment.content }} · {{ comment.createdAt }}</span>
                </div>
              </div>
            </div>
            <div class="portal-forum-item__actions">
              <el-input
                v-if="commentForms[post.id] !== undefined"
                v-model="commentForms[post.id]"
                placeholder="写下你的评论"
              />
              <el-button size="small" @click="submitComment(post)">评论</el-button>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无论坛帖子" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api'
import { useAuth } from '../../utils/auth'
import { fileUrl } from '../../utils/file'
import { repairOrderStatusText } from '../../utils/status'

const router = useRouter()
const auth = useAuth()
const announcements = ref([])
const orders = ref([])
const forumPosts = ref([])
const repairerRanking = ref([])
const statistics = ref({})
const commentForms = reactive({})
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
const keyword = ref('')
const selectedBuilding = ref('all')
const selectedType = ref('all')

const buildingOptions = computed(() => Array.from(new Set((orders.value || []).map((item) => String(item.buildingName || '').trim()).filter(Boolean))))
const repairTypeOptions = computed(() => Array.from(new Set((orders.value || []).map((item) => String(item.repairTypeName || '').trim()).filter(Boolean))))

const filteredOrders = computed(() => {
  return (orders.value || []).filter((item) => {
    const buildingMatch = selectedBuilding.value === 'all' || !selectedBuilding.value || item.buildingName === selectedBuilding.value
    const typeMatch = selectedType.value === 'all' || !selectedType.value || item.repairTypeName === selectedType.value
    const searchText = `${item.orderNo || ''} ${item.studentUsername || ''} ${item.studentName || ''} ${item.title || ''} ${item.description || ''}`.toLowerCase()
    const keywordMatch = !keyword.value.trim() || searchText.includes(keyword.value.trim().toLowerCase())
    return buildingMatch && typeMatch && keywordMatch
  })
})

async function loadPortal() {
  const { data } = await api.get('/portal/home')
  announcements.value = data.data.announcements || []
  orders.value = data.data.orders || []
  forumPosts.value = data.data.forumPosts || []
  repairerRanking.value = data.data.repairerRanking || []
  statistics.value = data.data.statistics || {}
}

function goProtected(path) {
  if (!auth.hasToken()) {
    router.push(`/login?redirect=${encodeURIComponent(path)}&role=student`)
    return
  }
  if (!auth.hasRole('student')) {
    ElMessage.warning('当前账号请进入对应工作台操作')
    router.push(auth.getRoleHomePath())
    return
  }
  router.push(path)
}

function orderTitle(item) {
  const prefix = item.buildingName ? `【${item.buildingName}】` : ''
  const typeName = item.repairTypeName || '综合报修'
  return `${prefix}${typeName}`
}

function orderLocation(item) {
  return [item.buildingName, item.roomNo].filter(Boolean).join(' / ') || '未填写位置'
}

async function submitComment(post) {
  if (!auth.hasToken()) {
    router.push(`/login?redirect=${encodeURIComponent('/home')}&role=student`)
    return
  }
  if (commentForms[post.id] === undefined) {
    commentForms[post.id] = ''
    return
  }
  if (!commentForms[post.id].trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  await api.post(`/portal/forum-posts/${post.id}/comments`, { content: commentForms[post.id].trim() })
  ElMessage.success('评论已发布')
  commentForms[post.id] = ''
  await loadPortal()
}

onMounted(loadPortal)
</script>
