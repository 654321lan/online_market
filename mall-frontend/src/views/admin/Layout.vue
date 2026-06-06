<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="250px">
        <div class="logo">
          <el-icon :size="28"><Setting /></el-icon>
          <span>管理后台</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#001529"
          text-color="#fff"
          active-text-color="#1890ff"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/merchant">
            <el-icon><Shop /></el-icon>
            <span>商家管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/category">
            <el-icon><Menu /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/product">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/notice">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/banner">
            <el-icon><Picture /></el-icon>
            <span>轮播图管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/member-level">
            <el-icon><Trophy /></el-icon>
            <span>会员等级管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主体内容 -->
      <el-container>
        <el-header class="header">
          <div class="header-content">
            <h2>系统管理平台</h2>
            
            <el-dropdown @command="handleCommand">
              <span class="user-name">
                <svg class="dropdown-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                {{ adminStore.adminInfo?.nickname || adminStore.adminInfo?.username }}
                <svg class="dropdown-arrow" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">
                    <svg class="menu-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                      <polyline points="16 17 21 12 16 7"/>
                      <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      adminStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.el-container {
  min-height: 100vh;
}

.el-aside {
  background: #001529;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 250px;
  overflow-y: auto;
  z-index: 100;
}

.el-container .el-header,
.el-container .el-main {
  margin-left: 250px;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: white;
  font-size: 20px;
  font-weight: bold;
  background: #002140;
}

.el-menu {
  border-right: none;
}

.header {
  background: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-content h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 15px;
  color: #333;
  padding: 6px 14px;
  border-radius: 8px;
  transition: all 0.25s;
  font-weight: 500;
}

.user-name:hover {
  background: #f0f2f5;
  color: #1890ff;
}

.dropdown-icon {
  color: #1890ff;
}

.dropdown-arrow {
  color: #999;
  margin-left: 2px;
}

.menu-icon {
  margin-right: 8px;
  vertical-align: -3px;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  font-size: 14px;
  padding: 8px 20px;
}

.main-content {
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
  padding: 24px;
}
</style>

