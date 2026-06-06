<template>
  <div class="user-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>用户管理</h2>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名或昵称"
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>
      
      <!-- 用户列表 -->
      <el-table :data="userList" style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="username" label="用户名" min-width="180" />
        <el-table-column prop="nickname" label="昵称" min-width="180" />
        <el-table-column label="头像" width="120">
          <template #default="{ row }">
            <img v-if="row.avatar" :src="row.avatar" class="avatar" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="会员等级" width="130">
          <template #default="{ row }">
            <el-tag :type="row.memberLevel === 0 ? 'info' : row.memberLevel === 1 ? '' : row.memberLevel === 2 ? 'warning' : 'danger'" effect="dark">
              {{ levelNames[row.memberLevel] || '普通会员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="余额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.balance || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="累计充值" width="120">
          <template #default="{ row }">
            ¥{{ row.totalRecharge || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEditLevel(row)">调级</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
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

    <!-- 调级对话框 -->
    <el-dialog v-model="levelDialogVisible" title="调整会员等级" width="400px">
      <el-form label-width="100px">
        <el-form-item label="用户">
          <span>{{ editingUser?.nickname || editingUser?.username }} (ID: {{ editingUser?.id }})</span>
        </el-form-item>
        <el-form-item label="当前等级">
          <el-tag>{{ levelNames[editingUser?.memberLevel] || '普通会员' }}</el-tag>
        </el-form-item>
        <el-form-item label="新等级">
          <el-select v-model="newLevel" style="width: 100%">
            <el-option v-for="(name, key) in levelNames" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="levelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLevelChange">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getUserList, updateUserStatus } from '@/api/admin'
import { updateUserLevel } from '@/api/member'

const userList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const levelDialogVisible = ref(false)
const editingUser = ref(null)
const newLevel = ref(0)
const levelNames = { 0: '普通会员', 1: '白银会员', 2: '黄金会员', 3: '钻石会员' }

const loadUsers = async () => {
  try {
    const res = await getUserList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    
    userList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadUsers()
}

const handleStatusChange = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const text = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${text}该用户吗？`, '提示', {
      type: 'warning'
    })
    
    await updateUserStatus(user.id, newStatus)
    ElMessage.success(`${text}成功`)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : ''
}

const handleEditLevel = (user) => {
  editingUser.value = user
  newLevel.value = user.memberLevel || 0
  levelDialogVisible.value = true
}

const submitLevelChange = async () => {
  try {
    await updateUserLevel(editingUser.value.id, newLevel.value)
    ElMessage.success('等级调整成功')
    levelDialogVisible.value = false
    loadUsers()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-page {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.search-bar {
  display: flex;
  gap: 10px;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

