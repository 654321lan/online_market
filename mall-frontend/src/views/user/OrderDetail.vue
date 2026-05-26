<template>
  <div class="order-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
          <h2>订单详情</h2>
          <el-tag :type="getStatusType(order.status)" size="large">{{ getStatusText(order.status) }}</el-tag>
        </div>
      </template>

      <!-- 订单基本信息 -->
      <div class="section">
        <h3>订单信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ order.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ order.shipTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ order.finishTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 物流信息 -->
      <div class="section" v-if="order.trackingNo">
        <h3>物流信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="快递公司">{{ order.expressCompany }}</el-descriptions-item>
          <el-descriptions-item label="运单号">
            <span class="tracking-no">{{ order.trackingNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ order.shipTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物流状态">
            <el-tag v-if="order.status === 2" type="info">运输中</el-tag>
            <el-tag v-else-if="order.status === 3" type="success">已签收</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 收货信息 -->
      <div class="section">
        <h3>收货信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ order.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 商品列表 -->
      <div class="section">
        <h3>商品信息</h3>
        <el-table :data="orderItems" border>
          <el-table-column label="商品" min-width="300">
            <template #default="{ row }">
              <div class="product-cell">
                <img :src="row.productImage" class="product-img" />
                <div class="product-info">
                  <p class="product-name">{{ row.productName }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" prop="price" width="120" align="center">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="100" align="center" />
          <el-table-column label="小计" width="120" align="center">
            <template #default="{ row }">
              <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 订单金额 -->
      <div class="section amount-section">
        <div class="amount-row" v-if="order.originalAmount">
          <span>商品原价：</span>
          <span :style="order.discountAmount > 0 ? 'text-decoration: line-through; color: #999' : ''">¥{{ order.originalAmount }}</span>
        </div>
        <div class="amount-row" v-if="order.memberLevel > 0">
          <span>会员等级：</span>
          <span style="color: #e6a23c">{{ memberLevelName(order.memberLevel) }}（{{ formatDiscount(order.discount) }}）</span>
        </div>
        <div class="amount-row" v-if="order.discountAmount > 0">
          <span>会员优惠：</span>
          <span style="color: #67c23a">-¥{{ order.discountAmount }}</span>
        </div>
        <div class="amount-row total">
          <span>实付金额：</span>
          <span class="total-amount">¥{{ order.totalAmount }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button v-if="order.status === 0" type="primary" @click="handlePay">去支付</el-button>
        <el-button v-if="order.status === 0" @click="handleCancel">取消订单</el-button>
        <el-button v-if="order.status === 2" type="success" @click="handleReceive">确认收货</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getOrderDetail, getOrderItems, payOrder, cancelOrder, receiveOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const order = ref({})
const orderItems = ref([])

const statusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '待发货', type: 'primary' },
  2: { text: '待收货', type: 'info' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'danger' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const levelNames = { 0: '普通会员', 1: '白银会员', 2: '黄金会员', 3: '钻石会员' }
const memberLevelName = (level) => levelNames[level] || '普通会员'
const formatDiscount = (discount) => {
  if (!discount || discount == 1) return '无折扣'
  return (discount * 10).toFixed(1) + '折'
}

const loadOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = route.params.id
    const [orderRes, itemsRes] = await Promise.all([
      getOrderDetail(orderId),
      getOrderItems(orderId)
    ])
    order.value = orderRes.data || {}
    orderItems.value = itemsRes.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/user/order')
}

const handlePay = async () => {
  try {
    await ElMessageBox.confirm('确认支付该订单吗？', '提示', {
      confirmButtonText: '确认支付',
      type: 'warning'
    })
    await payOrder(order.value.id)
    ElMessage.success('支付成功')
    loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleReceive = async () => {
  try {
    await ElMessageBox.confirm('确认收货吗？', '提示', { type: 'warning' })
    await receiveOrder(order.value.id)
    ElMessage.success('确认收货成功')
    loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.order-detail-page {
  padding: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  flex: 1;
}

.section {
  margin-bottom: 30px;
}

.section h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin: 0;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.tracking-no {
  color: #409eff;
  font-weight: bold;
  letter-spacing: 1px;
}

.amount-section {
  background: #fafafa;
  padding: 20px;
  border-radius: 8px;
}

.amount-row {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  margin-bottom: 10px;
  font-size: 14px;
}

.amount-row.total {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
  font-size: 16px;
  font-weight: bold;
}

.total-amount {
  color: #f56c6c;
  font-size: 24px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>
