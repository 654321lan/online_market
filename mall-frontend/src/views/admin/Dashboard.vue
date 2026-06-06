<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <!-- 数据统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #1890ff">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-title">用户总数</p>
              <p class="stat-value">{{ statistics.userCount || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #52c41a">
              <el-icon :size="32"><Shop /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-title">商家总数</p>
              <p class="stat-value">{{ statistics.merchantCount || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fa8c16">
              <el-icon :size="32"><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-title">商品总数</p>
              <p class="stat-value">{{ statistics.productCount || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f5222d">
              <el-icon :size="32"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-title">交易总额</p>
              <p class="stat-value">¥{{ statistics.totalAmount || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表统计 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>订单状态分布</h3>
          </template>
          <div ref="orderChartRef" style="width: 100%; height: 350px"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>商品状态统计</h3>
          </template>
          <div ref="productChartRef" style="width: 100%; height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>快捷操作</h3>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/user')">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
            <el-button type="success" @click="$router.push('/admin/merchant')">
              <el-icon><Shop /></el-icon>
              商家管理
            </el-button>
            <el-button type="warning" @click="$router.push('/admin/category')">
              <el-icon><Menu /></el-icon>
              分类管理
            </el-button>
            <el-button type="danger" @click="$router.push('/admin/notice')">
              <el-icon><Bell /></el-icon>
              公告管理
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>平台信息</h3>
          </template>
          <div class="platform-info">
            <div class="info-item">
              <div class="info-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                <el-icon :size="28"><Document /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">订单总数</div>
                <div class="info-value">{{ statistics.orderCount || 0 }}</div>
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
                <el-icon :size="28"><Goods /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">在售商品</div>
                <div class="info-value">{{ statistics.productCount || 0 }}</div>
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
                <el-icon :size="28"><User /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">活跃用户</div>
                <div class="info-value">{{ statistics.userCount || 0 }}</div>
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
                <el-icon :size="28"><Shop /></el-icon>
              </div>
              <div class="info-content">
                <div class="info-label">合作商家</div>
                <div class="info-value">{{ statistics.merchantCount || 0 }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Document } from '@element-plus/icons-vue'
import { getOverviewStatistics } from '@/api/admin'

const statistics = ref({
  userCount: 0,
  merchantCount: 0,
  productCount: 0,
  orderCount: 0,
  totalAmount: 0,
  orderStatus: {},
  productStatus: {}
})

const orderChartRef = ref()
const productChartRef = ref()
let orderChart = null
let productChart = null

const loadStatistics = async () => {
  try {
    const res = await getOverviewStatistics()
    statistics.value = res.data || {}
    
    await nextTick()
    initCharts()
  } catch (error) {
    console.error(error)
  }
}

const initCharts = () => {
  // 订单状态饼图
  if (orderChartRef.value) {
    orderChart = echarts.init(orderChartRef.value)
    const orderData = Object.entries(statistics.value.orderStatus || {}).map(([name, value]) => ({
      name,
      value
    }))
    
    orderChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '订单状态',
          type: 'pie',
          radius: '60%',
          data: orderData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }
  
  // 商品状态柱状图
  if (productChartRef.value) {
    productChart = echarts.init(productChartRef.value)
    const productData = statistics.value.productStatus || {}
    
    productChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: Object.keys(productData)
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '数量',
          type: 'bar',
          data: Object.values(productData),
          itemStyle: {
            color: '#5470c6'
          },
          label: {
            show: true,
            position: 'top'
          }
        }
      ]
    })
  }
}

onMounted(() => {
  loadStatistics()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    orderChart?.resize()
    productChart?.resize()
  })
})
</script>

<style scoped>
.dashboard-page {
  padding: 0;
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
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.quick-actions .el-button {
  width: 100%;
  height: 60px;
  font-size: 16px;
}

.platform-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f7f8fa;
  border-radius: 10px;
  transition: all 0.3s;
}

.info-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.info-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 15px;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
}

.info-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.info-value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
}
</style>
