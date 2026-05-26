<template>
  <div class="cart-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>购物车</h2>
        </div>
      </template>
      
      <div v-if="cartList.length > 0">
        <div v-for="(group, merchantId) in groupedCartList" :key="merchantId" class="merchant-group">
          <div class="merchant-group-header">
            <el-icon><Shop /></el-icon>
            <span class="shop-name">{{ group.shopName }}</span>
          </div>
          <el-table
            :data="group.items"
            style="width: 100%"
            :ref="el => setTableRef(merchantId, el)"
            @selection-change="(selection) => handleSelectionChange(merchantId, selection)"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column label="商品信息" width="500">
              <template #default="{ row }">
                <div class="product-info">
                  <img :src="row.product.image" class="product-image" />
                  <span>{{ row.product.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="product.price" label="单价" width="200">
              <template #default="{ row }">
                <span class="price">¥{{ row.product.price }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="300">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="row.product.stock"
                  @change="handleQuantityChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="200">
              <template #default="{ row }">
                <span class="price">¥{{ (row.product.price * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleDelete(row.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <el-empty v-else description="购物车是空的" />
      
      <!-- 结算栏 -->
      <div class="cart-footer" v-if="cartList.length > 0">
        <div class="footer-left">
          <span>已选 {{ selectedRows.length }} 件商品</span>
        </div>
        <div class="footer-right">
          <span class="total-label">合计：</span>
          <span class="total-price">¥{{ totalPrice }}</span>
          <span v-if="discountInfo.discount < 1" style="font-size: 12px; color: #e6a23c; margin-left: 5px">({{ discountInfo.levelName }}价)</span>
          <el-button type="primary" size="large" @click="handleCheckout" :disabled="selectedRows.length === 0">
            <el-icon><ShoppingCart /></el-icon>
            去结算
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 结算对话框 -->
    <el-dialog v-model="checkoutVisible" title="确认订单" width="600px">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="收货地址">
          <el-select v-model="orderForm.addressId" placeholder="选择收货地址" style="width: 100%">
            <el-option
              v-for="addr in addressList"
              :key="addr.id"
              :label="`${addr.receiverName} ${addr.receiverPhone} ${addr.province}${addr.city}${addr.district}${addr.detail}`"
              :value="addr.id"
            />
          </el-select>
          <el-button type="text" @click="$router.push('/user/address')">管理地址</el-button>
        </el-form-item>
        <el-form-item label="订单备注">
          <el-input v-model="orderForm.remark" type="textarea" :rows="3" placeholder="选填，可以告诉商家您的特殊需求" />
        </el-form-item>
        <el-form-item label="订单拆分" v-if="selectedMerchantCount > 1">
          <el-alert
            title="您选购了多个商家的商品，系统将自动按商家拆分为多个订单"
            type="info"
            :closable="false"
            show-icon
          />
        </el-form-item>
        <el-form-item label="会员信息">
          <el-tag :type="discountInfo.memberLevel === 0 ? 'info' : discountInfo.memberLevel === 1 ? '' : discountInfo.memberLevel === 2 ? 'warning' : 'danger'" effect="dark">
            {{ discountInfo.levelName }}
          </el-tag>
          <span v-if="discountInfo.discount < 1" style="margin-left: 10px; color: #e6a23c; font-weight: bold">
            享 {{ (discountInfo.discount * 10).toFixed(1) }} 折优惠
          </span>
        </el-form-item>
        <el-form-item label="商品原价">
          <span :style="discountInfo.discount < 1 ? 'text-decoration: line-through; color: #999' : ''" class="order-amount">¥{{ originalPrice }}</span>
        </el-form-item>
        <el-form-item label="会员优惠" v-if="discountInfo.discount < 1">
          <span style="color: #67c23a; font-weight: bold; font-size: 18px">-¥{{ discountAmount }}</span>
        </el-form-item>
        <el-form-item label="实付金额">
          <span class="order-amount" style="color: #f56c6c">¥{{ totalPrice }}</span>
          <span v-if="selectedMerchantCount > 1" class="order-split-info">
            （将拆分为 {{ selectedMerchantCount }} 个订单）
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCartList, updateCart, deleteCart } from '@/api/cart'
import { getAddressList } from '@/api/address'
import { createOrder } from '@/api/order'
import { getMerchantInfo } from '@/api/merchant'
import { getUserDiscount } from '@/api/member'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const cartList = ref([])
const selectedMap = ref({})
const checkoutVisible = ref(false)
const addressList = ref([])
const submitting = ref(false)
const merchantNames = ref({})
const tableRefs = ref({})
const discountInfo = ref({ memberLevel: 0, levelName: '普通会员', discount: 1 })

const setTableRef = (merchantId, el) => {
  if (el) tableRefs.value[merchantId] = el
}

const orderForm = ref({
  addressId: null,
  remark: ''
})

const selectedRows = computed(() => {
  let all = []
  for (const items of Object.values(selectedMap.value)) {
    all = all.concat(items)
  }
  return all
})

const selectedMerchantCount = computed(() => {
  const merchantIds = new Set(selectedRows.value.map(r => r.product.merchantId))
  return merchantIds.size
})

const originalPrice = computed(() => {
  return selectedRows.value
    .reduce((sum, item) => sum + item.product.price * item.quantity, 0)
    .toFixed(2)
})

const totalPrice = computed(() => {
  const original = parseFloat(originalPrice.value)
  return (original * (discountInfo.value.discount || 1)).toFixed(2)
})

const discountAmount = computed(() => {
  return (parseFloat(originalPrice.value) - parseFloat(totalPrice.value)).toFixed(2)
})

const groupedCartList = computed(() => {
  const groups = {}
  for (const cart of cartList.value) {
    const mid = cart.product.merchantId
    if (!groups[mid]) {
      groups[mid] = {
        shopName: merchantNames.value[mid] || '店铺加载中...',
        items: []
      }
    }
    groups[mid].items.push(cart)
  }
  return groups
})

const loadCartList = async () => {
  try {
    const res = await getCartList(userStore.userInfo.id)
    const carts = res.data || []
    
    // 加载每个购物车项的商品信息
    const merchantIdSet = new Set()
    for (let cart of carts) {
      const productRes = await import('@/api/product').then(m => m.getProductDetail(cart.productId))
      cart.product = productRes.data
      merchantIdSet.add(cart.product.merchantId)
    }
    
    // 加载商家名称
    for (const mid of merchantIdSet) {
      if (!merchantNames.value[mid]) {
        try {
          const mRes = await getMerchantInfo(mid)
          merchantNames.value[mid] = mRes.data?.shopName || '未知店铺'
        } catch (e) {
          merchantNames.value[mid] = '未知店铺'
        }
      }
    }
    
    cartList.value = carts
  } catch (error) {
    console.error(error)
  }
}

const loadAddressList = async () => {
  try {
    const res = await getAddressList(userStore.userInfo.id)
    addressList.value = res.data || []
    
    // 默认选中默认地址
    const defaultAddr = addressList.value.find(a => a.isDefault === 1)
    if (defaultAddr) {
      orderForm.value.addressId = defaultAddr.id
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSelectionChange = (merchantId, selection) => {
  selectedMap.value[merchantId] = selection
}

const handleQuantityChange = async (row) => {
  try {
    await updateCart(row.id, row.quantity)
    ElMessage.success('已更新数量')
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      type: 'warning'
    })
    
    await deleteCart(id)
    ElMessage.success('已删除')
    loadCartList()
    userStore.refreshCartCount()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const loadDiscountInfo = async () => {
  try {
    const res = await getUserDiscount(userStore.userInfo.id)
    discountInfo.value = res.data || { memberLevel: 0, levelName: '普通会员', discount: 1 }
  } catch (e) {
    console.error(e)
  }
}

const handleCheckout = async () => {
  await loadAddressList()
  await loadDiscountInfo()
  checkoutVisible.value = true
}

const submitOrder = async () => {
  if (!orderForm.value.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  const selectedAddr = addressList.value.find(a => a.id === orderForm.value.addressId)
  if (!selectedAddr) {
    ElMessage.warning('收货地址无效')
    return
  }
  
  submitting.value = true
  try {
    const order = {
      userId: userStore.userInfo.id,
      receiverName: selectedAddr.receiverName,
      receiverPhone: selectedAddr.receiverPhone,
      receiverAddress: `${selectedAddr.province}${selectedAddr.city}${selectedAddr.district}${selectedAddr.detail}`,
      remark: orderForm.value.remark
    }
    
    const cartIds = selectedRows.value.map(r => r.id).join(',')
    const res = await createOrder(order, cartIds)
    
    const orderCount = res.data?.length || 1
    ElMessage.success(`订单创建成功，共生成 ${orderCount} 个订单`)
    checkoutVisible.value = false
    userStore.refreshCartCount()
    router.push('/user/order')
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCartList()
  loadDiscountInfo()
})
</script>

<style scoped>
.cart-page {
  padding: 0;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.merchant-group {
  margin-bottom: 25px;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.merchant-group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: #f5f7fa;
  font-size: 15px;
  font-weight: bold;
  color: #333;
  border-bottom: 1px solid #eee;
}

.shop-name {
  color: #409eff;
}

.order-split-info {
  font-size: 14px;
  color: #909399;
  margin-left: 10px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.cart-footer {
  margin-top: 30px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-left {
  font-size: 16px;
  color: #666;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.total-label {
  font-size: 16px;
  color: #666;
}

.total-price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}

.order-amount {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}
</style>

