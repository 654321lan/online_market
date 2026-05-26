<template>
  <div class="member-page">
    <!-- 会员卡片 -->
    <el-card class="member-card" :body-style="{ padding: '0' }">
      <div class="member-banner" :class="'level-' + memberInfo.memberLevel">
        <div class="member-header">
          <div class="member-avatar">
            <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" />
            <el-icon v-else :size="40"><User /></el-icon>
          </div>
          <div class="member-detail">
            <h2>{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</h2>
            <div class="level-badge">
              <span class="level-icon">{{ levelIcons[memberInfo.memberLevel] || '👤' }}</span>
              <span class="level-name">{{ currentLevel?.name || '普通会员' }}</span>
            </div>
          </div>
          <div class="member-discount" v-if="currentLevel">
            <div class="discount-value">{{ formatDiscount(currentLevel.discount) }}</div>
            <div class="discount-label">购物折扣</div>
          </div>
        </div>
        <div class="member-stats">
          <div class="stat-item">
            <div class="stat-value">¥{{ memberInfo.balance || '0.00' }}</div>
            <div class="stat-label">账户余额</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">¥{{ memberInfo.totalRecharge || '0.00' }}</div>
            <div class="stat-label">累计充值</div>
          </div>
          <div class="stat-item" v-if="nextLevel">
            <div class="stat-value">¥{{ upgradeNeeded }}</div>
            <div class="stat-label">升级还需充值</div>
          </div>
          <div class="stat-item" v-else>
            <div class="stat-value">MAX</div>
            <div class="stat-label">已达最高等级</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 左侧：充值 + 等级说明 -->
      <el-col :span="10">
        <!-- 充值卡片 -->
        <el-card>
          <template #header><h3 style="margin:0">模拟充值</h3></template>
          <div class="recharge-section">
            <div class="quick-amounts">
              <el-button v-for="amt in [100, 300, 500, 1000, 2000, 5000]" :key="amt"
                :type="rechargeAmount === amt ? 'primary' : 'default'"
                @click="rechargeAmount = amt" round>
                ¥{{ amt }}
              </el-button>
            </div>
            <div class="custom-amount">
              <el-input-number v-model="rechargeAmount" :min="1" :max="99999" :step="100"
                controls-position="right" style="width: 200px" />
              <el-button type="primary" @click="handleRecharge" :loading="recharging" size="large">
                立即充值
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 等级说明 -->
        <el-card style="margin-top: 20px">
          <template #header><h3 style="margin:0">会员等级说明</h3></template>
          <el-table :data="allLevels" stripe>
            <el-table-column prop="name" label="等级" width="120">
              <template #default="{ row }">
                <span>{{ levelIcons[row.level] }} {{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="累计充值" width="120">
              <template #default="{ row }">
                ¥{{ row.minAmount }}
              </template>
            </el-table-column>
            <el-table-column label="折扣" width="100">
              <template #default="{ row }">
                <el-tag :type="row.level === 0 ? 'info' : row.level === 1 ? '' : row.level === 2 ? 'warning' : 'danger'" effect="dark">
                  {{ formatDiscount(row.discount) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-icon v-if="memberInfo.memberLevel >= row.level" color="#67c23a"><CircleCheck /></el-icon>
                <el-icon v-else color="#c0c4cc"><Lock /></el-icon>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：充值记录 -->
      <el-col :span="14">
        <el-card>
          <template #header><h3 style="margin:0">充值记录</h3></template>
          <el-table :data="rechargeRecords" stripe v-loading="recordsLoading">
            <el-table-column label="充值金额" width="150">
              <template #default="{ row }">
                <span style="color: #f56c6c; font-weight: bold">+¥{{ row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="充值时间" min-width="200">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="rechargeRecords.length === 0 && !recordsLoading" description="暂无充值记录" />
          <div class="pagination" v-if="recordTotal > 0">
            <el-pagination :current-page="recordPage" :page-size="10" :total="recordTotal"
              layout="total, prev, pager, next" @current-change="handleRecordPageChange" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMemberInfo, recharge, getRechargeRecords } from '@/api/member'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const memberInfo = ref({})
const currentLevel = ref(null)
const nextLevel = ref(null)
const allLevels = ref([])
const rechargeAmount = ref(100)
const recharging = ref(false)
const rechargeRecords = ref([])
const recordsLoading = ref(false)
const recordPage = ref(1)
const recordTotal = ref(0)

const levelIcons = { 0: '👤', 1: '🥈', 2: '🥇', 3: '💎' }

const upgradeNeeded = computed(() => {
  if (!nextLevel.value) return '0.00'
  const needed = parseFloat(nextLevel.value.minAmount) - parseFloat(memberInfo.value.totalRecharge || 0)
  return needed > 0 ? needed.toFixed(2) : '0.00'
})

const formatDiscount = (discount) => {
  if (!discount || discount == 1) return '无折扣'
  return (discount * 10).toFixed(1) + '折'
}

const formatTime = (time) => time ? new Date(time).toLocaleString() : ''

const loadMemberInfo = async () => {
  try {
    const res = await getMemberInfo(userStore.userInfo.id)
    const data = res.data
    memberInfo.value = {
      memberLevel: data.user.memberLevel || 0,
      balance: data.user.balance || '0.00',
      totalRecharge: data.user.totalRecharge || '0.00'
    }
    currentLevel.value = data.currentLevel
    nextLevel.value = data.nextLevel
    allLevels.value = data.allLevels || []
  } catch (e) {
    console.error(e)
  }
}

const loadRechargeRecords = async () => {
  recordsLoading.value = true
  try {
    const res = await getRechargeRecords(userStore.userInfo.id, { page: recordPage.value, size: 10 })
    rechargeRecords.value = res.data.records || []
    recordTotal.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    recordsLoading.value = false
  }
}

const handleRecharge = async () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning('请输入有效充值金额')
    return
  }
  recharging.value = true
  try {
    const res = await recharge(userStore.userInfo.id, rechargeAmount.value)
    const data = res.data
    if (data.upgraded) {
      ElMessage.success(`充值成功！恭喜您升级为 ${data.newLevelName}！`)
    } else {
      ElMessage.success('充值成功！')
    }
    // 更新 store 中的用户信息
    userStore.setUserInfo({
      ...userStore.userInfo,
      memberLevel: data.memberLevel,
      balance: data.balance,
      totalRecharge: data.totalRecharge
    })
    loadMemberInfo()
    loadRechargeRecords()
  } catch (e) {
    console.error(e)
  } finally {
    recharging.value = false
  }
}

const handleRecordPageChange = (page) => {
  recordPage.value = page
  loadRechargeRecords()
}

onMounted(() => {
  loadMemberInfo()
  loadRechargeRecords()
})
</script>

<style scoped>
.member-page {
  padding: 0;
}

.member-banner {
  padding: 30px;
  border-radius: 8px;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.member-banner.level-1 {
  background: linear-gradient(135deg, #bdc3c7 0%, #95a5a6 100%);
}

.member-banner.level-2 {
  background: linear-gradient(135deg, #f7971e 0%, #ffd200 100%);
}

.member-banner.level-3 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.member-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 25px;
}

.member-avatar img {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.5);
  object-fit: cover;
}

.member-detail h2 {
  margin: 0 0 8px 0;
  font-size: 22px;
}

.level-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  background: rgba(255,255,255,0.2);
  padding: 4px 14px;
  border-radius: 20px;
  display: inline-flex;
}

.level-icon {
  font-size: 18px;
}

.member-discount {
  margin-left: auto;
  text-align: center;
  background: rgba(255,255,255,0.15);
  padding: 15px 25px;
  border-radius: 12px;
}

.discount-value {
  font-size: 28px;
  font-weight: bold;
}

.discount-label {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 4px;
}

.member-stats {
  display: flex;
  gap: 40px;
  padding-top: 20px;
  border-top: 1px solid rgba(255,255,255,0.2);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: bold;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 4px;
}

.recharge-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.quick-amounts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.custom-amount {
  display: flex;
  align-items: center;
  gap: 15px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
