<script setup>
import Aside from '@/components/Aside.vue'
import Header from '@/components/Header.vue'
import MenuTag from '@/components/MenuTag.vue'
</script>

<template>
  <div class="layout">
    <el-container class="container">
      <!-- 自定义左侧组件 -->
      <Aside></Aside>
      <el-container>
        <!-- 自定义头部组件 -->
        <el-header>
          <Header></Header>
        </el-header>
        <MenuTag></MenuTag>
        <el-main class="main">
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped lang="less">
.layout ,.container{
  width: 100%;
  height: 100%;
}
.el-header{
  background-color: #333;
}

</style>
