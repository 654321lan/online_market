<template>
  <div class="merchant-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>商家管理</h2>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增商家
          </el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商家账号或店铺名称"
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>
      
      <!-- 商家列表 -->
      <el-table :data="merchantList" style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="登录账号" width="150" />
        <el-table-column prop="shopName" label="店铺名称" width="200" />
        <el-table-column prop="shopDesc" label="店铺简介" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系方式" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'">
              {{ row.status === 1 ? '正常' : row.status === 2 ? '待审核' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 2" size="small" type="success" @click="handleApprove(row.id)">审核通过</el-button>
            <el-button v-if="row.status === 2" size="small" type="danger" @click="handleReject(row.id)">审核拒绝</el-button>
            <el-button
              v-if="row.status !== 2"
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
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
    
    <!-- 商家表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editMode ? '编辑商家' : '新增商家'"
      width="600px"
    >
      <el-form :model="merchantForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="merchantForm.username" placeholder="请输入登录账号" :disabled="editMode">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="登录密码" prop="password" v-if="!editMode">
          <el-input v-model="merchantForm.password" type="password" placeholder="请输入登录密码" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="merchantForm.shopName" placeholder="请输入店铺名称">
            <template #prefix>
              <el-icon><Shop /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="店铺简介" prop="shopDesc">
          <el-input
            v-model="merchantForm.shopDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入店铺简介"
          />
        </el-form-item>
        
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="merchantForm.contact" placeholder="请输入联系方式">
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="merchantForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, User, Lock, Shop, Phone } from '@element-plus/icons-vue'
import {
  getMerchantList,
  addMerchant,
  updateMerchantInfo,
  updateMerchantStatus,
  deleteMerchant
} from '@/api/admin'

const formRef = ref()
const dialogVisible = ref(false)
const editMode = ref(false)

const merchantList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

const merchantForm = reactive({
  id: null,
  username: '',
  password: '',
  shopName: '',
  shopDesc: '',
  contact: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
}

const loadMerchants = async () => {
  try {
    const res = await getMerchantList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    
    merchantList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadMerchants()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadMerchants()
}

const handleAdd = () => {
  editMode.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (merchant) => {
  editMode.value = true
  Object.assign(merchantForm, merchant)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (editMode.value) {
        await updateMerchantInfo({
          id: merchantForm.id,
          shopName: merchantForm.shopName,
          shopDesc: merchantForm.shopDesc,
          contact: merchantForm.contact,
          status: merchantForm.status
        })
        ElMessage.success('修改成功')
      } else {
        await addMerchant(merchantForm)
        ElMessage.success('添加成功')
      }
      
      dialogVisible.value = false
      loadMerchants()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleStatusChange = async (merchant) => {
  const newStatus = merchant.status === 1 ? 0 : 1
  const text = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${text}该商家吗？`, '提示', {
      type: 'warning'
    })
    
    await updateMerchantStatus(merchant.id, newStatus)
    ElMessage.success(`${text}成功`)
    loadMerchants()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商家吗？', '提示', {
      type: 'warning'
    })
    
    await deleteMerchant(id)
    ElMessage.success('删除成功')
    loadMerchants()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定审核通过该商家吗？', '审核确认', {
      type: 'warning'
    })
    await updateMerchantStatus(id, 1)
    ElMessage.success('审核通过')
    loadMerchants()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleReject = async (id) => {
  try {
    await ElMessageBox.confirm('确定拒绝该商家的开店申请吗？', '审核拒绝', {
      type: 'warning'
    })
    await updateMerchantStatus(id, 0)
    ElMessage.success('已拒绝')
    loadMerchants()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const resetForm = () => {
  merchantForm.id = null
  merchantForm.username = ''
  merchantForm.password = ''
  merchantForm.shopName = ''
  merchantForm.shopDesc = ''
  merchantForm.contact = ''
  merchantForm.status = 1
}

const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : ''
}

onMounted(() => {
  loadMerchants()
})
</script>

<style scoped>
.merchant-page {
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

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

