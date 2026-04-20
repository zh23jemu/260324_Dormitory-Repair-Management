<template>
  <div class="mobile-shell">
    <div class="hero">
      <div class="hero-title">学生报修端</div>
      <div class="hero-subtitle">{{ auth.state.profile.realName }}，{{ auth.state.profile.studentNo || '未绑定学号' }}</div>
      <div class="hero-tip">支持提交报修、查看工单、浏览知识库和查看维修人员信息</div>
    </div>

    <div class="layout-content">
      <router-view />
    </div>

    <van-tabbar route fixed placeholder active-color="#2563eb">
      <van-tabbar-item replace to="/home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/repair/create" icon="edit">报修</van-tabbar-item>
      <van-tabbar-item replace to="/orders" icon="orders-o">工单</van-tabbar-item>
      <van-tabbar-item replace to="/resources" icon="description">知识库</van-tabbar-item>
      <van-tabbar-item replace to="/profile" icon="manager-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { useStudentAuth } from '../utils/auth'

const auth = useStudentAuth()
</script>
