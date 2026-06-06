<template>
  <div class="category-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>分类管理</h2>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增分类
          </el-button>
        </div>
      </template>
      
      <!-- 分类列表 -->
      <el-table :data="displayCategoryList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="120" />
        <el-table-column label="分类名称" min-width="300">
          <template #default="{ row }">
            <div :style="{ paddingLeft: row.level * 30 + 'px' }">
              <span v-if="row.level > 0" style="color: #909399; margin-right: 8px;">└─</span>
              <span :style="{ fontWeight: row.level === 0 ? 'bold' : 'normal' }">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="150" />
        <el-table-column label="状态" width="150">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="220">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
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
    
    <!-- 分类表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editMode ? '编辑分类' : '新增分类'"
      width="500px"
    >
      <el-form :model="categoryForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="父级分类">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父级分类" clearable>
            <el-option label="无（一级分类）" :value="0" />
            <el-option
              v-for="cat in parentCategoryList"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :label="1">启用</el-radio>
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
import { Plus } from '@element-plus/icons-vue'
import {
  getAdminCategoryList,
  addCategory,
  updateCategory,
  deleteCategory
} from '@/api/admin'

const formRef = ref()
const dialogVisible = ref(false)
const editMode = ref(false)

const categoryList = ref([])
const displayCategoryList = ref([])
const parentCategoryList = ref([])
const currentPage = ref(1)
const pageSize = ref(15)
const total = ref(0)

const categoryForm = reactive({
  id: null,
  name: '',
  parentId: 0,
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const loadCategories = async () => {
  try {
    const res = await getAdminCategoryList()
    const allCategories = res.data || []
    
    // 构建树形结构
    const treeData = buildTreeData(allCategories)
    categoryList.value = treeData
    
    // 获取父级分类列表（只显示一级分类）
    parentCategoryList.value = allCategories.filter(cat => cat.parentId === 0)
    
    // 分页处理
    total.value = treeData.length
    updateDisplayList()
  } catch (error) {
    console.error(error)
  }
}

// 构建树形结构数据
const buildTreeData = (categories) => {
  const result = []
  
  // 先找出所有一级分类
  const firstLevel = categories.filter(cat => cat.parentId === 0)
    .sort((a, b) => a.sort - b.sort)
  
  // 为每个一级分类添加其子分类
  firstLevel.forEach(parent => {
    result.push({ ...parent, level: 0 })
    
    // 找出该一级分类的所有子分类
    const children = categories.filter(cat => cat.parentId === parent.id)
      .sort((a, b) => a.sort - b.sort)
    
    children.forEach(child => {
      result.push({ ...child, level: 1 })
    })
  })
  
  return result
}

// 更新显示列表（分页）
const updateDisplayList = () => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  displayCategoryList.value = categoryList.value.slice(start, end)
}

const handlePageChange = (page) => {
  currentPage.value = page
  updateDisplayList()
}

const handleAdd = () => {
  editMode.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (category) => {
  editMode.value = true
  Object.assign(categoryForm, category)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (editMode.value) {
        await updateCategory(categoryForm)
        ElMessage.success('修改成功')
      } else {
        await addCategory(categoryForm)
        ElMessage.success('添加成功')
        currentPage.value = 1  // 添加成功后回到第一页
      }
      
      dialogVisible.value = false
      await loadCategories()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      type: 'warning'
    })
    
    await deleteCategory(id)
    ElMessage.success('删除成功')
    
    // 如果当前页删除后没有数据，回到上一页
    if (displayCategoryList.value.length === 1 && currentPage.value > 1) {
      currentPage.value--
    }
    
    await loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const resetForm = () => {
  categoryForm.id = null
  categoryForm.name = ''
  categoryForm.parentId = 0
  categoryForm.sort = 0
  categoryForm.status = 1
}

const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : ''
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-page {
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

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

