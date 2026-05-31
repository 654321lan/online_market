[README.md](https://github.com/user-attachments/files/28436594/README.md)
# 通用商城系统 - 前端

基于 Vue 3 + Element Plus 开发的通用商城系统前端项目，包含用户端、商家端和管理员端三个角色的完整功能。

## 技术栈

- Vue 3.3
- Vite 4.4
- Element Plus 2.3
- Vue Router 4.2
- Pinia 2.1
- Axios 1.5

## 项目结构

```
mall-frontend/
├── src/
│   ├── api/              # API接口
│   │   ├── user.js       # 用户接口
│   │   ├── product.js    # 商品接口
│   │   ├── cart.js       # 购物车接口
│   │   ├── order.js      # 订单接口
│   │   ├── address.js    # 地址接口
│   │   ├── review.js     # 评价接口
│   │   ├── notice.js     # 公告接口
│   │   ├── merchant.js   # 商家接口
│   │   ├── admin.js      # 管理员接口
│   │   └── upload.js     # 文件上传接口
│   ├── assets/           # 静态资源
│   ├── stores/           # 状态管理
│   │   ├── user.js       # 用户状态
│   │   ├── merchant.js   # 商家状态
│   │   └── admin.js      # 管理员状态
│   ├── utils/            # 工具类
│   │   └── request.js    # Axios封装
│   ├── views/            # 页面
│   │   ├── Login.vue     # 统一登录页
│   │   ├── user/         # 用户端页面
│   │   │   ├── Layout.vue
│   │   │   ├── Home.vue
│   │   │   ├── ProductDetail.vue
│   │   │   ├── Cart.vue
│   │   │   ├── Order.vue
│   │   │   ├── Profile.vue
│   │   │   ├── Address.vue
│   │   │   └── Notice.vue
│   │   ├── merchant/     # 商家端页面
│   │   │   ├── Layout.vue
│   │   │   ├── Dashboard.vue
│   │   │   ├── Product.vue
│   │   │   ├── Order.vue
│   │   │   └── Profile.vue
│   │   └── admin/        # 管理员端页面
│   │       ├── Layout.vue
│   │       ├── Dashboard.vue
│   │       ├── User.vue
│   │       ├── Merchant.vue
│   │       ├── Category.vue
│   │       ├── Product.vue
│   │       ├── Notice.vue
│   │       └── Banner.vue
│   ├── router/           # 路由配置
│   ├── App.vue
│   └── main.js
├── index.html
├── vite.config.js
└── package.json
```

## 功能模块

### 用户端
- 用户注册登录
- 商品浏览、搜索、筛选
- 商品详情查看
- 购物车管理
- 订单管理（创建、支付、取消、确认收货）
- 收货地址管理
- 商品评价
- 个人信息修改
- 公告查看

### 商家端
- 商家登录
- 数据概览（销售统计）
- 商品管理（发布、编辑、上下架、删除）
- 订单管理（查看、发货）
- 店铺信息管理

### 管理员端
- 管理员登录
- 数据概览（平台统计）
- 用户管理（查看、启用/禁用）
- 商家管理（增删改查、启用/禁用）
- 分类管理（增删改查）
- 商品管理（查看、上下架）
- 公告管理（增删改查、发布/下架）
- 轮播图管理（增删改查）

## 安装依赖

```bash
npm install
```

## 启动开发服务器

```bash
npm run dev
```

访问地址：http://localhost:5173

## 构建生产版本

```bash
npm run build
```

## 登录入口

所有角色共用一个登录页面，通过选项卡切换不同角色：
- 用户登录 → 跳转到用户端 (/user/home)
- 商家登录 → 跳转到商家端 (/merchant/dashboard)
- 管理员登录 → 跳转到管理员端 (/admin/dashboard)

## 注意事项

1. 后端服务地址配置在 `vite.config.js` 中，默认代理到 `http://localhost:8080`
2. 图片上传依赖后端文件上传接口 `/api/upload`
3. 请确保后端服务已启动并正常运行
4. 建议使用 Chrome、Edge、Firefox 等现代浏览器访问

## 测试账号

请参考后端数据库初始化脚本中的测试账号。

