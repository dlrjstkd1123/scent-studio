import { defineStore } from 'pinia'

/** 기본 표시 시간 (ms) */
const DURATION = 3000

let timer = null

/**
 * 화면 아래에 잠깐 떴다 사라지는 안내 (App.vue 의 AppToast 가 보여준다).
 * 어느 화면에서든 useToastStore().show('문구')
 */
export const useToastStore = defineStore('toast', {
  state: () => ({
    message: ''
  }),
  actions: {
    show(message, duration = DURATION) {
      clearTimeout(timer)
      this.message = message
      timer = setTimeout(() => {
        this.message = ''
      }, duration)
    }
  }
})
