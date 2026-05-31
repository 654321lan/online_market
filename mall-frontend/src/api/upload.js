import axios from 'axios'
import { ElMessage } from 'element-plus'

// 文件上传
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return axios.post('/api/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => {
    if (res.data.code === 200) {
      return res.data
    } else {
      ElMessage.error(res.data.message || '上传失败')
      return Promise.reject(res.data.message)
    }
  }).catch(err => {
    ElMessage.error('上传失败')
    return Promise.reject(err)
  })
}

