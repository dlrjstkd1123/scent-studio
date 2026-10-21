<script setup>
import { onMounted, reactive, ref } from 'vue'
import UploadPanel from '@/components/admin/UploadPanel.vue'
import {
  checkAdminKey,
  getAdminSummary,
  getImportLogList,
  getScentFamilyList,
  updateScentFamily,
  uploadAccordCsv,
  uploadAliasCsv,
  uploadIngredientCsv,
  uploadKeywordCsv,
  uploadPerfumeCsv
} from '@/api/admin'
import { clearAdminKey, getAdminKey, setAdminKey } from '@/utils/adminKey'

const IMPORT_TYPE_LABEL = {
  INGREDIENT_CSV: '향료 CSV',
  PERFUME_CSV: '향수 CSV',
  ALIAS_CSV: '향료 별칭 CSV',
  ACCORD_CSV: '어코드 매핑 CSV',
  KEYWORD_CSV: '한글 검색어 CSV'
}

const SUMMARY_ITEMS = [
  { key: 'perfumeCount', label: '향수' },
  { key: 'ingredientCount', label: '향료' },
  { key: 'unassignedIngredientCount', label: '계열 미지정 향료', warn: true },
  { key: 'ingredientAliasCount', label: '향료 별칭' },
  { key: 'accordFamilyMapCount', label: '어코드 매핑' },
  { key: 'keywordTranslationCount', label: '한글 검색어' },
  { key: 'perfumeNoteCount', label: '향수-노트 연결' },
  { key: 'ifraStandardCount', label: 'IFRA 규제 물질' }
]

const UNKNOWN_ERROR = '예기치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'

const UPLOAD_GUIDE = {
  ingredient: '향료 영문명 · 한글명 · 계열 · 휘발도. 같은 영문명이 있으면 갱신합니다. '
    + '향수보다 먼저 올리면 계열이 채워진 상태로 연결됩니다.',
  perfume: '향수 + 탑/미들/베이스 노트(| 로 구분) + 어코드(이름:강도). '
    + '같은 브랜드·이름이면 노트를 CSV 내용으로 교체합니다. 처음 보는 향료는 자동 추가됩니다.',
  alias: '같은 향료의 다른 표기(예: Cedar → Cedarwood). 향수 노트 이름이 별칭이면 그 향료의 '
    + '계열 · 한글명을 씁니다. 가리키는 향료가 먼저 등록되어 있어야 합니다.',
  keyword: '한글 검색어 → Fragella 로 보낼 영문 검색어 (예: 디올 소바쥬 → Dior Sauvage). '
    + '처음 보는 한글은 AI(Gemini)가 바꿔서 저장하고, 여기서 올린 값은 AI 값보다 우선합니다.',
  accord: 'Fragella 메인 어코드(소문자, 예: fresh spicy) → 향 계열 코드. '
    + '계열 비중(추정) 그래프와 분위기 계산에 쓰입니다. 같은 어코드가 있으면 계열을 바꿉니다.'
}

// ------------------------------------------------------------------ 로그인
const authed = ref(false)
const checking = ref(true)
const keyInput = ref('')
const loginError = ref('')
const pageError = ref('')

const login = () => {
  const key = keyInput.value.trim()
  if (!key) return
  loginError.value = ''
  checkAdminKey(key)
    .then((res) => {
      const { code, message } = res.data
      switch (parseInt(code)) {
      case 100:
        setAdminKey(key)
        keyInput.value = ''
        authed.value = true
        loadAll()
        break
      default:
        loginError.value = message
        break
      }
    })
    .catch((err) => {
      loginError.value = err.response?.data?.message || '서버에 연결할 수 없습니다.'
      console.log(err)
    })
}

const logout = () => {
  clearAdminKey()
  authed.value = false
}

// 관리자 키가 만료/변경되면(401) 로그인 화면으로
const handleError = (err) => {
  if (err.response?.status === 401) {
    logout()
    loginError.value = err.response.data?.message || ''
    return
  }
  pageError.value = UNKNOWN_ERROR
  console.log(err)
}

// ------------------------------------------------------------------ 데이터
const summary = ref(null)
const families = ref([])
const logs = ref([])
const familyForm = reactive({}) // familyCode → { familyNameKo, hexColorCode, saving, saved, error }

const loadSummary = () => {
  getAdminSummary()
    .then((res) => {
      const { code, message, response } = res.data
      switch (parseInt(code)) {
      case 100:
        summary.value = response
        break
      default:
        pageError.value = message
        break
      }
    })
    .catch(handleError)
}

