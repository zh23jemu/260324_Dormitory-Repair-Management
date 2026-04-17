<script setup>
import { ref, onMounted, reactive, onUnmounted } from 'vue'
import { getCountData, getProportion } from '@/api/home'
import { getUserByToken } from '@/api/user'
import { useStore } from '@/store'
import * as echarts from 'echarts'
import { User, Tools, Box, ChatDotRound } from '@element-plus/icons-vue'

const store = useStore()
const userInfo = ref({})
const loading = ref(true)
const chartInstance = ref(null)

// 统计数据
const countData = reactive({
  userCount: 0,
  resourceCount: 0,
  repairRecordCount: 0,
  postCount: 0
})

// 获取统计数据
const getStatisticData = async () => {
  try {
    const res = await getCountData()
    if (res.code === 200) {
      Object.assign(countData, res.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 初始化图表
const initChart = () => {
  const chartDom = document.getElementById('repairTypeChart')
  if (!chartDom) return
  
  chartInstance.value = echarts.init(chartDom)
  
  // 设置默认配置
  const option = {
    title: {
      text: '维修类型占比',
      subtext: '各类型报修数量统计',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        const data = params[0]
        const totalRecords = countData.repairRecordCount || data.value * 2 // 备用计算方法
        return `${data.name}<br/>报修数量：${data.value} (${(data.value / totalRecords * 100).toFixed(2)}%)`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['暂无数据'],
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      name: '报修数量'
    },
    series: [
      {
        name: '报修数量',
        type: 'bar',
        barWidth: '60%',
        data: [0],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  }
  
  chartInstance.value.setOption(option)
}

// 更新图表数据
const updateChartData = async () => {
  try {
    const res = await getProportion()
    if (res.code === 200 && res.data.length > 0) {
      const data = res.data
      
      // 对数据按报修次数排序
      data.sort((a, b) => b.recordCount - a.recordCount)
      
      // 适配实际API返回的字段名
      const typeNames = data.map(item => item.repairTypeName || '未知类型')
      const counts = data.map(item => item.recordCount || 0)
      
      // 计算总数用于百分比显示
      const totalCount = counts.reduce((acc, curr) => acc + curr, 0)
      
      const option = {
        tooltip: {
          formatter: function(params) {
            const data = params[0]
            return `${data.name}<br/>报修数量：${data.value} (${(data.value / totalCount * 100).toFixed(2)}%)`
          }
        },
        xAxis: {
          data: typeNames
        },
        series: [
          {
            data: counts.map((value, index) => {
              return {
                value: value,
                // 为每个柱子添加名称
                name: typeNames[index],
                // 使用不同颜色区分不同类型
                itemStyle: {
                  color: getRandomColor(index)
                }
              }
            })
          }
        ]
      }
      
      chartInstance.value.setOption(option)
    }
  } catch (error) {
    console.error('获取维修类型占比数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 生成随机颜色，但保持一致性(基于索引)
const getRandomColor = (index) => {
  const colors = [
    '#1976d2', '#2196f3', '#64b5f6', '#90caf9',
    '#ff9800', '#ffa726', '#ffb74d', '#ffcc80',
    '#4caf50', '#66bb6a', '#81c784', '#a5d6a7',
    '#e91e63', '#ec407a', '#f48fb1', '#f8bbd0'
  ]
  return colors[index % colors.length]
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    const res = await getUserByToken()
    if (res.code === 200) {
      userInfo.value = res.data
      store.state.userInfo = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 窗口大小改变时重绘图表
const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

onMounted(async () => {
  // 获取用户信息
  await getUserInfo()
  
  // 获取统计数据
  await getStatisticData()
  
  // 初始化图表
  initChart()
  
  // 更新图表数据
  await updateChartData()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
})
</script>

<template>
  <div class="home-container">
    <!-- 欢迎消息 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1>欢迎来到校园后勤报修系统</h1>
        <p>您好，{{ userInfo.name || '尊敬的用户' }}！今天是 {{ new Date().toLocaleDateString() }}，祝您工作愉快。</p>
      </div>
    </div>
    
    <!-- 数据统计卡片 -->
    <div class="stat-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="card-content user-card">
          <div class="card-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="card-data">
            <div class="card-value">{{ countData.userCount }}</div>
            <div class="card-label">用户总数</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="card-content repair-card">
          <div class="card-icon">
            <el-icon><Tools /></el-icon>
          </div>
          <div class="card-data">
            <div class="card-value">{{ countData.repairRecordCount }}</div>
            <div class="card-label">报修总数</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="card-content resource-card">
          <div class="card-icon">
            <el-icon><Box /></el-icon>
          </div>
          <div class="card-data">
            <div class="card-value">{{ countData.resourceCount }}</div>
            <div class="card-label">资源总数</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="card-content post-card">
          <div class="card-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="card-data">
            <div class="card-value">{{ countData.postCount }}</div>
            <div class="card-label">帖子总数</div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-section">
      <el-card shadow="hover">
        <div class="chart-container">
          <div id="repairTypeChart" class="chart" v-loading="loading"></div>
        </div>
      </el-card>
    </div>
    
    <!-- 系统介绍 -->
    <div class="system-intro">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>校园后勤报修系统介绍</h3>
          </div>
        </template>
        <div class="intro-content">
          <p>校园后勤报修系统是为提高校园后勤维修服务质量而开发的综合管理平台，通过该系统实现对校园内设备设施报修、维修进度跟踪、维修结果反馈等全流程的管理。</p>
          <p>系统主要功能包括：</p>
          <ul>
            <li><strong>用户管理：</strong>管理系统内的用户信息，包括普通用户、维修人员和管理员。</li>
            <li><strong>维修类型管理：</strong>设置和管理不同类型的维修项目。</li>
            <li><strong>报修记录管理：</strong>处理用户提交的报修申请，分配维修人员，跟踪维修进度。</li>
            <li><strong>社区交流：</strong>通过帖子和评论功能，为师生提供交流平台。</li>
            <li><strong>消息管理：</strong>发布公告、查看用户留言，增强系统与用户的互动性。</li>
          </ul>
          <p>通过本系统，学校可以提高后勤维修工作的效率和透明度，使师生能够便捷地报修并跟踪维修进度，提升校园服务质量。</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-section {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #1976d2, #64b5f6);
  color: white;
  border-radius: 8px;
  padding: 20px;
}

.welcome-content h1 {
  margin: 0;
  font-size: 24px;
  margin-bottom: 10px;
}

.welcome-content p {
  margin: 0;
  font-size: 16px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.card-icon {
  font-size: 48px;
  margin-right: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  color: white;
}

.user-card .card-icon {
  background-color: #1976d2;
}

.repair-card .card-icon {
  background-color: #ff9800;
}

.resource-card .card-icon {
  background-color: #4caf50;
}

.post-card .card-icon {
  background-color: #e91e63;
}

.card-data {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
  line-height: 1;
}

.card-label {
  color: #666;
  font-size: 14px;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-container {
  padding: 10px;
}

.chart {
  height: 400px;
  width: 100%;
}

.system-intro {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.intro-content {
  line-height: 1.8;
}

.intro-content p {
  margin-bottom: 10px;
}

.intro-content ul {
  padding-left: 20px;
}

.intro-content li {
  margin-bottom: 5px;
}

@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr;
  }
}
</style>