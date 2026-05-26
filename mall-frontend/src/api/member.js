import request from '@/utils/request'

// 获取会员信息
export const getMemberInfo = (userId) => {
  return request({
    url: `/member/info/${userId}`,
    method: 'get'
  })
}

// 模拟充值
export const recharge = (userId, amount) => {
  return request({
    url: '/member/recharge',
    method: 'post',
    params: { userId, amount }
  })
}

// 充值记录
export const getRechargeRecords = (userId, params) => {
  return request({
    url: `/member/recharge/records/${userId}`,
    method: 'get',
    params
  })
}

// 获取所有会员等级
export const getAllLevels = () => {
  return request({
    url: '/member/levels',
    method: 'get'
  })
}

// 获取用户折扣信息
export const getUserDiscount = (userId) => {
  return request({
    url: `/member/discount/${userId}`,
    method: 'get'
  })
}

// 管理员 - 获取等级列表
export const getAdminLevelList = () => {
  return request({
    url: '/admin/member-level/list',
    method: 'get'
  })
}

// 管理员 - 更新等级配置
export const updateLevel = (level) => {
  return request({
    url: '/admin/member-level/update',
    method: 'put',
    data: level
  })
}

// 管理员 - 启用/禁用等级
export const updateLevelStatus = (id, status) => {
  return request({
    url: `/admin/member-level/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 管理员 - 所有充值记录
export const getAdminRechargeRecords = (params) => {
  return request({
    url: '/admin/member-level/recharge/records',
    method: 'get',
    params
  })
}

// 管理员 - 手动调整用户会员等级
export const updateUserLevel = (userId, level) => {
  return request({
    url: `/admin/member-level/user-level/${userId}`,
    method: 'put',
    params: { level }
  })
}
