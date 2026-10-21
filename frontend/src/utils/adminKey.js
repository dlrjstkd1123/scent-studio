const ADMIN_KEY_STORAGE = 'scent-admin-key'

// 관리자 키는 브라우저 탭을 닫으면 사라지도록 sessionStorage 에 둔다
export const getAdminKey = () => {
  try {
    return sessionStorage.getItem(ADMIN_KEY_STORAGE) || ''
  } catch (e) {
    return ''
  }
}

export const setAdminKey = (key) => {
  try {
    sessionStorage.setItem(ADMIN_KEY_STORAGE, key)
  } catch (e) {
    /* 저장 불가 환경: 새로고침 시 다시 입력 */
  }
}

export const clearAdminKey = () => {
  try {
    sessionStorage.removeItem(ADMIN_KEY_STORAGE)
  } catch (e) {
    /* noop */
  }
}
