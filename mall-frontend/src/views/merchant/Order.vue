<template>
  <div class="order-page">
    <el-card>
      <template #header>
        <h2>订单管理</h2>
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
            <span>订单编号：{{ order.orderNo }}</span>
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
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
              <p>下单时间：{{ formatTime(order.createTime) }}</p>
              <p>收货人：{{ order.receiverName }}</p>
              <p>联系电话：{{ order.receiverPhone }}</p>
              <p>收货地址：{{ order.receiverAddress }}</p>
              <p v-if="order.memberLevel > 0" style="color: #e6a23c">
                {{ memberLevelName(order.memberLevel) }} · {{ formatMemberDiscount(order.discount) }}
              </p>
              <p v-if="order.discountAmount > 0" style="color: #67c23a">会员优惠：-¥{{ order.discountAmount }}</p>
              <p class="order-total">实付金额：<span>¥{{ order.totalAmount }}</span></p>
            </div>
          </div>
          
          <div class="order-actions">
            <el-button v-if="order.status === 1" type="primary" size="small" @click="handleShip(order.id)">
              订单发货
            </el-button>
            <el-button size="small" @click="viewOrderDetail(order)">查看详情</el-button>
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
    
    <!-- 发货对话框 -->
    <el-dialog v-model="shipVisible" title="订单发货" width="500px">
      <el-form :model="shipForm" :rules="shipRules" ref="shipFormRef" label-width="100px">
        <el-form-item label="快递公司" prop="expressCompany">
          <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="京东物流" value="京东物流" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="邮政快递包裹" value="邮政快递包裹" />
          </el-select>
        </el-form-item>
        <el-form-item label="运单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入运单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>
    
    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="800px">
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span class="price">¥{{ currentOrder.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="会员等级" v-if="currentOrder.memberLevel > 0">
            <el-tag type="warning" effect="dark">{{ memberLevelName(currentOrder.memberLevel) }}</el-tag>
            <span style="margin-left: 8px; color: #e6a23c">{{ formatMemberDiscount(currentOrder.discount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="会员优惠" v-if="currentOrder.discountAmount > 0">
            <span style="color: #67c23a; font-weight: bold">-¥{{ currentOrder.discountAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="订单备注" :span="2">
            {{ currentOrder.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <h3>商品明细</h3>
        <el-table :data="currentOrder.items" style="margin-top: 15px">
          <el-table-column label="商品图片" width="100">
            <template #default="{ row }">
              <img :src="row.productImage" class="product-img" />
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <span class="price">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="price">¥{{ row.totalAmount }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantOrders, getMerchantOrderItems, shipOrder } from '@/api/merchant'
import { useMerchantStore } from '@/stores/merchant'

const merchantStore = useMerchantStore()

const orderList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeStatus = ref('all')
const detailVisible = ref(false)
const currentOrder = ref(null)
const shipVisible = ref(false)
const shipFormRef = ref()
const currentShipOrderId = ref(null)

const shipForm = ref({
  expressCompany: '',
  trackingNo: ''
})

const shipRules = {
  expressCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  trackingNo: [{ required: true, message: '请输入运单号', trigger: 'blur' }]
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
const formatMemberDiscount = (discount) => {
  if (!discount || discount == 1) return '无折扣'
  return (discount * 10).toFixed(1) + '折'
}

const loadOrders = async () => {
  try {
    // 将 'all' 转换为 undefined，其他状态转换为数字
    const statusParam = activeStatus.value === 'all' ? undefined : Number(activeStatus.value)
    
    const res = await getMerchantOrders({
      merchantId: merchantStore.merchantInfo.id,
      page: currentPage.value,
      size: pageSize.value,
      status: statusParam
    })
    
    const orders = res.data.records || []
    
    // 加载每个订单的明细
    for (let order of orders) {
      const itemsRes = await getMerchantOrderItems(order.id)
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

const handleShip = (id) => {
  currentShipOrderId.value = id
  shipForm.value = { expressCompany: '', trackingNo: '' }
  shipVisible.value = true
}

const submitShip = async () => {
  await shipFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await shipOrder(currentShipOrderId.value, shipForm.value.trackingNo, shipForm.value.expressCompany)
      ElMessage.success('发货成功')
      shipVisible.value = false
      loadOrders()
    } catch (error) {
      console.error(error)
    }
  })
}

const viewOrderDetail = (order) => {
  currentOrder.value = order
  detailVisible.value = true
}

const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : ''
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-page {
  padding: 0;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 20px;
}

.order-item {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
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
  padding: 15px;
  background: #fafafa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.8;
}

.order-total {
  margin-top: 10px;
  font-weight: bold;
}

.order-total span {
  color: #f56c6c;
  font-size: 18px;
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
  margin-top: 20px;
}

.order-detail {
  padding: 0 20px;
}

.order-detail h3 {
  font-size: 16px;
  margin: 20px 0 10px 0;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}
</style>

