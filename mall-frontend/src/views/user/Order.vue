<template>
  <div class="order-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>我的订单</h2>
        </div>
      </template>
      
      <!-- 订单状态筛选 -->
      <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待支付" name="0" />
        <el-tab-pane label="待发货" name="1" />
        <el-tab-pane label="待收货" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>
      
      <!-- 订单列表 -->
      <div v-if="orderList.length > 0" class="order-list">
        <div v-for="order in orderList" :key="order.id" class="order-item">
          <div class="order-header">
            <div class="order-no">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
              </svg>
              <span>订单编号：{{ order.orderNo }}</span>
            </div>
            <el-tag :type="getStatusType(order.status)" effect="dark" round>{{ getStatusText(order.status) }}</el-tag>
          </div>
          
          <div class="order-content">
            <div class="order-products">
              <div v-for="item in order.items" :key="item.id" class="product-item">
                <img :src="item.productImage" class="product-img" />
                <div class="product-detail">
                  <p class="product-name">{{ item.productName }}</p>
                  <p class="product-price">¥{{ item.price }} × {{ item.quantity }}</p>
                </div>
              </div>
            </div>
            
            <div class="order-info">
              <div class="info-row">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8c8c8c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                <span>收货人：{{ order.receiverName }}</span>
              </div>
              <div class="info-row">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8c8c8c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.79 19.79 0 0 1 2.12 4.11 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
                </svg>
                <span>联系电话：{{ order.receiverPhone }}</span>
              </div>
              <div class="info-row">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8c8c8c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3"/>
                </svg>
                <span>收货地址：{{ order.receiverAddress }}</span>
              </div>
              <div class="info-row member-level-row" v-if="order.memberLevel > 0">
                <span>{{ memberLevelName(order.memberLevel) }} · {{ formatDiscount(order.discount) }}</span>
              </div>
              <div class="info-row" v-if="order.discountAmount > 0">
                <span style="color: #67c23a">会员优惠：-¥{{ order.discountAmount }}</span>
              </div>
              <div class="info-row order-total">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#f56c6c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="1" x2="12" y2="23"/>
                  <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                </svg>
                <span>实付金额：<strong>¥{{ order.totalAmount }}</strong></span>
              </div>
            </div>
          </div>
          
          <!-- 物流信息 -->
          <div v-if="order.trackingNo && (order.status === 2 || order.status === 3)" class="logistics-info">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="1" y="3" width="15" height="13"/>
              <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
              <circle cx="5.5" cy="18.5" r="2.5"/>
              <circle cx="18.5" cy="18.5" r="2.5"/>
            </svg>
            <span>{{ order.expressCompany }}：{{ order.trackingNo }}</span>
          </div>
          
          <div class="order-actions">
            <el-button v-if="order.status === 0" type="primary" size="small" @click="handlePay(order)">去支付</el-button>
            <el-button v-if="order.status === 0" size="small" @click="handleCancel(order.id)">取消订单</el-button>
            <el-button v-if="order.status === 2" type="success" size="small" @click="handleReceive(order.id)">确认收货</el-button>
            <el-button v-if="order.status === 3" size="small" @click="handleReview(order)">评价</el-button>
            <el-button
              v-if="(order.status === 2 || order.status === 3) && !activeRefundOrderIds.includes(order.id)"
              type="warning" size="small" @click="handleRefund(order)"
            >退换货</el-button>
            <el-tag
              v-if="(order.status === 2 || order.status === 3) && activeRefundOrderIds.includes(order.id)"
              type="warning" effect="plain" size="small" class="refund-tag"
            >
              <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="vertical-align: -1px; margin-right: 3px">
                <polyline points="23 4 23 10 17 10"/>
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
              </svg>
              退换货处理中
            </el-tag>
            <el-button size="small" @click="viewOrderDetail(order.id)">查看详情</el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无订单" />
      
      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
    
    <!-- 评价对话框 -->
    <el-dialog v-model="reviewVisible" title="订单评价" width="600px">
      <div v-if="currentOrder" class="review-dialog-content">
        <!-- 显示订单中的所有商品 -->
        <div class="review-products">
          <h4>本次评价将应用到以下商品：</h4>
          <div class="review-product-list">
            <div v-for="item in currentOrder.items" :key="item.id" class="review-product-item">
              <img :src="item.productImage" class="review-product-img" />
              <span class="review-product-name">{{ item.productName }}</span>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <el-form :model="reviewForm" label-width="80px">
          <el-form-item label="评分">
            <el-rate v-model="reviewForm.rating" />
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input v-model="reviewForm.content" type="textarea" :rows="5" placeholder="请输入评价内容" />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
    <!-- 退换货申请对话框 -->
    <el-dialog v-model="refundVisible" title="退换货申请" width="600px">
      <el-form :model="refundForm" :rules="refundRules" ref="refundFormRef" label-width="100px">
        <el-form-item label="订单编号">
          <el-input :value="currentRefundOrder?.orderNo" disabled />
        </el-form-item>
        <el-form-item label="申请类型" prop="type">
          <el-radio-group v-model="refundForm.type">
            <el-radio :label="1">退货退款</el-radio>
            <el-radio :label="2">换货</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="申请原因" prop="reason">
          <el-select v-model="refundForm.reason" placeholder="请选择申请原因" style="width: 100%">
            <el-option label="商品质量问题" value="商品质量问题" />
            <el-option label="商品破损" value="商品破损" />
            <el-option label="与描述不符" value="与描述不符" />
            <el-option label="尺码不合适" value="尺码不合适" />
            <el-option label="不想要了" value="不想要了" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="refundForm.description" type="textarea" :rows="4" placeholder="请详细描述退换货原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, getOrderItems, payOrder, cancelOrder, receiveOrder } from '@/api/order'
