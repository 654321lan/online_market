<template>
  <div class="address-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>收货地址</h2>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增地址
          </el-button>
        </div>
      </template>
      
      <div v-if="addressList.length > 0" class="address-list">
        <div
          v-for="address in addressList"
          :key="address.id"
          class="address-item"
          :class="{ 'is-default': address.isDefault === 1 }"
        >
          <div class="address-info">
            <div class="address-header">
              <span class="receiver-name">{{ address.receiverName }}</span>
              <span class="receiver-phone">{{ address.receiverPhone }}</span>
              <el-tag v-if="address.isDefault === 1" type="success" size="small">默认</el-tag>
            </div>
            <p class="address-detail">
              {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
            </p>
          </div>
          
          <div class="address-actions">
            <el-button size="small" @click="handleEdit(address)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              v-if="address.isDefault !== 1"
              size="small"
              type="success"
              @click="handleSetDefault(address.id)"
            >
              <el-icon><Star /></el-icon>
              设为默认
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(address.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无收货地址" />
    </el-card>
    
    <!-- 地址表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editMode ? '编辑地址' : '新增地址'"
      width="500px"
    >
      <el-form :model="addressForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号码">
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="请输入省份">
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="请输入城市">
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="区县" prop="district">
          <el-input v-model="addressForm.district" placeholder="请输入区县">
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model="addressForm.detail"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址"
          >
            <template #prefix>
              <el-icon><LocationInformation /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="默认地址">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
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
import {
  getAddressList,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
} from '@/api/address'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref()
const dialogVisible = ref(false)
const editMode = ref(false)
const addressList = ref([])

const addressForm = reactive({
  id: null,
  userId: null,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const loadAddressList = async () => {
  try {
    const res = await getAddressList(userStore.userInfo.id)
    addressList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleAdd = () => {
  editMode.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (address) => {
  editMode.value = true
  Object.assign(addressForm, address)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      addressForm.userId = userStore.userInfo.id
      
      if (editMode.value) {
        await updateAddress(addressForm)
        ElMessage.success('修改成功')
      } else {
        await addAddress(addressForm)
        ElMessage.success('添加成功')
      }
      
      dialogVisible.value = false
      loadAddressList()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSetDefault = async (id) => {
  try {
    await setDefaultAddress(id, userStore.userInfo.id)
    ElMessage.success('设置成功')
    loadAddressList()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      type: 'warning'
    })
    
    await deleteAddress(id)
    ElMessage.success('删除成功')
    loadAddressList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const resetForm = () => {
  addressForm.id = null
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.province = ''
  addressForm.city = ''
  addressForm.district = ''
  addressForm.detail = ''
  addressForm.isDefault = 0
}

onMounted(() => {
  loadAddressList()
})
</script>

<style scoped>
.address-page {
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

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.address-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s;
}

.address-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.address-item.is-default {
  border-color: #67c23a;
  background: #f0f9ff;
}

.address-info {
  margin-bottom: 15px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.receiver-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.receiver-phone {
  color: #666;
}

.address-detail {
  color: #666;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  gap: 10px;
}
</style>

