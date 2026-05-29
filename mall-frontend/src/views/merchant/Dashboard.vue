<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <!-- 数据统计卡片 -->
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #1890ff">
              <el-icon :size="32"><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-title">商品总数</p>
              <p class="stat-value">{{ productStats.productCount || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #52c41a">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-title">订单总数</p>
              <p class="stat-value">{{ salesStats.orderCount || 0 }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fa8c16">
              <el-icon :size="32"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-title">销售总额</p>
              <p class="stat-value">¥{{ salesStats.totalSales || 0 }}</p>
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
    
    <!-- 待处理订单 -->
    <el-card class="pending-orders" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <h3>待处理订单</h3>
          <el-button type="text" @click="$router.push('/merchant/order')">查看全部</el-button>
        </div>
      </template>
      
      <el-table :data="pendingOrders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" min-width="250" />
        <el-table-column label="下单时间" min-width="220">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="180">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="150">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="primary" size="small" @click="handleShip(row.id)">
              发货
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="pendingOrders.length === 0" description="暂无待处理订单" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getSalesStatistics, getProductStatistics, getMerchantOrders, shipOrder } from '@/api/merchant'
import { useMerchantStore } from '@/stores/merchant'

const merchantStore = useMerchantStore()

const salesStats = ref({
  totalSales: 0,
  orderCount: 0,
  completedCount: 0,
  orderStatus: {}
})

const productStats = ref({
  productCount: 0,
  onSale: 0,
  offSale: 0,
  lowStock: 0,
  productStatus: {}
})

const pendingOrders = ref([])
const orderChartRef = ref()
const productChartRef = ref()
let orderChart = null
let productChart = null

const statusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '待发货', type: 'primary' },
  2: { text: '待收货', type: 'info' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'danger' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const loadStatistics = async () => {
  try {
    const [salesRes, productRes] = await Promise.all([
      getSalesStatistics(merchantStore.merchantInfo.id),
      getProductStatistics(merchantStore.merchantInfo.id)
    ])
    
    salesStats.value = salesRes.data || {}
    productStats.value = productRes.data || {}
    
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
    const orderData = Object.entries(salesStats.value.orderStatus || {}).map(([name, value]) => ({
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
    const productData = productStats.value.productStatus || {}
    
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
            color: '#52c41a'
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

const loadPendingOrders = async () => {
  try {
    const res = await getMerchantOrders({
      merchantId: merchantStore.merchantInfo.id,
      page: 1,
      size: 10,
      status: 1
    })
    
    pendingOrders.value = res.data.records || []
  } catch (error) {
    console.error(error)
  }
}

const handleShip = async (id) => {
  try {
    await shipOrder(id)
    ElMessage.success('发货成功')
    loadPendingOrders()
    loadStatistics()
  } catch (error) {
    console.error(error)
  }
}

const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : ''
}

onMounted(() => {
  loadStatistics()
  loadPendingOrders()
  
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}
</style>
