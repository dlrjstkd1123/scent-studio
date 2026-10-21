<script setup>
import { ref, watch } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'

/**
 * 검색창. 검색 자체는 부모가 한다: Enter · 검색 버튼을 누르면 search 이벤트로 검색어를 알리고,
 * 결과는 results 로 받는다. 검색 1번이 외부 API 사용량 1회라서 글자를 칠 때마다 검색하지 않는다.
 */
const props = defineProps({
  results: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  /** 검색 실패 (사유는 부모가 토스트로 안내) */
  failed: { type: Boolean, default: false },
  /** 한글로 검색해서 영문으로 바꿔 검색했을 때의 영문 검색어 (예: Dior Sauvage) */
  searchedAs: { type: String, default: '' }
})

/** 최소 글자 수: 영문은 Fragella 기준 3자, 한글은 2자 ("샤넬") */
const MIN_ENGLISH = 3
const MIN_HANGUL = 2
const HANGUL = /[가-힣ㄱ-ㅎㅏ-ㅣ]/

const emit = defineEmits(['search', 'select'])

const keyword = ref('')
const open = ref(false)
const activeIndex = ref(-1)
/** 마지막으로 검색한 검색어. 입력이 이것과 다르면 "Enter 로 검색" 안내 */
const searched = ref('')

watch(keyword, (value) => {
  if (!value.trim()) open.value = false
})

watch(
  () => props.results,
  (list) => {
    activeIndex.value = list.length ? 0 : -1
  }
)

const minLength = () => (HANGUL.test(keyword.value) ? MIN_HANGUL : MIN_ENGLISH)
const tooShort = () => keyword.value.trim().length < minLength()
const changed = () => keyword.value.trim() !== searched.value

const choose = (perfume) => {
  emit('select', perfume)
  keyword.value = ''
  searched.value = ''
  open.value = false
}

const search = () => {
  const value = keyword.value.trim()
  open.value = true
  if (tooShort() || props.loading) return
  searched.value = value
  emit('search', value)
}

const onKeydown = (e) => {
  if (!open.value || !props.results.length) return
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    activeIndex.value = (activeIndex.value + 1) % props.results.length
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    activeIndex.value = (activeIndex.value - 1 + props.results.length) % props.results.length
  } else if (e.key === 'Escape') {
    open.value = false
  }
}

/** Enter: 검색어가 바뀌었으면 검색, 그대로면 고른 결과 선택 */
const submit = () => {
  const picked = props.results[activeIndex.value]
  if (!changed() && open.value && picked) choose(picked)
  else search()
}

/** 인기 검색어 등 외부에서 검색어를 채우고 바로 검색 */
const setKeyword = (value) => {
  keyword.value = value
  search()
}

defineExpose({ setKeyword })
</script>

<template>
  <form class="search" role="search" @submit.prevent="submit">
    <AppIcon name="search" :size="20" class="search-icon" />
    <input
      v-model="keyword"
      type="search"
      class="search-input"
      placeholder="향수 · 브랜드 검색 (한글 · 영문)"
      autocomplete="off"
      role="combobox"
      aria-label="향수 검색"
      aria-autocomplete="list"
      :aria-expanded="open"
      aria-controls="perfume-search-list"
      @keydown="onKeydown"
      @focus="open = results.length > 0"
      @blur="open = false"
    />
    <button type="submit" class="search-submit" aria-label="검색">
      <AppIcon name="arrow" :size="20" />
    </button>

    <ul v-if="open" id="perfume-search-list" class="suggestions" role="listbox">
      <li v-if="tooShort()" class="no-result">{{ minLength() }}자 이상 입력해 주세요.</li>
      <li v-else-if="loading" class="no-result">검색 중이에요…</li>
      <li v-else-if="changed()" class="no-result">Enter 를 누르면 검색해요.</li>
      <template v-else>
        <li v-if="searchedAs" class="searched-as">
          <span><b>{{ searchedAs }}</b>(으)로 검색했어요</span>
        </li>
        <li
          v-for="(p, i) in results"
          :key="p.id"
          role="option"
          :aria-selected="i === activeIndex"
          :class="{ active: i === activeIndex }"
          @mousedown.prevent="choose(p)"
          @mouseenter="activeIndex = i"
        >
          <span class="s-name">{{ p.name }}</span>
          <span class="s-brand">{{ p.brand }}</span>
        </li>
        <li v-if="failed" class="no-result">검색하지 못했어요.</li>
        <li v-else-if="!results.length" class="no-result">검색 결과가 없어요.</li>
      </template>
    </ul>
  </form>
</template>

<style scoped>
.search {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  height: 60px;
  padding: 0 8px 0 22px;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(28, 18, 11, 0.18);
}

.search-icon {
  flex: none;
  color: var(--c-muted);
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  padding: 0 14px;
  border: 0;
  background: none;
  font: inherit;
  font-size: 16px;
  color: var(--c-text);
  outline: none;
}

.search-input::-webkit-search-cancel-button {
  display: none;
}

.search-submit {
  display: inline-flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-left: 1px solid #eee4d7;
  color: var(--c-text);
}

.suggestions {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  z-index: 20;
  overflow: hidden;
  padding: 6px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 16px 40px rgba(28, 18, 11, 0.2);
  text-align: left;
}

.suggestions li {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  color: var(--c-text);
}

.suggestions li.active {
  background: var(--c-cream);
}

.s-name {
  font-weight: 700;
}

.s-brand {
  font-size: 13px;
  color: var(--c-muted);
}

.searched-as {
  justify-content: flex-start !important;
  cursor: default !important;
  font-size: 13px;
  color: var(--c-muted) !important;
  background: none !important;
}

.searched-as b {
  margin-right: 2px;
  color: var(--c-text);
}

.no-result {
  justify-content: center !important;
  cursor: default !important;
  font-size: 14px;
  color: var(--c-muted) !important;
}

@media (max-width: 767px) {
  .search {
    height: 52px;
    padding-left: 16px;
  }

  .search-input {
    font-size: 15px;
  }

  .search-submit {
    width: 40px;
    height: 40px;
  }
}
</style>
