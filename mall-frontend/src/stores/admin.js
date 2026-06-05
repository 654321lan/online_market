import { defineStore } from 'pinia'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    adminInfo: JSON.parse(localStorage.getItem('adminInfo')) || null
  }),
  
  getters: {
    isLogin: (state) => !!state.adminInfo
  },
  
  actions: {
    setAdminInfo(info) {
      this.adminInfo = info
      localStorage.setItem('adminInfo', JSON.stringify(info))
    },
    
    logout() {
      this.adminInfo = null
      localStorage.removeItem('adminInfo')
    }
  }
})

