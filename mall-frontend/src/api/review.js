import request from '@/utils/request'

// 发表评价
export const addReview = (review) => {
  return request({
    url: '/review/add',
    method: 'post',
    data: review
  })
}

// 我的评价
export const getMyReviews = (userId) => {
  return request({
    url: '/review/list',
    method: 'get',
    params: { userId }
  })
}

// 商品评价列表
export const getProductReviews = (productId) => {
  return request({
    url: `/review/product/${productId}`,
    method: 'get'
  })
}

