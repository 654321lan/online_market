<template>
  <div class="refund-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>退换货申请</h2>
        </div>
      </template>
      
      <!-- 状态筛选 -->
      <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待处理" name="0" />
        <el-tab-pane label="已同意" name="1" />
        <el-tab-pane label="已拒绝" name="2" />
        <el-tab-pane label="已完成" name="3" />
      </el-tabs>
      
      <!-- 退换货列表 -->
      <div v-if="refundList.length > 0" class="refund-list">
        <div v-for="item in refundList" :key="item.id" class="refund-item">
          <div class="refund-header">
            <div class="refund-order-no">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
              </svg>
              <span>订单编号：{{ item.orderNo }}</span>
            </div>
            <el-tag :type="getStatusType(item.status)" effect="dark" round>{{ getStatusText(item.status) }}</el-tag>
          </div>
          
          <div class="refund-content">
            <div class="refund-info">
              <div class="refund-row">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8c8c8c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 11 12 14 22 4"/>
                  <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                </svg>
                <span><strong>申请类型：</strong></span>
                <el-tag size="small" :type="item.type === 1 ? 'danger' : 'warning'" effect="plain">
                  {{ item.type === 1 ? '退货退款' : '换货' }}
                </el-tag>
              </div>
              <div class="refund-row">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8c8c8c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12"/>
                  <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                <span><strong>申请原因：</strong>{{ item.reason }}</span>
              </div>
              <div class="refund-row">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8c8c8c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="17" y1="10" x2="3" y2="10"/>
                  <line x1="21" y1="6" x2="3" y2="6"/>
                  <line x1="21" y1="14" x2="3" y2="14"/>
                  <line x1="17" y1="18" x2="3" y2="18"/>
                </svg>
                <span><strong>详细描述：</strong>{{ item.description || '无' }}</span>
              </div>
              <div class="refund-row" v-if="item.type === 1">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#f56c6c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="1" x2="12" y2="23"/>
                  <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                </svg>
                <span><strong>退款金额：</strong><span class="amount">¥{{ item.refundAmount }}</span></span>
              </div>
              <div class="refund-row">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8c8c8c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
                <span><strong>申请时间：</strong>{{ formatTime(item.createTime) }}</span>
              </div>
            </div>
            
            <div v-if="item.merchantReply" class="merchant-reply">
              <div class="reply-header">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#409eff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <h4>商家回复</h4>
              </div>
              <p>{{ item.merchantReply }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无退换货记录" />
      
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getRefundList } from '@/api/refund'

const userStore = useUserStore()

const refundList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeStatus = ref('all')

const statusMap = {
  0: { text: '待处理', type: 'warning' },
  1: { text: '已同意', type: 'success' },
  2: { text: '已拒绝', type: 'danger' },
  3: { text: '已完成', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const loadRefunds = async () => {
  try {
    const statusParam = activeStatus.value === 'all' ? undefined : Number(activeStatus.value)
    
    const res = await getRefundList({
      userId: userStore.userInfo.id,
      page: currentPage.value,
      size: pageSize.value,
      status: statusParam
    })
    
    refundList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handleStatusChange = () => {
  currentPage.value = 1
  loadRefunds()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadRefunds()
}

const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : ''
}

onMounted(() => {
  loadRefunds()
})
</script>

<style scoped>
.refund-page {
  padding: 0;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.refund-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 20px;
}

.refund-item {
  border: 1px solid #eee;
  border-radius: 10px;
  overflow: hidden;
  transition: box-shadow 0.25s;
}

.refund-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.refund-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
  border-bottom: 1px solid #eee;
}

.refund-order-no {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1890ff;
  font-weight: 500;
  font-size: 14px;
}

.refund-order-no svg {
  color: #1890ff;
}

.refund-content {
  padding: 24px;
}

.refund-info {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.refund-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #333;
}

.refund-row svg {
  flex-shrink: 0;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.merchant-reply {
  margin-top: 20px;
  padding: 16px;
  background: #f0f9ff;
  border-radius: 8px;
  border-left: 3px solid #409eff;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.reply-header h4 {
  margin: 0;
  font-size: 14px;
  color: #409eff;
}

.merchant-reply p {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
