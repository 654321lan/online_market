# 商城系统API接口文档

## 基础信息

- 基础URL：`http://localhost:8080`
- API文档地址：`http://localhost:8080/doc.html`
- 返回格式：JSON

## 统一返回格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 一、用户端接口

### 1.1 用户认证

#### 用户注册
- **接口**：`POST /api/user/register`
- **参数**：
  - `username`：用户名
  - `password`：密码

#### 用户登录
- **接口**：`POST /api/user/login`
- **参数**：
  - `username`：用户名
  - `password`：密码
- **返回**：用户信息（不含密码）

#### 获取用户信息
- **接口**：`GET /api/user/info/{id}`

#### 更新用户信息
- **接口**：`PUT /api/user/update`
- **参数**：User对象（JSON）

#### 修改密码
- **接口**：`PUT /api/user/password`
- **参数**：
  - `id`：用户ID
  - `oldPassword`：原密码
  - `newPassword`：新密码

### 1.2 商品浏览

#### 商品列表
- **接口**：`GET /api/product/list`
- **参数**：
  - `page`：页码（默认1）
  - `size`：每页数量（默认10）
  - `categoryId`：分类ID（可选）
  - `keyword`：搜索关键词（可选）
  - `sort`：排序方式（price-价格，sales-销量）

#### 商品详情
- **接口**：`GET /api/product/{id}`

#### 分类列表
- **接口**：`GET /api/category/list`
- **参数**：`parentId`：父级分类ID（可选）

#### 轮播图列表
- **接口**：`GET /api/banner/list`

### 1.3 购物车管理

#### 添加购物车
- **接口**：`POST /api/cart/add`
- **参数**：
  - `userId`：用户ID
  - `productId`：商品ID
  - `quantity`：数量

#### 购物车列表
- **接口**：`GET /api/cart/list`
- **参数**：`userId`：用户ID

#### 更新购物车
- **接口**：`PUT /api/cart/update`
- **参数**：
  - `id`：购物车ID
  - `quantity`：数量

#### 删除购物车
- **接口**：`DELETE /api/cart/delete/{id}`

### 1.4 订单管理

#### 创建订单
- **接口**：`POST /api/order/create`
- **参数**：
  - Order对象（JSON）
  - `cartIds`：购物车ID列表（逗号分隔）

#### 订单列表
- **接口**：`GET /api/order/list`
- **参数**：
  - `userId`：用户ID
  - `page`：页码
  - `size`：每页数量
  - `status`：订单状态（可选）

#### 订单详情
- **接口**：`GET /api/order/{id}`

#### 订单明细
- **接口**：`GET /api/order/items/{orderId}`

#### 支付订单
- **接口**：`POST /api/order/pay/{id}`

#### 取消订单
- **接口**：`PUT /api/order/cancel/{id}`

#### 确认收货
- **接口**：`PUT /api/order/receive/{id}`

### 1.5 地址管理

#### 添加地址
- **接口**：`POST /api/address/add`
- **参数**：Address对象（JSON）

#### 地址列表
- **接口**：`GET /api/address/list`
- **参数**：`userId`：用户ID

#### 更新地址
- **接口**：`PUT /api/address/update`
- **参数**：Address对象（JSON）

#### 删除地址
- **接口**：`DELETE /api/address/delete/{id}`

#### 设置默认地址
- **接口**：`PUT /api/address/default/{id}`
- **参数**：`userId`：用户ID

### 1.6 评价管理

#### 发表评价
- **接口**：`POST /api/review/add`
- **参数**：Review对象（JSON）

#### 我的评价
- **接口**：`GET /api/review/list`
- **参数**：`userId`：用户ID

#### 商品评价列表
- **接口**：`GET /api/review/product/{productId}`

### 1.7 公告查看

#### 公告列表
- **接口**：`GET /api/notice/list`
- **参数**：
  - `page`：页码
  - `size`：每页数量

#### 公告详情
- **接口**：`GET /api/notice/{id}`

---

## 二、商家端接口

### 2.1 商家认证

#### 商家登录
- **接口**：`POST /api/merchant/login`
- **参数**：
  - `username`：用户名
  - `password`：密码

#### 商家信息
- **接口**：`GET /api/merchant/info/{id}`

#### 更新商家信息
- **接口**：`PUT /api/merchant/update`
- **参数**：Merchant对象（JSON）

### 2.2 商品管理

#### 发布商品
- **接口**：`POST /api/merchant/product/add`
- **参数**：Product对象（JSON）

#### 商品列表
- **接口**：`GET /api/merchant/product/list`
- **参数**：
  - `merchantId`：商家ID
  - `page`：页码
  - `size`：每页数量
  - `status`：状态（可选）

#### 商品详情
- **接口**：`GET /api/merchant/product/{id}`

#### 更新商品
- **接口**：`PUT /api/merchant/product/update`
- **参数**：Product对象（JSON）

#### 商品上下架
- **接口**：`PUT /api/merchant/product/status/{id}`
- **参数**：`status`：状态（1-上架，0-下架）

#### 删除商品
- **接口**：`DELETE /api/merchant/product/delete/{id}`

### 2.3 订单管理

