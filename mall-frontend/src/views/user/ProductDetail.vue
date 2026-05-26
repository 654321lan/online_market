<template>
  <div class="product-detail-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator=">" class="breadcrumb" v-if="product">
      <el-breadcrumb-item :to="{ path: '/user/home' }">
        <el-icon><HomeFilled /></el-icon>
        首页
      </el-breadcrumb-item>
      <el-breadcrumb-item v-if="categoryName" @click="goToCategory">
        <span class="category-link">{{ categoryName }}</span>
      </el-breadcrumb-item>
      <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
    </el-breadcrumb>
    
    <el-card v-if="product">
      <el-row :gutter="30">
        <el-col :span="10">
          <img :src="product.image" class="product-main-image" />
        </el-col>
        
        <el-col :span="14">
          <div class="product-detail">
            <h1 class="product-title">{{ product.name }}</h1>
            
            <div class="product-price-box">
              <span class="price-label">价格：</span>
              <span class="price-value">¥{{ product.price }}</span>
            </div>
            
            <div class="product-meta-info">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="meta-item">
                    <span class="meta-label">销量：</span>
                    <span class="meta-value">{{ product.sales }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="meta-item">
                    <span class="meta-label">库存：</span>
                    <span class="meta-value">{{ product.stock }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>
            
            <div class="shop-info-box" v-if="merchantInfo">
              <div class="shop-header">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#1890ff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                  <polyline points="9 22 9 12 15 12 15 22"/>
                </svg>
                <span class="shop-name">{{ merchantInfo.shopName }}</span>
              </div>
              <div class="shop-detail-rows">
                <div class="shop-detail-row" v-if="merchantInfo.shopDesc">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8c8c8c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="17" y1="10" x2="3" y2="10"/>
                    <line x1="21" y1="6" x2="3" y2="6"/>
                    <line x1="21" y1="14" x2="3" y2="14"/>
                    <line x1="17" y1="18" x2="3" y2="18"/>
                  </svg>
                  <span>{{ merchantInfo.shopDesc }}</span>
                </div>
                <div class="shop-detail-row" v-if="merchantInfo.contact">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#8c8c8c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.79 19.79 0 0 1 2.12 4.11 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
                  </svg>
                  <span>联系商家：{{ merchantInfo.contact }}</span>
                </div>
              </div>
            </div>
            
            <div class="quantity-box">
              <span class="quantity-label">数量：</span>
              <el-input-number v-model="quantity" :min="1" :max="product.stock" />
            </div>
            
            <div class="action-buttons">
              <el-button type="primary" size="large" @click="handleAddCart" :disabled="product.stock === 0">
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </el-button>
              <el-button type="success" size="large" @click="handleBuyNow" :disabled="product.stock === 0">
                立即购买
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 商品详情描述 -->
      <el-divider />
      <div class="product-description">
        <h2>商品详情</h2>
        <div class="description-content">
          {{ product.description || '暂无详细描述' }}
        </div>
      </div>
      
      <!-- 商品评价 -->
      <el-divider />
      <div class="product-reviews">
        <h2>商品评价</h2>
        <div v-if="reviews.length > 0" class="review-list">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-user-info">
              <img :src="review.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="user-avatar" />
              <div class="user-detail">
                <span class="user-nickname">{{ review.nickname || review.username || '匿名用户' }}</span>
                <div class="review-meta">
                  <el-rate v-model="review.rating" disabled size="small" />
                  <span class="review-time">{{ formatTime(review.createTime) }}</span>
                </div>
              </div>
            </div>
            <div class="review-content">{{ review.content }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无评价" />
      </div>
    </el-card>
    
    <el-empty v-else description="商品不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail, getCategoryList } from '@/api/product'
import { addCart } from '@/api/cart'
import { getProductReviews } from '@/api/review'
import { getMerchantInfo } from '@/api/merchant'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const quantity = ref(1)
const reviews = ref([])
const categoryName = ref('')
const currentCategoryId = ref(null)
const merchantInfo = ref(null)

const loadProduct = async () => {
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
    
    // 加载商家信息
    if (res.data.merchantId) {
      loadMerchantInfo(res.data.merchantId)
    }
    
    // 加载分类信息
    if (res.data.categoryId) {
      loadCategoryName(res.data.categoryId)
    }
  } catch (error) {
    console.error(error)
  }
}

const loadCategoryName = async (categoryId) => {
  try {
    currentCategoryId.value = categoryId
    
    // 先尝试从一级分类查找
    const res1 = await getCategoryList()
    const firstLevel = res1.data || []
    
    // 遍历一级分类，查找对应的二级分类
    for (let parent of firstLevel) {
      const res2 = await getCategoryList(parent.id)
      const children = res2.data || []
      const found = children.find(c => c.id === categoryId)
      if (found) {
        categoryName.value = `${parent.name} / ${found.name}`
        return
      }
    }
    
    // 如果是一级分类
    const parent = firstLevel.find(c => c.id === categoryId)
    if (parent) {
      categoryName.value = parent.name
    }
  } catch (error) {
    console.error(error)
  }
}

const loadMerchantInfo = async (merchantId) => {
  try {
    const res = await getMerchantInfo(merchantId)
    merchantInfo.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const goToCategory = () => {
  // 跳转到首页并传递分类ID参数
  router.push({
    path: '/user/home',
    query: { categoryId: currentCategoryId.value }
  })
}

const loadReviews = async () => {
  try {
    const res = await getProductReviews(route.params.id)
    reviews.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleAddCart = async () => {
  try {
    await addCart(userStore.userInfo.id, product.value.id, quantity.value)
    ElMessage.success('已加入购物车')
    userStore.refreshCartCount()
  } catch (error) {
    console.error(error)
  }
}

const handleBuyNow = async () => {
  await handleAddCart()
  router.push('/user/cart')
}

const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : ''
}

onMounted(() => {
  loadProduct()
  loadReviews()
})
</script>

<style scoped>
.product-detail-page {
  padding: 0;
}

.breadcrumb {
  padding: 15px 20px;
  background: white;
  border-radius: 4px;
  margin-bottom: 20px;
}

.breadcrumb :deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
  font-weight: normal;
}

.breadcrumb :deep(.el-breadcrumb__inner:hover) {
  color: #409eff;
}

.category-link {
  cursor: pointer;
  color: #606266;
}

.category-link:hover {
  color: #409eff;
}

.product-main-image {
  width: 100%;
  border-radius: 8px;
}

.product-detail {
  padding: 20px 0;
}

.product-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 30px;
}

.product-price-box {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.price-label {
  font-size: 16px;
  color: #666;
}

.price-value {
  font-size: 36px;
  color: #f56c6c;
  font-weight: bold;
  margin-left: 10px;
}

.product-meta-info {
  margin-bottom: 20px;
}

.shop-info-box {
  padding: 16px 18px;
  background: #f0f9ff;
  border-radius: 10px;
  margin-bottom: 30px;
  border: 1px solid #d9ecff;
}

.shop-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.shop-name {
  font-size: 16px;
  color: #1890ff;
  font-weight: 600;
}

.shop-detail-rows {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 26px;
}

.shop-detail-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.shop-detail-row svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.meta-item {
  padding: 10px;
  background: #fafafa;
  border-radius: 4px;
}

.meta-label {
  color: #666;
  margin-right: 10px;
}

.meta-value {
  color: #333;
  font-weight: 500;
}

.quantity-box {
  margin-bottom: 30px;
  display: flex;
  align-items: center;
}

.quantity-label {
  font-size: 16px;
  margin-right: 20px;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.product-description,
.product-reviews {
  margin-top: 30px;
}

.product-description h2,
.product-reviews h2 {
  font-size: 20px;
  margin-bottom: 20px;
}

.description-content {
  line-height: 1.8;
  color: #666;
  white-space: pre-wrap;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.review-user-info {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  margin-bottom: 15px;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.user-detail {
  flex: 1;
}

.user-nickname {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 8px;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.review-time {
  color: #999;
  font-size: 14px;
}

.review-content {
  color: #666;
  line-height: 1.8;
  padding-left: 65px;
  font-size: 15px;
}
</style>

