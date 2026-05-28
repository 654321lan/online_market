import request from '@/utils/request'

// 公告列表
export const getNoticeList = (page, size) => {
  return request({
    url: '/notice/list',
    method: 'get',
    params: { page, size }
  })
}

// 公告详情
export const getNoticeDetail = (id) => {
  return request({
    url: `/notice/${id}`,
    method: 'get'
  })
}

