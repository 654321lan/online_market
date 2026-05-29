<template>
  <div class="product-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>商品管理</h2>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增商品
          </el-button>
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品名称"
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
        
        <el-select v-model="filterStatus" placeholder="商品状态" clearable @change="handleSearch" style="width: 150px">
          <el-option label="全部" :value="null" />
          <el-option label="已上架" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
      </div>
      
      <!-- 商品列表 -->
      <el-table :data="productList" style="width: 100%; margin-top: 20px">
        <el-table-column label="商品图片" width="120">
          <template #default="{ row }">
            <img :src="row.image" class="product-image" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="250" />
        <el-table-column label="价格" width="130">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="120" />
        <el-table-column prop="sales" label="销量" width="120" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
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
    
    <!-- 商品表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editMode ? '编辑商品' : '新增商品'"
      width="700px"
      top="8vh"
    >
      <el-form :model="productForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称">
            <template #prefix>
              <el-icon><Goods /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in categoryList"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="productForm.price" :min="0.01" :precision="2" :step="1" />
        </el-form-item>
        
        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" :step="1" />
        </el-form-item>
        
        <el-form-item label="商品图片" prop="image">
          <el-upload
            class="image-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :on-error="handleImageError"
            :before-upload="beforeImageUpload"
            name="file"
          >
            <img v-if="productForm.image" :src="productForm.image" class="uploaded-image" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="5"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        
        <el-form-item label="商品状态">
          <el-radio-group v-model="productForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
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
import { Plus, Search, Goods } from '@element-plus/icons-vue'
import {
  getMerchantProducts,
  addProduct,
  updateProduct,
  updateProductStatus,
  deleteProduct
} from '@/api/merchant'
import { getCategoryList } from '@/api/product'
import { useMerchantStore } from '@/stores/merchant'

const merchantStore = useMerchantStore()
const formRef = ref()
const dialogVisible = ref(false)
const editMode = ref(false)

const uploadUrl = '/api/upload'

const productList = ref([])
const categoryList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref(null)
const searchKeyword = ref('')

const productForm = reactive({
  id: null,
  merchantId: null,
  categoryId: null,
  name: '',
  image: '',
  price: 0,
  stock: 0,
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }],
  image: [{ required: true, message: '请上传商品图片', trigger: 'change' }]
}

const loadProducts = async () => {
  try {
    const res = await getMerchantProducts({
      merchantId: merchantStore.merchantInfo.id,
      page: currentPage.value,
      size: pageSize.value,
      status: filterStatus.value,
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

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleAdd = () => {
  editMode.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (product) => {
  editMode.value = true
  Object.assign(productForm, product)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      productForm.merchantId = merchantStore.merchantInfo.id
      
      if (editMode.value) {
        await updateProduct(productForm)
        ElMessage.success('修改成功')
      } else {
        await addProduct(productForm)
        ElMessage.success('添加成功')
      }
      
      dialogVisible.value = false
      loadProducts()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleStatusChange = async (product) => {
  const newStatus = product.status === 1 ? 0 : 1
  const text = newStatus === 1 ? '上架' : '下架'
  
  try {
    await ElMessageBox.confirm(`确定要${text}该商品吗？`, '提示', {
      type: 'warning'
    })
    
    await updateProductStatus(product.id, newStatus)
    ElMessage.success(`${text}成功`)
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      type: 'warning'
    })
    
    await deleteProduct(id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleImageSuccess = (response) => {
  console.log('上传成功，返回数据：', response)
  if (response && response.code === 200) {
    // 后端返回的是 /uploads/xxx.jpg 格式
    let imageUrl = response.data
    
    productForm.image = imageUrl
    console.log('设置商品图片URL：', imageUrl)
    ElMessage.success('图片上传成功')
  } else {
    console.error('上传失败：', response)
    ElMessage.error(response?.message || '上传失败')
  }
}

const handleImageError = (error) => {
  console.error('上传失败', error)
  ElMessage.error('商品图片上传失败，请重试')
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const resetForm = () => {
  productForm.id = null
  productForm.categoryId = null
  productForm.name = ''
  productForm.image = ''
  productForm.price = 0
  productForm.stock = 0
  productForm.description = ''
  productForm.status = 1
}

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<style scoped>
.product-page {
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

.filter-bar {
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

.image-uploader {
  display: inline-block;
}

.image-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.uploaded-image {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
</style>

