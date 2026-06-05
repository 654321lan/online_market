import request from '@/utils/request'

// 管理员登录
export const adminLogin = (username, password) => {
  return request({
    url: '/admin/login',
    method: 'post',
    params: { username, password }
  })
}

// 修改密码
export const adminUpdatePassword = (id, oldPassword, newPassword) => {
  return request({
    url: '/admin/password',
    method: 'put',
    params: { id, oldPassword, newPassword }
  })
}

// 用户管理
export const getUserList = (params) => {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

export const getUser = (id) => {
  return request({
    url: `/admin/user/${id}`,
    method: 'get'
  })
}

export const updateUserStatus = (id, status) => {
  return request({
    url: `/admin/user/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 商家管理
export const getMerchantList = (params) => {
  return request({
    url: '/admin/merchant/list',
    method: 'get',
    params
  })
}

export const addMerchant = (merchant) => {
  return request({
    url: '/admin/merchant/add',
    method: 'post',
    data: merchant
  })
}

export const updateMerchantInfo = (merchant) => {
  return request({
    url: '/admin/merchant/update',
    method: 'put',
    data: merchant
  })
}

export const updateMerchantStatus = (id, status) => {
  return request({
    url: `/admin/merchant/status/${id}`,
    method: 'put',
    params: { status }
  })
}

export const deleteMerchant = (id) => {
  return request({
    url: `/admin/merchant/delete/${id}`,
    method: 'delete'
  })
}

// 分类管理
export const getAdminCategoryList = () => {
  return request({
    url: '/admin/category/list',
    method: 'get'
  })
}

export const addCategory = (category) => {
  return request({
    url: '/admin/category/add',
    method: 'post',
    data: category
  })
}

export const updateCategory = (category) => {
  return request({
    url: '/admin/category/update',
    method: 'put',
    data: category
  })
}

export const deleteCategory = (id) => {
  return request({
    url: `/admin/category/delete/${id}`,
    method: 'delete'
  })
}

// 商品管理
export const getAdminProductList = (params) => {
  return request({
    url: '/admin/product/list',
    method: 'get',
    params
  })
}

export const getAdminProductDetail = (id) => {
  return request({
    url: `/admin/product/${id}`,
    method: 'get'
  })
}

export const updateAdminProductStatus = (id, status) => {
  return request({
    url: `/admin/product/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 公告管理
export const getAdminNoticeList = (params) => {
  return request({
    url: '/admin/notice/list',
    method: 'get',
    params
  })
}

export const addNotice = (notice) => {
  return request({
    url: '/admin/notice/add',
    method: 'post',
    data: notice
  })
}

export const updateNotice = (notice) => {
  return request({
    url: '/admin/notice/update',
    method: 'put',
    data: notice
  })
}

export const deleteNotice = (id) => {
  return request({
    url: `/admin/notice/delete/${id}`,
    method: 'delete'
  })
}

export const updateNoticeStatus = (id, status) => {
  return request({
    url: `/admin/notice/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 轮播图管理
export const getAdminBannerList = () => {
  return request({
    url: '/admin/banner/list',
    method: 'get'
  })
}

export const addBanner = (banner) => {
  return request({
    url: '/admin/banner/add',
    method: 'post',
    data: banner
  })
}

export const updateBanner = (banner) => {
  return request({
    url: '/admin/banner/update',
    method: 'put',
    data: banner
  })
}

export const deleteBanner = (id) => {
  return request({
    url: `/admin/banner/delete/${id}`,
    method: 'delete'
  })
}

// 数据统计
export const getOverviewStatistics = () => {
  return request({
    url: '/admin/statistics/overview',
    method: 'get'
  })
}

