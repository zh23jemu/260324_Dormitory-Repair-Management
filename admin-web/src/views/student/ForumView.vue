<template>
  <div class="student-page">
    <section class="student-hero">
      <div class="student-hero__copy">
        <div class="student-hero__eyebrow">学生交流社区</div>
        <h1>学生论坛</h1>
        <p>这里只展示已发布帖子。你可以按关键字检索内容，登录后再进入发帖页面。</p>
      </div>
    </section>

    <section class="student-layout">
      <div class="student-main">
        <div class="student-card student-forum-toolbar">
          <form class="student-forum-toolbar__search" @submit.prevent="searchPosts">
            <el-input
              v-model="keyword"
              clearable
              placeholder="输入关键字检索标题、内容或发布者"
              @clear="resetSearch"
              @keydown.enter.prevent="searchPosts"
            />
            <el-button type="primary" native-type="submit">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </form>
          <div class="student-forum-toolbar__actions">
            <el-button type="primary" plain @click="goCreatePost">发布帖子</el-button>
          </div>
        </div>

        <div class="student-card">
          <div class="student-card__header">
            <div>
              <h2>帖子列表</h2>
              <p>帖子、评论和发帖入口均仅对登录学生开放，评论会显示头像与发布时间。</p>
            </div>
          </div>
          <div v-if="posts.length" class="student-forum-grid">
            <article v-for="item in posts" :key="item.id" class="student-forum-card">
              <div class="student-forum-card__author-row">
                <el-avatar :src="fileUrl(item.avatar) || defaultAvatar" :size="46" />
                <div class="student-forum-card__author-info">
                  <strong>{{ item.username || item.studentName || '同学' }}</strong>
                  <span>发帖时间：{{ item.createdAt }}</span>
                </div>
              </div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.content }}</p>
              <img v-if="item.imagePath" :src="fileUrl(item.imagePath)" alt="帖子图片" />
              <div class="student-forum-card__comment-summary">共 {{ item.commentCount || 0 }} 条评论</div>
              <div v-if="(item.comments || []).length" class="student-forum-card__comments">
                <div v-for="comment in item.comments" :key="comment.id" class="student-forum-card__comment">
                  <el-avatar :src="fileUrl(comment.avatar) || defaultAvatar" :size="30" />
                  <div class="student-forum-card__comment-body">
                    <div class="student-forum-card__comment-head">
                      <strong>{{ comment.username || comment.realName || '用户' }}</strong>
                      <span>{{ comment.createdAt }}</span>
                    </div>
                    <p>{{ comment.content }}</p>
                  </div>
                </div>
              </div>
              <div class="student-forum-card__comment-form">
                <el-input v-model="commentForms[item.id]" placeholder="写下评论，大家都会看到" />
                <el-button type="primary" @click="submitComment(item)">发表评论</el-button>
              </div>
            </article>
          </div>
          <el-empty v-else description="暂无帖子" />
          <el-pagination
            v-model:current-page="page.pageNum"
            v-model:page-size="page.pageSize"
            :total="page.total"
            layout="total, sizes, prev, pager, next"
            class="student-pagination"
            @current-change="loadPosts"
            @size-change="loadPosts"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import api from '../../api'
import { fileUrl } from '../../utils/file'
import { useAuth } from '../../utils/auth'
import { applyPageResult, createPageState, pageParams } from '../../utils/pagination'

const router = useRouter()
const auth = useAuth()
const posts = ref([])
const page = reactive(createPageState())
const keyword = ref('')
const commentForms = reactive({})
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

async function loadPosts() {
  if (!auth.hasToken()) {
    router.push(`/login?redirect=${encodeURIComponent('/forum')}&role=student`)
    return
  }
  const params = {
    ...pageParams(page)
  }
  if (keyword.value.trim()) {
    params.keyword = keyword.value.trim()
  }
  posts.value = applyPageResult(page, (await api.get('/portal/forum-posts', { params })).data.data)
  posts.value.forEach((item) => {
    if (commentForms[item.id] === undefined) {
      commentForms[item.id] = ''
    }
  })
}

function searchPosts() {
  page.pageNum = 1
  loadPosts()
}

function resetSearch() {
  keyword.value = ''
  page.pageNum = 1
  loadPosts()
}

function goCreatePost() {
  router.push('/forum/create')
}

async function submitComment(post) {
  if (!auth.hasToken()) {
    router.push(`/login?redirect=${encodeURIComponent('/forum')}&role=student`)
    return
  }
  const content = String(commentForms[post.id] || '').trim()
  if (!content) {
    showToast('请输入评论内容')
    return
  }
  await api.post(`/portal/forum-posts/${post.id}/comments`, { content })
  showToast('评论已发布')
  commentForms[post.id] = ''
  await loadPosts()
}

onMounted(loadPosts)
</script>

<style scoped>
.student-forum-toolbar {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.student-forum-toolbar__search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 10px;
  align-items: center;
}

.student-forum-toolbar__actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  color: #64748b;
  font-size: 13px;
}

.student-forum-card__comment-summary {
  margin-top: 14px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.student-forum-card__comments {
  display: grid;
  gap: 10px;
  margin-top: 12px;
  padding: 14px;
  border: 1px solid rgba(37, 99, 235, 0.12);
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.9), rgba(240, 253, 250, 0.75));
}

.student-forum-card__comment {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.student-forum-card__comment-body {
  flex: 1;
  min-width: 0;
}

.student-forum-card__comment-head {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
  color: #64748b;
  font-size: 12px;
}

.student-forum-card__comment-head strong {
  color: #0f172a;
  font-size: 13px;
}

.student-forum-card__comment-body p {
  margin: 4px 0 0;
  color: #334155;
  font-size: 13px;
  line-height: 1.7;
}

.student-forum-card__comment-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  margin-top: 14px;
}

@media (max-width: 640px) {
  .student-forum-toolbar__search {
    grid-template-columns: 1fr;
  }

  .student-forum-toolbar__actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .student-forum-card__comment-form {
    grid-template-columns: 1fr;
  }
}
</style>