const loadFamilies = () => {
  getScentFamilyList()
    .then((res) => {
      const { code, message, response } = res.data
      switch (parseInt(code)) {
      case 100:
        families.value = response
        response.forEach((family) => {
          familyForm[family.familyCode] = {
            familyNameKo: family.familyNameKo || '',
            hexColorCode: family.hexColorCode,
            saving: false,
            saved: false,
            error: ''
          }
        })
        break
      case 101:
        families.value = []
        break
      default:
        pageError.value = message
        break
      }
    })
    .catch(handleError)
}

const loadLogs = () => {
  getImportLogList(20)
    .then((res) => {
      const { code, message, response } = res.data
      switch (parseInt(code)) {
      case 100:
        logs.value = response
        break
      case 101:
        logs.value = []
        break
      default:
        pageError.value = message
        break
      }
    })
    .catch(handleError)
}

const loadAll = () => {
  pageError.value = ''
  loadSummary()
  loadFamilies()
  loadLogs()
}

const isFamilyChanged = (family) => {
  const form = familyForm[family.familyCode]
  if (!form) return false
  const nameChanged = form.familyNameKo !== (family.familyNameKo || '')
  const colorChanged = form.hexColorCode.toUpperCase() !== family.hexColorCode.toUpperCase()
  return nameChanged || colorChanged
}

const saveFamily = (family) => {
  const form = familyForm[family.familyCode]
  form.saving = true
  form.error = ''
  updateScentFamily(family.familyCode, form.familyNameKo, form.hexColorCode)
    .then((res) => {
      const { code, message } = res.data
      switch (parseInt(code)) {
      case 100:
        family.familyNameKo = form.familyNameKo
        family.hexColorCode = form.hexColorCode.toUpperCase()
        form.saved = true
        setTimeout(() => {
          form.saved = false
        }, 1500)
        break
      case 101:
        form.error = '없는 계열입니다.'
        break
      default:
        form.error = message
        break
      }
    })
    .catch(handleError)
    .finally(() => {
      form.saving = false
    })
}

onMounted(() => {
  const savedKey = getAdminKey()
  if (!savedKey) {
    checking.value = false
    return
  }
  checkAdminKey(savedKey)
    .then((res) => {
      const { code } = res.data
      switch (parseInt(code)) {
      case 100:
        authed.value = true
        loadAll()
        break
      default:
        clearAdminKey()
        break
      }
    })
    .catch((err) => {
      clearAdminKey()
      console.log(err)
    })
    .finally(() => {
      checking.value = false
    })
})
</script>

