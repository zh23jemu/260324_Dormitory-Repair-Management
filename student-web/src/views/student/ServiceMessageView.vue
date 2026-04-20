<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-title">服务留言反馈</div>
      <van-field v-model="form.title" label="标题" placeholder="例如：希望增加维修进度提醒" />
      <van-field v-model="form.content" label="内容" type="textarea" rows="4" placeholder="请填写建议、意见或使用中遇到的问题" />
      <van-button type="primary" block @click="submitMessage">提交留言</van-button>
    </div>
    <div class="section">
      <div class="section-title">我的留言</div>
      <van-cell-group inset>
        <van-cell v-for="item in messages" :key="item.id" :title="item.title" :label="item.content" :value="item.status" />
      </van-cell-group>
      <div v-for="item in messages" :key="`reply-${item.id}`" class="message-reply" v-if="item.replyContent">
        <strong>管理员回复：</strong>{{ item.replyContent }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import api from '../../api'

const form = reactive({ title: '', content: '', imagePath: '' })
const messages = ref([])

async function loadMessages() {
  messages.value = (await api.get('/student/service-messages')).data.data
}

async function submitMessage() {
  await api.post('/student/service-messages', form)
  showToast('留言已提交')
  form.title = ''
  form.content = ''
  await loadMessages()
}

onMounted(loadMessages)
</script>