#### 订单列表
- **接口**：`GET /api/merchant/order/list`
- **参数**：
  - `merchantId`：商家ID
  - `page`：页码
  - `size`：每页数量
  - `status`：状态（可选）

#### 订单详情
- **接口**：`GET /api/merchant/order/{id}`

#### 订单明细
- **接口**：`GET /api/merchant/order/items/{orderId}`

#### 订单发货
- **接口**：`PUT /api/merchant/order/ship/{id}`

### 2.4 数据统计

#### 销售统计
- **接口**：`GET /api/merchant/statistics/sales`
- **参数**：`merchantId`：商家ID

#### 商品统计
- **接口**：`GET /api/merchant/statistics/product`
- **参数**：`merchantId`：商家ID

---

## 三、管理员端接口

### 3.1 管理员认证

#### 管理员登录
- **接口**：`POST /api/admin/login`
- **参数**：
  - `username`：用户名
  - `password`：密码

#### 修改密码
- **接口**：`PUT /api/admin/password`
- **参数**：
  - `id`：管理员ID
  - `oldPassword`：原密码
  - `newPassword`：新密码

### 3.2 用户管理

#### 用户列表
- **接口**：`GET /api/admin/user/list`
- **参数**：
  - `page`：页码
  - `size`：每页数量
  - `keyword`：搜索关键词（可选）

#### 用户详情
- **接口**：`GET /api/admin/user/{id}`

#### 启用/禁用用户
- **接口**：`PUT /api/admin/user/status/{id}`
- **参数**：`status`：状态（1-启用，0-禁用）

### 3.3 商家管理

#### 商家列表
- **接口**：`GET /api/admin/merchant/list`
- **参数**：
  - `page`：页码
  - `size`：每页数量
  - `keyword`：搜索关键词（可选）

#### 添加商家
- **接口**：`POST /api/admin/merchant/add`
- **参数**：Merchant对象（JSON）

#### 更新商家
- **接口**：`PUT /api/admin/merchant/update`
- **参数**：Merchant对象（JSON）

#### 启用/禁用商家
- **接口**：`PUT /api/admin/merchant/status/{id}`
- **参数**：`status`：状态（1-启用，0-禁用）

#### 删除商家
- **接口**：`DELETE /api/admin/merchant/delete/{id}`

### 3.4 分类管理

#### 分类列表
- **接口**：`GET /api/admin/category/list`

#### 添加分类
- **接口**：`POST /api/admin/category/add`
- **参数**：Category对象（JSON）

#### 更新分类
- **接口**：`PUT /api/admin/category/update`
- **参数**：Category对象（JSON）

#### 删除分类
- **接口**：`DELETE /api/admin/category/delete/{id}`

### 3.5 商品管理

#### 商品列表
- **接口**：`GET /api/admin/product/list`
- **参数**：
  - `page`：页码
  - `size`：每页数量
  - `keyword`：搜索关键词（可选）

#### 商品详情
- **接口**：`GET /api/admin/product/{id}`

#### 商品上下架
- **接口**：`PUT /api/admin/product/status/{id}`
- **参数**：`status`：状态（1-上架，0-下架）

### 3.6 公告管理

#### 公告列表
- **接口**：`GET /api/admin/notice/list`
- **参数**：
  - `page`：页码
  - `size`：每页数量

#### 添加公告
- **接口**：`POST /api/admin/notice/add`
- **参数**：Notice对象（JSON）

#### 更新公告
- **接口**：`PUT /api/admin/notice/update`
- **参数**：Notice对象（JSON）

#### 删除公告
- **接口**：`DELETE /api/admin/notice/delete/{id}`

#### 公告上下架
- **接口**：`PUT /api/admin/notice/status/{id}`
- **参数**：`status`：状态（1-发布，0-下架）

### 3.7 轮播图管理

#### 轮播图列表
- **接口**：`GET /api/admin/banner/list`

#### 添加轮播图
- **接口**：`POST /api/admin/banner/add`
- **参数**：Banner对象（JSON）

#### 更新轮播图
- **接口**：`PUT /api/admin/banner/update`
- **参数**：Banner对象（JSON）

#### 删除轮播图
- **接口**：`DELETE /api/admin/banner/delete/{id}`

### 3.8 数据统计

#### 平台数据概览
- **接口**：`GET /api/admin/statistics/overview`
- **返回数据**：
  - `userCount`：用户总数
  - `merchantCount`：商家总数
  - `productCount`：商品总数
  - `orderCount`：订单总数
  - `totalAmount`：交易总额

---

## 四、文件上传接口

### 上传文件
- **接口**：`POST /api/upload`
- **参数**：`file`（multipart/form-data）
- **返回**：文件访问路径

---

## 订单状态说明

- `0`：待支付
- `1`：待发货
- `2`：待收货
- `3`：已完成
- `4`：已取消

## 公告类型说明

- `1`：平台公告
- `2`：系统通知
- `3`：活动公告

## 注意事项

1. 所有接口返回格式统一为：`{code, message, data}`
2. 密码未加密，实际项目中应使用加密算法
3. 未实现JWT token认证，实际项目需要添加
4. 文件上传保存在本地 `./uploads/` 目录
5. 分页查询返回 MyBatis-Plus 的 IPage 对象

