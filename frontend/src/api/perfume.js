import axios from 'axios'
import { API_BASE } from './http'

/**
 * [노트 사전 조회]
 * 향수 탐색 > 노트 계열 색상 · 한글명
 * */
const getNoteDictionary = () => {
  return axios.get(`${API_BASE}/perfume/note-dictionary`)
}

/**
 * [향수 검색]
 * 향수 탐색 > 검색 (Fragella, 영문 3자 이상)
 * */
const getFragranceList = (keyword) => {
  return axios.get(`${API_BASE}/perfume/fragrances?keyword=${encodeURIComponent(keyword)}`)
}

/**
 * [비슷한 향수 조회]
 * 향수 탐색 > 검색한 향수 기준 추천 (Fragella)
 * */
const getSimilarFragranceList = (name) => {
  return axios.get(`${API_BASE}/perfume/fragrances/similar?name=${encodeURIComponent(name)}`)
}

export { getNoteDictionary, getFragranceList, getSimilarFragranceList }