<template>
  <div class="admin">
    <header class="admin-header">
      <div class="admin-header-inner">
        <RouterLink to="/home" class="logo">SCENT STUDIO</RouterLink>
        <span class="admin-badge">관리자</span>
        <button v-if="authed" type="button" class="logout-btn" @click="logout">로그아웃</button>
      </div>
    </header>

    <!-- 로그인 -->
    <main v-if="!authed && !checking" class="login">
      <form class="login-card" @submit.prevent="login">
        <h1>관리자 로그인</h1>
        <p>서버에 설정된 관리자 키를 입력하세요.</p>
        <input
          v-model="keyInput"
          type="password"
          placeholder="관리자 키"
          autocomplete="current-password"
          aria-label="관리자 키"
        />
        <button type="submit" :disabled="!keyInput.trim()">확인</button>
        <p v-if="loginError" class="login-error" role="alert">{{ loginError }}</p>
      </form>
    </main>

    <!-- 데이터 관리 -->
    <main v-else-if="authed" class="admin-main">
      <div class="page-head">
        <h1>데이터 관리</h1>
        <p>향료 · 향수 데이터를 CSV로 올리고, 향 계열 색상을 관리합니다.</p>
      </div>

      <p v-if="pageError" class="page-error" role="alert">{{ pageError }}</p>

      <ul class="summary">
        <li
          v-for="item in SUMMARY_ITEMS"
          :key="item.key"
          :class="{ warn: item.warn && summary?.[item.key] > 0 }"
        >
          <span class="summary-label">{{ item.label }}</span>
          <b class="summary-value">
            {{ summary ? Number(summary[item.key]).toLocaleString() : '-' }}
          </b>
        </li>
      </ul>

      <h2 class="section-title">데이터 업로드</h2>
      <div class="upload-grid">
        <UploadPanel
          title="① 향료 CSV"
          :description="UPLOAD_GUIDE.ingredient"
          template-url="/templates/ingredient_template.csv"
          :uploader="uploadIngredientCsv"
          @done="loadAll"
        />
        <UploadPanel
          title="② 향수 CSV"
          :description="UPLOAD_GUIDE.perfume"
          template-url="/templates/perfume_template.csv"
          :uploader="uploadPerfumeCsv"
          @done="loadAll"
        />
        <UploadPanel
          title="③ 향료 별칭 CSV"
          :description="UPLOAD_GUIDE.alias"
          template-url="/templates/alias_template.csv"
          :uploader="uploadAliasCsv"
          @done="loadAll"
        />
        <UploadPanel
          title="④ 어코드 매핑 CSV"
          :description="UPLOAD_GUIDE.accord"
          template-url="/templates/accord_template.csv"
          :uploader="uploadAccordCsv"
          @done="loadAll"
        />
        <UploadPanel
          title="⑤ 한글 검색어 CSV"
          :description="UPLOAD_GUIDE.keyword"
          template-url="/templates/keyword_template.csv"
          :uploader="uploadKeywordCsv"
          @done="loadAll"
        />
        <section class="panel-placeholder">
          <h3>⑥ IFRA 규제 엑셀</h3>
          <p>
            IFRA 공식 사이트(ifrafragrance.org)의 <b>IFRA Standards overview</b> 엑셀을 받아
            프로젝트에 넣어주시면, 실제 컬럼 구조에 맞춰 업로드 기능을 연결합니다.
          </p>
          <span class="coming">준비 중</span>
        </section>
      </div>

      <h2 class="section-title">향 계열 색상</h2>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>색상</th>
              <th>코드</th>
              <th>영문명</th>
              <th>한글명</th>
              <th class="num">향료 수</th>
              <th />
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in families" :key="f.familyCode">
              <td>
                <label class="color-cell">
                  <input
                    v-model="familyForm[f.familyCode].hexColorCode"
                    type="color"
                    :aria-label="`${f.familyNameEn} 색상`"
                  />
                  <input
                    v-model="familyForm[f.familyCode].hexColorCode"
                    class="hex-input"
                    maxlength="7"
                    :aria-label="`${f.familyNameEn} 색상 코드`"
                  />
                </label>
              </td>
              <td class="mono">{{ f.familyCode }}</td>
              <td>{{ f.familyNameEn }}</td>
              <td>
                <input
                  v-model="familyForm[f.familyCode].familyNameKo"
                  class="text-input"
                  :aria-label="`${f.familyNameEn} 한글명`"
                />
              </td>
              <td class="num">{{ f.ingredientCount }}</td>
              <td class="actions">
                <span v-if="familyForm[f.familyCode].saved" class="saved">저장됨</span>
                <span v-if="familyForm[f.familyCode].error" class="row-error">
                  {{ familyForm[f.familyCode].error }}
                </span>
                <button
                  type="button"
                  class="small-btn"
                  :disabled="!isFamilyChanged(f) || familyForm[f.familyCode].saving"
                  @click="saveFamily(f)"
                >
                  저장
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <h2 class="section-title">업로드 이력 <small>최근 20건</small></h2>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>일시</th>
              <th>종류</th>
              <th>파일</th>
              <th class="num">전체</th>
              <th class="num">추가</th>
              <th class="num">갱신</th>
              <th class="num">실패</th>
              <th>메시지</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!logs.length">
              <td colspan="8" class="empty">아직 업로드 이력이 없습니다.</td>
            </tr>
            <tr v-for="log in logs" :key="log.logId">
              <td class="nowrap">{{ log.createdAt }}</td>
              <td class="nowrap">{{ IMPORT_TYPE_LABEL[log.importType] || log.importType }}</td>
              <td class="file">{{ log.fileName }}</td>
              <td class="num">{{ log.totalCount }}</td>
              <td class="num">{{ log.insertedCount }}</td>
              <td class="num">{{ log.updatedCount }}</td>
              <td class="num" :class="{ bad: log.failedCount }">{{ log.failedCount }}</td>
              <td class="message">{{ log.message }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin {
  min-height: 100vh;
  background: #f5f1eb;
}

/* ------------------------------------------------------------------ 헤더 */
.admin-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--c-brown-900);
  color: #f5ece0;
}

.admin-header-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 1200px;
  height: 60px;
  margin: 0 auto;
  padding: 0 24px;
}

.logo {
  font-family: var(--f-serif-en);
  font-size: 22px;
  letter-spacing: 0.05em;
}

.admin-badge {
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(239, 226, 207, 0.16);
  font-size: 12px;
}

.logout-btn {
  margin-left: auto;
  font-size: 14px;
  opacity: 0.85;
}

.logout-btn:hover {
  opacity: 1;
}

