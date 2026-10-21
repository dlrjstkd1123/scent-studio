import axios from 'axios'
import { API_BASE } from './http'

// 관리자 키(X-Admin-Key)는 http.js 인터셉터가 붙인다

/**
 * [관리자 키 확인]
 * 관리자 > 로그인
 * */
const checkAdminKey = (key) => {
  return axios.get(`${API_BASE}/admin/check`, { headers: { 'X-Admin-Key': key } })
}

/**
 * [데이터 현황 조회]
 * 관리자 > 데이터 관리
 * */
const getAdminSummary = () => {
  return axios.get(`${API_BASE}/admin/summary`)
}

/**
 * [향 계열 목록 조회]
 * 관리자 > 데이터 관리 > 계열 색상
 * */
const getScentFamilyList = () => {
  return axios.get(`${API_BASE}/admin/families`)
}

/**
 * [향 계열 수정]
 * 관리자 > 데이터 관리 > 계열 색상
 * */
const updateScentFamily = (familyCode, familyNameKo, hexColorCode) => {
  return axios.put(
    `${API_BASE}/admin/families/${encodeURIComponent(familyCode)}`,
    { familyNameKo, hexColorCode }
  )
}

/**
 * [업로드 이력 조회]
 * 관리자 > 데이터 관리 > 업로드 이력
 * */
const getImportLogList = (limit = 20) => {
  return axios.get(`${API_BASE}/admin/import-logs?limit=${limit}`)
}

/**
 * [향료 CSV 업로드]
 * 관리자 > 데이터 관리 > 향료 업로드
 * */
const uploadIngredientCsv = (formData) => {
  return axios.post(`${API_BASE}/admin/import/ingredients`, formData, { timeout: 120000 })
}

/**
 * [향수 CSV 업로드]
 * 관리자 > 데이터 관리 > 향수 업로드
 * */
const uploadPerfumeCsv = (formData) => {
  return axios.post(`${API_BASE}/admin/import/perfumes`, formData, { timeout: 120000 })
}

/**
 * [향료 별칭 CSV 업로드]
 * 관리자 > 데이터 관리 > 별칭 업로드
 * */
const uploadAliasCsv = (formData) => {
  return axios.post(`${API_BASE}/admin/import/aliases`, formData, { timeout: 120000 })
}

/**
 * [어코드 매핑 CSV 업로드]
 * 관리자 > 데이터 관리 > 어코드 매핑 업로드
 * */
const uploadAccordCsv = (formData) => {
  return axios.post(`${API_BASE}/admin/import/accords`, formData, { timeout: 120000 })
}

/**
 * [한글 검색어 CSV 업로드]
 * 관리자 > 데이터 관리 > 한글 검색어 업로드
 * */
const uploadKeywordCsv = (formData) => {
  return axios.post(`${API_BASE}/admin/import/keywords`, formData, { timeout: 120000 })
}

export {
  checkAdminKey,
  getAdminSummary,
  getScentFamilyList,
  updateScentFamily,
  getImportLogList,
  uploadIngredientCsv,
  uploadPerfumeCsv,
  uploadAliasCsv,
  uploadAccordCsv,
  uploadKeywordCsv
}
