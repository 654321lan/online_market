<template>
  <div class="user-layout">
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-content">
          <div class="logo" @click="$router.push('/user/home')">
            <el-icon :size="28"><ShoppingBag /></el-icon>
            <span>通用商城</span>
          </div>
          
          <div class="nav-menu">
            <el-menu mode="horizontal" :default-active="activeMenu" router>
              <el-menu-item index="/user/home">首页</el-menu-item>
              <el-menu-item index="/user/cart">
                <el-badge :value="userStore.cartCount" :hidden="userStore.cartCount === 0">
                  购物车
                </el-badge>
              </el-menu-item>
              <el-menu-item index="/user/order">我的订单</el-menu-item>
              <el-menu-item index="/user/refund">退换货</el-menu-item>
              <el-menu-item index="/user/member">会员中心</el-menu-item>
              <el-menu-item index="/user/notice">公告通知</el-menu-item>
            </el-menu>
          </div>
          
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="user-name">
                <el-icon><User /></el-icon>
                {{ userStore.userInfo?.nickname || userStore.userInfo?.username }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人信息
                  </el-dropdown-item>
                  <el-dropdown-item command="address">
                    <el-icon><Location /></el-icon>
                    收货地址
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <!-- 主体内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
      
      <!-- 底部 -->
      <el-footer class="footer">
        <p>&copy; 2025 通用商城系统. All rights reserved.</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

onMounted(() => {
  userStore.refreshCartCount()
})

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'address') {
    router.push('/user/address')
  }
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  padding: 0;
  height: 70px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 100%;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
}

.nav-menu {
  flex: 1;
  margin: 0 50px;
}

.nav-menu :deep(.el-menu) {
  background: transparent;
  border: none;
}

.nav-menu :deep(.el-menu-item) {
  color: white;
  font-size: 16px;
  border: none;
}

.nav-menu :deep(.el-menu-item:hover),
.nav-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.2) !important;
  color: white;
}

.user-info {
  color: white;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  font-size: 16px;
  color: white;
}

.main-content {
  max-width: 1600px;
  width: 95%;
  margin: 20px auto;
  min-height: calc(100vh - 180px);
  padding: 0 20px;
}

.footer {
  background: white;
  text-align: center;
  color: #999;
  line-height: 60px;
  height: 60px;
}
</style>

