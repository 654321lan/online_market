<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <el-card class="banner-card" :body-style="{ padding: '0' }">
      <el-carousel height="400px" v-if="bannerList.length > 0">
        <el-carousel-item v-for="banner in bannerList" :key="banner.id">
          <img :src="banner.image" :alt="banner.title" class="banner-image" />
        </el-carousel-item>
      </el-carousel>
      <div v-else class="no-banner">
        <el-empty description="暂无轮播图" />
      </div>
    </el-card>
    
    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品"
          size="large"
          style="max-width: 500px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
        
        <div class="filter-bar">
          <el-select v-model="selectedCategory" placeholder="选择分类" clearable @change="handleSearch" style="width: 250px">
            <el-option label="全部分类" :value="null" />
            <el-option
              v-for="category in categoryList"
              :key="category.id"
              :label="category.label || category.name"
              :value="category.id"
            />
          </el-select>
          
          <el-select v-model="sortType" placeholder="排序方式" @change="handleSearch" style="width: 120px">
            <el-option label="默认" value="" />
            <el-option label="价格" value="price" />
            <el-option label="销量" value="sales" />
          </el-select>
        </div>
      </div>
    </el-card>
    
    <!-- 商品列表 -->
    <div class="product-list">
      <el-row :gutter="20">
        <el-col :span="6" v-for="product in productList" :key="product.id">
          <el-card class="product-card" shadow="hover" @click="goToDetail(product.id)">
            <img :src="product.image" class="product-image" />
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-price">¥{{ product.price }}</p>
              <div class="product-meta">
                <span>销量: {{ product.sales }}</span>
                <span>库存: {{ product.stock }}</span>
              </div>
              <el-button type="primary" size="small" @click.stop="addToCart(product)">加入购物车</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-empty v-if="productList.length === 0" description="暂无商品" />
      
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getProductList, getCategoryList, getBannerList } from '@/api/product'
import { addCart } from '@/api/cart'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const bannerList = ref([])
const categoryList = ref([])
const productList = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const searchKeyword = ref('')
const selectedCategory = ref(null)
const sortType = ref('')

const loadBanners = async () => {
  try {
    const res = await getBannerList()
    bannerList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const loadCategories = async () => {
  try {
    // 先加载一级分类
    const res1 = await getCategoryList()
    const firstLevel = res1.data || []
    
    // 加载所有二级分类
    const allCategories = []
    for (let parent of firstLevel) {
      allCategories.push({ ...parent, label: parent.name })
      
      // 加载该一级分类下的二级分类
      const res2 = await getCategoryList(parent.id)
      const children = res2.data || []
      children.forEach(child => {
        allCategories.push({
          ...child,
          label: `　${parent.name} - ${child.name}` // 前面加空格缩进
        })
      })
    }
    
    categoryList.value = allCategories
    return Promise.resolve()
  } catch (error) {
    console.error(error)
    return Promise.reject(error)
  }
}

const loadProducts = async () => {
  try {
    const res = await getProductList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      categoryId: selectedCategory.value || undefined,
      sort: sortType.value || undefined
    })
    productList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadProducts()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToDetail = (id) => {
  router.push(`/user/product/${id}`)
}

const addToCart = async (product) => {
  try {
    await addCart(userStore.userInfo.id, product.id, 1)
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadBanners()
  loadCategories().then(() => {
    // 检查URL参数中是否有分类ID
    if (route.query.categoryId) {
      selectedCategory.value = Number(route.query.categoryId)
      handleSearch()
    } else {
      loadProducts()
    }
  })
})
</script>

<style scoped>
.home-page {
  padding: 0;
}

.banner-card {
  margin-bottom: 20px;
}

.banner-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

.no-banner {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.search-card {
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 20px;
}

.filter-bar {
  display: flex;
  gap: 10px;
}

.product-list {
  margin-top: 20px;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  padding: 15px 0;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 10px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 14px;
  margin-bottom: 15px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>

