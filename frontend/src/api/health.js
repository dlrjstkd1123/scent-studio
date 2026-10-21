import axios from 'axios'
import { API_BASE } from './http'

/**
 * [연결상태 조회]
 * 홈 > 연결 상태
 * */
const getHealth = () => {
  return axios.get(`${API_BASE}/health`)
}

export { getHealth }
