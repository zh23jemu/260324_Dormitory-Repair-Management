<template>
  <div class="statistics-container">
    <div class="filter-section">
      <el-card>
        <el-form :inline="true" :model="timeRange">
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="timeRange.dates"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="handleTimeChange"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="refreshData">刷新数据</el-button>
            <el-button @click="resetTimeRange">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon total">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">总报修数</div>
            <div class="stat-value">{{ totalCount }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="charts-grid">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>维修类型统计</span>
          </div>
        </template>
        <div ref="typeChartRef" class="chart"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>维修地点统计</span>
          </div>
        </template>
        <div ref="locationChartRef" class="chart"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>报修状态统计</span>
          </div>
        </template>
        <div ref="statusChartRef" class="chart"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, reactive } from 'vue'
import * as echarts from 'echarts'
import { 
  getRepairTypeStatistics, 
  getRepairLocationStatistics, 
  getRepairTypeStatisticsByTime,
  getRepairLocationStatisticsByTime,
  getTotalRepairCount,
  getRepairStatusStatistics
} from '@/api/statistics'
import { Document } from '@element-plus/icons-vue'

const typeChartRef = ref(null)
const locationChartRef = ref(null)
const statusChartRef = ref(null)
let typeChart = null
let locationChart = null
let statusChart = null

const totalCount = ref(0)
const timeRange = reactive({
  dates: null
})

const initCharts = () => {
  if (typeChartRef.value) {
    typeChart = echarts.init(typeChartRef.value)
  }
  if (locationChartRef.value) {
    locationChart = echarts.init(locationChartRef.value)
  }
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
  }
  
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  typeChart?.resize()
  locationChart?.resize()
  statusChart?.resize()
}

const loadTypeStatistics = async () => {
  try {
    let res
    if (timeRange.dates && timeRange.dates.length === 2) {
      res = await getRepairTypeStatisticsByTime({
        startTime: timeRange.dates[0],
        endTime: timeRange.dates[1]
      })
    } else {
      res = await getRepairTypeStatistics()
    }
    
    if (res.code === 200 && res.data) {
      const option = {
        title: {
          text: '维修类型分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle'
        },
        series: [
          {
            name: '维修类型',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b}: {c}'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 16,
                fontWeight: 'bold'
              }
            },
            data: res.data.map(item => ({
              value: item.count,
              name: item.name
            }))
          }
        ]
      }
      typeChart?.setOption(option)
    }
  } catch (error) {
    console.error('加载维修类型统计失败:', error)
  }
}

const loadLocationStatistics = async () => {
  try {
    let res
    if (timeRange.dates && timeRange.dates.length === 2) {
      res = await getRepairLocationStatisticsByTime({
        startTime: timeRange.dates[0],
        endTime: timeRange.dates[1]
      })
    } else {
      res = await getRepairLocationStatistics()
    }
    
    if (res.code === 200 && res.data) {
      const option = {
        title: {
          text: '维修地点分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
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
          data: res.data.map(item => item.name),
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '报修数量',
            type: 'bar',
            data: res.data.map(item => item.count),
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
      locationChart?.setOption(option)
    }
  } catch (error) {
    console.error('加载维修地点统计失败:', error)
  }
}

const loadStatusStatistics = async () => {
  try {
    const res = await getRepairStatusStatistics()
    
    if (res.code === 200 && res.data) {
      const option = {
        title: {
          text: '报修状态统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle'
        },
        series: [
          {
            name: '报修状态',
            type: 'pie',
            radius: '60%',
            data: res.data.map(item => ({
              value: item.count,
              name: item.name
            })),
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      statusChart?.setOption(option)
    }
  } catch (error) {
    console.error('加载报修状态统计失败:', error)
  }
}

const loadTotalCount = async () => {
  try {
    const res = await getTotalRepairCount()
    if (res.code === 200) {
      totalCount.value = res.data
    }
  } catch (error) {
    console.error('加载总报修数失败:', error)
  }
}

const handleTimeChange = () => {
  refreshData()
}

const resetTimeRange = () => {
  timeRange.dates = null
  refreshData()
}

const refreshData = () => {
  loadTypeStatistics()
  loadLocationStatistics()
  loadStatusStatistics()
  loadTotalCount()
}

onMounted(() => {
  initCharts()
  refreshData()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  typeChart?.dispose()
  locationChart?.dispose()
  statusChart?.dispose()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card {
  min-height: 400px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.chart {
  width: 100%;
  height: 350px;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
