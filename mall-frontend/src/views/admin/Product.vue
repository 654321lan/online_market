<template>
  <div class="product-page">
    <el-card>
      <template #header>
        <h2>商品管理</h2>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品名称"
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>
      
      <!-- 商品列表 -->
      <el-table :data="productList" style="width: 100%; margin-top: 20px">
        <el-table-column label="商品图片" width="120">
          <template #default="{ row }">
            <img :src="row.image" class="product-image" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="300" />
        <el-table-column label="价格" width="150">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="120" />
        <el-table-column prop="sales" label="销量" width="120" />
        <el-table-column label="状态" width="150">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAdminProductList, updateAdminProductStatus } from '@/api/admin'

const productList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

const loadProducts = async () => {
  try {
    const res = await getAdminProductList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined
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
}

const handleStatusChange = async (product) => {
  const newStatus = product.status === 1 ? 0 : 1
  const text = newStatus === 1 ? '上架' : '下架'
  
  try {
    await ElMessageBox.confirm(`确定要${text}该商品吗？`, '提示', {
      type: 'warning'
    })
    
    await updateAdminProductStatus(product.id, newStatus)
    ElMessage.success(`${text}成功`)
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-page {
  padding: 0;
}

.search-bar {
  display: flex;
  gap: 10px;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