/* ------------------------------------------------------------------ 로그인 */
.login {
  display: flex;
  justify-content: center;
  padding: 12vh 20px;
}

.login-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: min(100%, 380px);
  padding: 32px 28px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(91, 58, 34, 0.08);
}

.login-card h1 {
  font-size: 20px;
}

.login-card p {
  font-size: 14px;
  color: var(--c-muted);
}

.login-card input,
.text-input,
.hex-input {
  height: 44px;
  padding: 0 12px;
  border: 1px solid #ddd0bf;
  border-radius: 10px;
  background: #fff;
  font: inherit;
  font-size: 15px;
}

.login-card button {
  height: 46px;
  border-radius: 10px;
  background: var(--c-brown-600);
  color: #fff;
  font-weight: 700;
}

.login-card button:disabled {
  background: #cdbfae;
  cursor: not-allowed;
}

.login-error {
  color: #b3261e !important;
}

.page-error {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 10px;
  background: #fbeaea;
  font-size: 14px;
  color: #b3261e;
}

/* ------------------------------------------------------------------ 본문 */
.admin-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px 80px;
}

.page-head h1 {
  font-size: 24px;
}

.page-head p {
  margin-top: 6px;
  font-size: 14px;
  color: var(--c-muted);
}

.summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 24px;
}

.summary li {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 18px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 4px 18px rgba(91, 58, 34, 0.06);
}

.summary-label {
  font-size: 13px;
  color: var(--c-muted);
}

.summary-value {
  font-size: 26px;
  font-weight: 700;
}

.summary li.warn .summary-value {
  color: #c26a00;
}

.section-title {
  margin: 36px 0 14px;
  font-size: 18px;
}

.section-title small {
  margin-left: 6px;
  font-size: 13px;
  font-weight: 400;
  color: var(--c-muted);
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  align-items: start;
}

.panel-placeholder {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
  border: 1.5px dashed #d9cbb8;
  border-radius: 14px;
}

.panel-placeholder h3 {
  font-size: 16px;
}

.panel-placeholder p {
  font-size: 13px;
  line-height: 1.6;
  color: var(--c-muted);
}

.coming {
  align-self: flex-start;
  padding: 4px 10px;
  border-radius: 999px;
  background: #ece3d6;
  font-size: 12px;
  color: var(--c-brown-700);
}

/* ------------------------------------------------------------------ 표 */
.table-wrap {
  overflow-x: auto;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 4px 18px rgba(91, 58, 34, 0.06);
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table th,
.table td {
  padding: 12px 14px;
  border-bottom: 1px solid #f0e8dd;
  text-align: left;
  vertical-align: middle;
}

.table th {
  background: #faf6f0;
  font-size: 13px;
  font-weight: 700;
  color: var(--c-muted);
  white-space: nowrap;
}

.table tbody tr:last-child td {
  border-bottom: 0;
}

.num {
  text-align: right !important;
  font-variant-numeric: tabular-nums;
}

.mono {
  font-family: ui-monospace, Consolas, monospace;
  font-size: 13px;
}

.nowrap {
  white-space: nowrap;
}

.file {
  max-width: 220px;
  word-break: break-all;
}

.message {
  min-width: 220px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--c-muted);
  white-space: pre-line;
}

.bad {
  color: #b3261e;
  font-weight: 700;
}

.empty {
  padding: 28px !important;
  text-align: center !important;
  color: var(--c-muted);
}

.color-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-cell input[type='color'] {
  width: 36px;
  height: 36px;
  padding: 0;
  border: 1px solid #ddd0bf;
  border-radius: 8px;
  background: none;
  cursor: pointer;
}

.hex-input {
  width: 96px;
  height: 36px;
  font-family: ui-monospace, Consolas, monospace;
  font-size: 13px;
}

.text-input {
  width: 130px;
  height: 36px;
  font-size: 14px;
}

.actions {
  white-space: nowrap;
  text-align: right !important;
}

.small-btn {
  height: 34px;
  padding: 0 14px;
  border-radius: 8px;
  background: var(--c-brown-600);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}

.small-btn:disabled {
  background: #e5dbcd;
  color: #9d8f80;
  cursor: not-allowed;
}

.saved {
  margin-right: 8px;
  font-size: 12px;
  color: #2f7a3e;
}

.row-error {
  margin-right: 8px;
  font-size: 12px;
  color: #b3261e;
}

@media (max-width: 1023px) {
  .summary {
    grid-template-columns: repeat(3, 1fr);
  }

  .upload-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 599px) {
  .admin-main {
    padding: 24px 16px 60px;
  }

  .summary {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
