import { defineStore } from 'pinia'

export const useMerchantStore = defineStore('merchant', {
  state: () => ({
    merchantInfo: JSON.parse(localStorage.getItem('merchantInfo')) || null
  }),
  
  getters: {
    isLogin: (state) => !!state.merchantInfo
  },
  
  actions: {
    setMerchantInfo(info) {
      this.merchantInfo = info
      localStorage.setItem('merchantInfo', JSON.stringify(info))
    },
    
    logout() {
      this.merchantInfo = null
      localStorage.removeItem('merchantInfo')
    }
  }
})