import { addReview } from '@/api/review'
import { applyRefund, getActiveRefundOrderIds } from '@/api/refund'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()

const orderList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeStatus = ref('all')
const reviewVisible = ref(false)
const currentOrder = ref(null)

const reviewForm = ref({
  rating: 5,
  content: ''
})

const refundVisible = ref(false)
const currentRefundOrder = ref(null)
const refundFormRef = ref()
const refundForm = ref({
  type: 1,
  reason: '',
  description: ''
})

const refundRules = {
  type: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
  reason: [{ required: true, message: '请选择申请原因', trigger: 'change' }]
}

const activeRefundOrderIds = ref([])

const loadActiveRefunds = async () => {
  try {
    const res = await getActiveRefundOrderIds(userStore.userInfo.id)
    activeRefundOrderIds.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

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

const loadOrders = async () => {
  try {
    // 将 'all' 转换为 undefined，其他状态转换为数字
    const statusParam = activeStatus.value === 'all' ? undefined : Number(activeStatus.value)
    
    const res = await getOrderList({
      userId: userStore.userInfo.id,
      page: currentPage.value,
      size: pageSize.value,
      status: statusParam
    })
    
    const orders = res.data.records || []
    
    // 加载每个订单的明细
    for (let order of orders) {
      const itemsRes = await getOrderItems(order.id)
      order.items = itemsRes.data || []
    }
    
    orderList.value = orders
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handleStatusChange = () => {
  currentPage.value = 1
  loadOrders()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadOrders()
}

const handlePay = async (order) => {
  try {
    await ElMessageBox.confirm('确认支付该订单吗？', '提示', {
      confirmButtonText: '确认支付',
      type: 'warning'
    })
    
    await payOrder(order.id)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      type: 'warning'
    })
    
    await cancelOrder(id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleReceive = async (id) => {
  try {
    await ElMessageBox.confirm('确认收货吗？', '提示', {
      type: 'warning'
    })
    
    await receiveOrder(id)
    ElMessage.success('确认收货成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleReview = (order) => {
  currentOrder.value = order
  reviewForm.value = {
    rating: 5,
    content: ''
  }
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  
  try {
    // 为订单中的所有商品创建评价，这样在每个商品详情页都能看到
    const reviewPromises = currentOrder.value.items.map(item => {
      return addReview({
        orderId: currentOrder.value.id,
        productId: item.productId,
        userId: userStore.userInfo.id,
        rating: reviewForm.value.rating,
        content: reviewForm.value.content
      })
    })
    
    // 等待所有评价都创建成功
    await Promise.all(reviewPromises)
    
    ElMessage.success(`评价成功！已为 ${currentOrder.value.items.length} 件商品添加评价`)
    reviewVisible.value = false
    loadOrders() // 刷新订单列表
  } catch (error) {
    console.error(error)
    // 如果是重复评价的错误，显示友好提示
    if (error.message && error.message.includes('已经评价')) {
      ElMessage.warning('该订单已经评价过了')
    } else {
      ElMessage.error('评价失败，请重试')
    }
  }
}

const handleRefund = (order) => {
  currentRefundOrder.value = order
  refundForm.value = { type: 1, reason: '', description: '' }
  refundVisible.value = true
}

const submitRefund = async () => {
  await refundFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await applyRefund({
        orderId: currentRefundOrder.value.id,
        userId: userStore.userInfo.id,
        type: refundForm.value.type,
        reason: refundForm.value.reason,
        description: refundForm.value.description
      })
      ElMessage.success('退换货申请已提交')
      refundVisible.value = false
      loadActiveRefunds()
    } catch (error) {
      console.error(error)
    }
  })
}

const viewOrderDetail = (id) => {
  router.push(`/user/order/${id}`)
}

onMounted(() => {
  loadOrders()
  loadActiveRefunds()
})
</script>

<style scoped>
.order-page {
  padding: 0;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 20px;
}

.order-item {
  border: 1px solid #eee;
  border-radius: 10px;
  overflow: hidden;
  transition: box-shadow 0.25s;
}

.order-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
  border-bottom: 1px solid #eee;
}

.order-no {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1890ff;
  font-weight: 500;
  font-size: 14px;
}

.order-no svg {
  color: #1890ff;
}

.order-content {
  display: flex;
  padding: 20px;
  gap: 30px;
}

.order-products {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-item {
  display: flex;
  gap: 15px;
}

.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.product-detail {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
}

.product-price {
  color: #999;
  font-size: 14px;
}

.order-info {
  width: 300px;
  padding: 18px;
  background: #fafafa;
  border-radius: 8px;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #666;
  line-height: 1.5;
}

.info-row svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.info-row.order-total {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px dashed #e0e0e0;
}

.info-row.order-total strong {
  color: #f56c6c;
  font-size: 18px;
}

.member-level-row span {
  color: #e6a23c;
  font-weight: 500;
  font-size: 13px;
}

.refund-tag {
  line-height: 28px;
}

.logistics-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #f0f9eb;
  color: #67c23a;
  font-size: 14px;
  border-top: 1px solid #eee;
}

.order-actions {
  display: flex;
  gap: 10px;
  padding: 15px 20px;
  background: #fafafa;
  border-top: 1px solid #eee;
  justify-content: flex-end;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.review-dialog-content {
  padding: 0 10px;
}

.review-products h4 {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #666;
}

.review-product-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.review-product-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
}

.review-product-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.review-product-name {
  flex: 1;
  font-size: 14px;
  color: #333;
}
</style>

