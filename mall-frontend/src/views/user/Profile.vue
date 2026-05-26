<template>
  <div class="profile-page">
    <el-card>
      <template #header>
        <h2>个人信息</h2>
      </template>
      
      <el-form :model="userForm" :rules="rules" ref="formRef" label-width="100px" style="max-width: 600px">
        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" disabled>
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称">
            <template #prefix>
              <el-icon><Edit /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :on-error="handleAvatarError"
            :before-upload="beforeAvatarUpload"
            name="file"
          >
            <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
            <div v-else class="avatar-uploader-placeholder">
              <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传头像</div>
            </div>
          </el-upload>
          <div class="upload-tip">支持 jpg、png 格式，大小不超过 2MB</div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleUpdate">保存修改</el-button>
          <el-button @click="passwordVisible = true">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordUpdate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUser, updatePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref()
const passwordFormRef = ref()
const passwordVisible = ref(false)

const uploadUrl = 'http://localhost:8080/api/upload'

const userInfo = ref({})
const userForm = reactive({
  nickname: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo(userStore.userInfo.id)
    userInfo.value = res.data
    userForm.nickname = res.data.nickname || ''
    userForm.avatar = res.data.avatar || ''
  } catch (error) {
    console.error(error)
  }
}

const handleUpdate = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await updateUser({
        id: userStore.userInfo.id,
        nickname: userForm.nickname,
        avatar: userForm.avatar
      })
      
      ElMessage.success('修改成功')
      
      // 更新 store 中的用户信息
      userStore.setUserInfo({
        ...userStore.userInfo,
        nickname: userForm.nickname,
        avatar: userForm.avatar
      })
    } catch (error) {
      console.error(error)
    }
  })
}

const handlePasswordUpdate = async () => {
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await updatePassword(
        userStore.userInfo.id,
        passwordForm.oldPassword,
        passwordForm.newPassword
      )
      
      ElMessage.success('密码修改成功，请重新登录')
      passwordVisible.value = false
      
      // 退出登录
      setTimeout(() => {
        userStore.logout()
        window.location.href = '/login'
      }, 1500)
    } catch (error) {
      console.error(error)
    }
  })
}

const handleAvatarSuccess = (response, file) => {
  console.log('上传成功，返回数据：', response)
  if (response && response.code === 200) {
    // 后端返回的是 /uploads/xxx.jpg 格式
    let avatarUrl = response.data
    
    // 如果不是完整URL，补充服务器地址
    if (!avatarUrl.startsWith('http')) {
      avatarUrl = 'http://localhost:8080' + avatarUrl
    }
    
    userForm.avatar = avatarUrl
    console.log('设置头像URL：', avatarUrl)
    ElMessage.success('头像上传成功')
  } else {
    console.error('上传失败：', response)
    ElMessage.error(response?.message || '上传失败')
  }
}

const handleAvatarError = (error) => {
  console.error('上传失败', error)
  ElMessage.error('头像上传失败，请重试')
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  padding: 0;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.avatar-uploader-placeholder {
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-text {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}
</style>

