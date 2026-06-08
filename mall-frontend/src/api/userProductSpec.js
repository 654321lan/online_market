import request from '@/utils/request'

export const getProductSpecsAndSkus = (productId) => {
  return request({
    url: `/product/spec/${productId}`,
    method: 'get'
  })
}

export const getSkuDetail = (skuId) => {
  return request({
    url: `/product/spec/sku/${skuId}`,
    method: 'get'
  })
}