<template>
  <div class="nested-replies">
    <div 
      v-for="reply in comments" 
      :key="reply.id" 
      class="comment-item reply-item"
    >
      <div class="comment-avatar">
        <el-avatar :size="36" :src="reply.userAvatar">
          {{ reply.username ? reply.username.charAt(0) : 'U' }}
        </el-avatar>
      </div>
      
      <div class="comment-main">
        <div class="comment-header">
          <span class="user-name">{{ reply.username }}</span>
          <span class="comment-time">{{ formatDateTime(reply.createdTime) }}</span>
        </div>
        
        <div class="comment-content">
          {{ reply.content }}
        </div>
        
        <div class="comment-actions">
          <span class="action-item" @click="$emit('reply', reply)">
            <el-icon><ChatDotRound /></el-icon> 回复
          </span>
          
          <span class="action-item" @click="$emit('like', reply)">
            <el-icon><StarFilled /></el-icon> {{ reply.likeCount || 0 }}
          </span>
        </div>
        
        <!-- 递归渲染更深层级的回复 -->
        <CommentReplies
          v-if="reply.reply && reply.reply.length > 0"
          :comments="reply.reply"
          @reply="$emit('reply', $event)"
          @like="$emit('like', $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  comments: {
    type: Array,
    required: true
  },
  parentId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['reply', 'like'])

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.nested-replies {
  margin-left: 12px;
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #ebeef5;
}

.reply-item {
  margin-bottom: 12px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.comment-item {
  display: flex;
  margin-bottom: 16px;
  gap: 12px;
}

.comment-main {
  flex: 1;
}

.comment-header {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 8px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  cursor: pointer;
}

.action-item:hover {
  color: #409EFF;
}
</style> 