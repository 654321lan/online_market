import request from '@/utils/request'

// 商品列表
export const getProductList = (params) => {
  return request({
    url: '/product/list',
    method: 'get',
    params
  })
}

// 商品详情
export const getProductDetail = (id) => {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

// 分类列表
export const getCategoryList = (parentId) => {
  return request({
    url: '/category/list',
    method: 'get',
    params: { parentId }
  })
}

// 轮播图列表
export const getBannerList = () => {
  return request({
    url: '/banner/list',
    method: 'get'
  })
}

