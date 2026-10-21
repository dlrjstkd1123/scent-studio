<script setup>
import { ref } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
  /** 양식 파일 경로 (public 기준) */
  templateUrl: { type: String, default: '' },
  /** (formData) => Promise<axios response> */
  uploader: { type: Function, required: true }
})

const emit = defineEmits(['done'])

const inputRef = ref(null)
const file = ref(null)
const dragging = ref(false)
const uploading = ref(false)
const result = ref(null)
const errorMessage = ref('')

const pick = (f) => {
  if (!f) return
  if (!/\.csv$/i.test(f.name)) {
    errorMessage.value = 'CSV 파일만 올릴 수 있습니다.'
    return
  }
  file.value = f
  result.value = null
  errorMessage.value = ''
}

const onChange = (e) => pick(e.target.files[0])

const onDrop = (e) => {
  dragging.value = false
  pick(e.dataTransfer.files[0])
}

const clearFile = () => {
  file.value = null
  if (inputRef.value) inputRef.value.value = ''
}

const upload = () => {
  if (!file.value || uploading.value) return
  uploading.value = true
  result.value = null
  errorMessage.value = ''

  const formData = new FormData()
  formData.append('file', file.value)

  props.uploader(formData)
    .then((res) => {
      const { code, message, response } = res.data
      switch (parseInt(code)) {
      case 100:
        result.value = response
        clearFile()
        break
      case 102:
      case 103:
        // CSV 형식 오류(103) · 저장 오류(102): 이력은 남고 사유를 보여준다
        errorMessage.value = message
        break
      default:
        errorMessage.value = message
        break
      }
    })
    .catch((err) => {
      errorMessage.value = err.response?.data?.message
        || '업로드에 실패했습니다. 서버 상태를 확인하세요.'
      console.log(err)
    })
    .finally(() => {
      uploading.value = false
      emit('done')
    })
}
</script>

<template>
  <section class="panel">
    <header class="panel-head">
      <h3>{{ title }}</h3>
      <a v-if="templateUrl" :href="templateUrl" class="template-link" download>양식 받기</a>
    </header>
    <p v-if="description" class="panel-desc">{{ description }}</p>

    <label
      class="dropzone"
      :class="{ 'is-dragging': dragging, 'has-file': file }"
      @dragover.prevent="dragging = true"
      @dragleave.prevent="dragging = false"
      @drop.prevent="onDrop"
    >
      <input
        ref="inputRef"
        type="file"
        accept=".csv,text/csv"
        class="sr-only"
        @change="onChange"
      />
      <span v-if="file" class="file-name">{{ file.name }}</span>
      <span v-else>CSV 파일을 끌어다 놓거나 <u>선택</u>하세요</span>
    </label>

    <button type="button" class="upload-btn" :disabled="!file || uploading" @click="upload">
      {{ uploading ? '업로드 중…' : '업로드' }}
    </button>

    <div v-if="result" class="result" role="status">
      <ul class="result-counts">
        <li>전체 <b>{{ result.totalCount }}</b></li>
        <li class="ok">추가 <b>{{ result.insertedCount }}</b></li>
        <li class="ok">갱신 <b>{{ result.updatedCount }}</b></li>
        <li :class="{ bad: result.failedCount }">실패 <b>{{ result.failedCount }}</b></li>
        <li v-if="result.newIngredientCount">새 향료 <b>{{ result.newIngredientCount }}</b></li>
      </ul>
      <ul v-if="result.errors?.length" class="result-errors">
        <li v-for="err in result.errors.slice(0, 20)" :key="err">{{ err }}</li>
        <li v-if="result.errors.length > 20">외 {{ result.errors.length - 20 }}건</li>
      </ul>
    </div>
    <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
  </section>
</template>

<style scoped>
.panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 4px 18px rgba(91, 58, 34, 0.06);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.panel-head h3 {
  font-size: 16px;
  font-weight: 700;
}

.template-link {
  font-size: 13px;
  color: var(--c-brown-600);
  text-decoration: underline;
}

.panel-desc {
  font-size: 13px;
  line-height: 1.6;
  color: var(--c-muted);
}

.dropzone {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 84px;
  padding: 12px;
  border: 1.5px dashed #d9cbb8;
  border-radius: 10px;
  background: var(--c-cream);
  font-size: 14px;
  color: var(--c-muted);
  text-align: center;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.dropzone:hover,
.dropzone.is-dragging {
  border-color: var(--c-brown-500);
  background: #f3ebe0;
}

.dropzone.has-file {
  border-style: solid;
  border-color: var(--c-brown-500);
}

.file-name {
  font-weight: 700;
  color: var(--c-text);
  word-break: break-all;
}

.upload-btn {
  height: 42px;
  border-radius: 10px;
  background: var(--c-brown-600);
  color: #fff;
  font-weight: 700;
  transition: background-color 0.2s ease;
}

.upload-btn:hover:not(:disabled) {
  background: var(--c-brown-700);
}

.upload-btn:disabled {
  background: #cdbfae;
  cursor: not-allowed;
}

.result {
  padding: 12px 14px;
  border-radius: 10px;
  background: var(--c-cream);
}

.result-counts {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 14px;
  font-size: 13px;
  color: var(--c-muted);
}

.result-counts b {
  margin-left: 2px;
  color: var(--c-text);
}

.result-counts .ok b {
  color: #2f7a3e;
}

.result-counts .bad b {
  color: #b3261e;
}

.result-errors {
  margin-top: 8px;
  max-height: 160px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.7;
  color: #b3261e;
}

.error {
  font-size: 13px;
  line-height: 1.6;
  color: #b3261e;
  white-space: pre-line;
}
</style>
