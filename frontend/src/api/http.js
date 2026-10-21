import axios from 'axios'
import { getAdminKey } from '@/utils/adminKey'

/**
 * API 기본 주소. 개발: /api (Vite 프록시 → 8090), 운영: .env.production 값
 * */
export const API_BASE = import.meta.env.VITE_API_BASE_URL

// axios 공통 설정. api/*.js 에서 이 파일을 import 하면 함께 적용된다.
axios.defaults.timeout = 15000
axios.defaults.withCredentials = true

// 관리자 API 는 저장된 관리자 키를 헤더에 붙인다 (직접 지정한 헤더가 있으면 그대로 사용)
axios.interceptors.request.use((config) => {
  if (config.url?.startsWith(`${API_BASE}/admin/`) && !config.headers['X-Admin-Key']) {
    config.headers['X-Admin-Key'] = getAdminKey()
  }
  return config
})

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('[API ERROR]', error.config?.url, error.response?.status, error.message)
    return Promise.reject(error)
  }
)
