import request from '@/utils/request'

// 用户注册
export const register = (username, password) => {
  return request({
    url: '/user/register',
    method: 'post',
    params: { username, password }
  })
}

// 用户登录
export const login = (username, password) => {
  return request({
    url: '/user/login',
    method: 'post',
    params: { username, password }
  })
}

// 获取用户信息
export const getUserInfo = (id) => {
  return request({
    url: `/user/info/${id}`,
    method: 'get'
  })
}

// 更新用户信息
export const updateUser = (user) => {
  return request({
    url: '/user/update',
    method: 'put',
    data: user
  })
}

// 修改密码
export const updatePassword = (id, oldPassword, newPassword) => {
  return request({
    url: '/user/password',
    method: 'put',
    params: { id, oldPassword, newPassword }
  })
}

