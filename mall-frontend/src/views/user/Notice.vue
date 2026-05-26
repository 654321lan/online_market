<template>
  <div class="notice-page">
    <el-card>
      <template #header>
        <h2>公告通知</h2>
      </template>
      
      <div v-if="noticeList.length > 0" class="notice-list">
        <div
          v-for="notice in noticeList"
          :key="notice.id"
          class="notice-item"
          @click="viewNotice(notice)"
        >
          <div class="notice-header">
            <h3 class="notice-title">{{ notice.title }}</h3>
            <el-tag :type="getTypeTag(notice.type)" size="small">
              {{ getTypeText(notice.type) }}
            </el-tag>
          </div>
          <p class="notice-time">{{ formatTime(notice.createTime) }}</p>
        </div>
      </div>
      
      <el-empty v-else description="暂无公告" />
      
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
    
    <!-- 公告详情对话框 -->
    <el-dialog v-model="detailVisible" :title="currentNotice?.title" width="700px">
      <div class="notice-detail">
        <div class="detail-meta">
          <el-tag :type="getTypeTag(currentNotice?.type)" size="small">
            {{ getTypeText(currentNotice?.type) }}
          </el-tag>
          <span class="detail-time">{{ formatTime(currentNotice?.createTime) }}</span>
        </div>
        <el-divider />
        <div class="detail-content">
          {{ currentNotice?.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNoticeList } from '@/api/notice'

const noticeList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const currentNotice = ref(null)

const typeMap = {
  1: { text: '平台公告', tag: 'primary' },
  2: { text: '系统通知', tag: 'info' },
  3: { text: '活动公告', tag: 'success' }
}

const getTypeText = (type) => typeMap[type]?.text || '未知'
const getTypeTag = (type) => typeMap[type]?.tag || 'info'

const loadNotices = async () => {
  try {
    const res = await getNoticeList(currentPage.value, pageSize.value)
    noticeList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadNotices()
}

const viewNotice = (notice) => {
  currentNotice.value = notice
  detailVisible.value = true
}

const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : ''
}

onMounted(() => {
  loadNotices()
})
</script>

<style scoped>
.notice-page {
  padding: 0;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 20px;
}

.notice-item {
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.notice-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notice-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin: 0;
}

.notice-time {
  color: #999;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.notice-detail {
  padding: 20px 0;
}

.detail-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-time {
  color: #999;
  font-size: 14px;
}

.detail-content {
  line-height: 1.8;
  color: #666;
  white-space: pre-wrap;
  font-size: 15px;
}
</style>

