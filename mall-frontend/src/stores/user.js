import { defineStore } from 'pinia'
import { getCartList } from '@/api/cart'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    cartCount: 0
  }),
  
  getters: {
    isLogin: (state) => !!state.userInfo
  },
  
  actions: {
    setUserInfo(info) {
      this.userInfo = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    },
    
    logout() {
      this.userInfo = null
      this.cartCount = 0
      localStorage.removeItem('userInfo')
    },
    
    async refreshCartCount() {
      if (!this.userInfo) return
      try {
        const res = await getCartList(this.userInfo.id)
        this.cartCount = res.data?.length || 0
      } catch (e) {
        console.error(e)
      }
    }
  }
})

