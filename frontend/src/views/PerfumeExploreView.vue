<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import AppIcon from '@/components/common/AppIcon.vue'
import SiteHeader from '@/components/layout/SiteHeader.vue'
import AccordBars from '@/components/perfume/AccordBars.vue'
import BottleIllust from '@/components/perfume/BottleIllust.vue'
import FamilyComposition from '@/components/perfume/FamilyComposition.vue'
import NotePyramid from '@/components/perfume/NotePyramid.vue'
import PerfumeCard from '@/components/perfume/PerfumeCard.vue'
import PerfumeSearchBox from '@/components/perfume/PerfumeSearchBox.vue'
import { getFragranceList, getSimilarFragranceList } from '@/api/perfume'
import { useNoteDictionaryStore } from '@/stores/noteDictionary'
import { useToastStore } from '@/stores/toast'
import { OCCASION_OPTIONS, SEASON_OPTIONS, fromFragella } from '@/utils/fragella'
import { buildProfile, rankByFilters, rankSimilar } from '@/utils/scentProfile'

/** 누르면 바로 검색한다 (검색 1번 = Fragella 사용량 1회). 한글 검색어는 영문으로 바꿔 검색 */
const POPULAR_KEYWORDS = ['디올 소바쥬', '르라보 상탈 33', 'Black Opium', 'Light Blue']

const UNKNOWN_ERROR = '예기치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
/** 외부 API(Fragella) 오류 안내 — 150: 호출 실패, 151: 월 사용량 초과 */
const FRAGELLA_ERROR = '향수 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.'
const FRAGELLA_LIMIT = '이번 달 향수 검색 가능 횟수를 모두 사용했어요.'
/** 152: 한글 검색어 → 영문 변환 실패 */
const TRANSLATE_ERROR = '한글 검색어를 바꾸지 못했어요. 영문으로 검색해 주세요.'

const dict = useNoteDictionaryStore()
const toast = useToastStore()

const selected = ref(null)
const analysisRef = ref(null)
const searchRef = ref(null)

const selFamilies = ref([])
const selSeasons = ref([])
const selOccasions = ref([])

// ------------------------------------------------------------------ 검색
const searchResults = ref([])
const searching = ref(false)
const searchFailed = ref(false)
/** 한글로 검색했을 때 실제로 검색한 영문 검색어 */
const searchedAs = ref('')

const onSearch = (keyword) => {
  searching.value = true
  searchFailed.value = false
  searchResults.value = []
  searchedAs.value = ''
  getFragranceList(keyword)
    .then((res) => {
      const { code, message, response } = res.data
      switch (parseInt(code)) {
      case 100:
        searchResults.value = response.fragrances.map(fromFragella)
        searchedAs.value = response.translated ? response.searchKeyword : ''
        break
      case 101:
        // 결과 없음 (한글이 향수 검색어가 아니면 searchKeyword 가 null)
        searchResults.value = []
        searchedAs.value = response?.translated ? response.searchKeyword || '' : ''
        break
      case 152:
        searchFailed.value = true
        toast.show(TRANSLATE_ERROR)
        break
      case 150:
        searchFailed.value = true
        toast.show(FRAGELLA_ERROR)
        break
      case 151:
        searchFailed.value = true
        toast.show(FRAGELLA_LIMIT)
        break
      default:
        searchFailed.value = true
        toast.show(message)
        break
      }
    })
    .catch((err) => {
      searchFailed.value = true
      toast.show(UNKNOWN_ERROR)
      console.log(err)
    })
    .finally(() => {
      searching.value = false
    })
}

// ------------------------------------------------------------------ 비슷한 향수 (Fragella 추천 목록)
const similarPool = ref([])
const similarLoading = ref(false)
const similarFailed = ref(false)
// 다른 향수를 연달아 고르면 늦게 온 이전 응답은 버린다
let similarRequestNo = 0

const loadSimilar = (perfume) => {
  const requestNo = ++similarRequestNo
  similarPool.value = []
  similarFailed.value = false
  similarLoading.value = true
  getSimilarFragranceList(perfume.name)
    .then((res) => {
      if (requestNo !== similarRequestNo) return
      const { code, message, response } = res.data
      switch (parseInt(code)) {
      case 100:
        similarPool.value = response.similar_fragrances
          .map(fromFragella)
          .filter((p) => p.id !== perfume.id)
        break
      case 101:
        similarPool.value = []
        break
      case 150:
        similarFailed.value = true
        toast.show(FRAGELLA_ERROR)
        break
      case 151:
        similarFailed.value = true
        toast.show(FRAGELLA_LIMIT)
        break
      default:
        similarFailed.value = true
        toast.show(message)
        break
      }
    })
    .catch((err) => {
      if (requestNo !== similarRequestNo) return
      similarFailed.value = true
      toast.show(UNKNOWN_ERROR)
      console.log(err)
    })
    .finally(() => {
      if (requestNo === similarRequestNo) similarLoading.value = false
    })
}

// ------------------------------------------------------------------ 향 프로필 (사전이 로드되면 다시 계산)
const profiles = computed(
  () => new Map(similarPool.value.map((p) => [p.id, buildProfile(p, dict)]))
)
const profileOf = (p) => profiles.value.get(p.id) || buildProfile(p, dict)
const selectedProfile = computed(() => (selected.value ? buildProfile(selected.value, dict) : null))

const noteLabel = (key) => {
  const ingredient = dict.ingredientByName.get(key)
  if (ingredient) return ingredient.ingredientNameKo || ingredient.ingredientNameEn
  return key.replace(/\b\w/g, (c) => c.toUpperCase())
}

// ------------------------------------------------------------------ 검색한 향수와 같은 향료가 많은 순
const similar = computed(() =>
  selected.value
    ? rankSimilar(selected.value, selectedProfile.value, similarPool.value, profileOf)
    : []
)

// ------------------------------------------------------------------ 필터 (비슷한 향수 안에서 다시 고르기)
const hasFilter = computed(() => {
  const selectedCount = selFamilies.value.length
    + selSeasons.value.length
    + selOccasions.value.length
  return selectedCount > 0
})

const filtered = computed(() =>
  rankByFilters(similarPool.value, profileOf, {
    families: selFamilies.value,
    seasons: selSeasons.value,
    occasions: selOccasions.value
  })
)

// 템플릿에서는 ref 가 풀린 배열이 넘어온다
const toggle = (list, value) => {
  const i = list.indexOf(value)
  if (i >= 0) list.splice(i, 1)
  else list.push(value)
}

const resetFilters = () => {
  selFamilies.value = []
  selSeasons.value = []
  selOccasions.value = []
}

const familyName = (code) => dict.familyByCode.get(code)?.familyNameKo || code

// ------------------------------------------------------------------ 선택
// 검색 · 추천 응답에 전체 정보가 들어 있어 상세 조회 없음. 비슷한 향수만 새로 부른다
const select = async (perfume) => {
  selected.value = perfume
  loadSimilar(perfume)
  await nextTick()
  analysisRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const clearSelected = () => {
  similarRequestNo += 1
  selected.value = null
  similarPool.value = []
  similarFailed.value = false
  similarLoading.value = false
}

onMounted(() => {
  dict.load()
})
</script>

<template>
  <div class="explore">
    <SiteHeader />

    <!-- 검색 -->
    <section class="hero">
      <div class="hero-bg" aria-hidden="true" />
      <div class="hero-inner">
        <h1 class="hero-title">당신의 일상에,<br />특별한 향을 더하세요.</h1>
        <p class="hero-sub">SCENT STUDIO</p>
        <div class="hero-search">
          <PerfumeSearchBox
            ref="searchRef"
            :results="searchResults"
            :loading="searching"
            :failed="searchFailed"
            :searched-as="searchedAs"
            @search="onSearch"
            @select="select"
          />
        </div>
        <p class="popular">
          <span class="popular-label">인기 검색어</span>
          <button
            v-for="k in POPULAR_KEYWORDS"
            :key="k"
            type="button"
            class="popular-item"
            @click="searchRef.setKeyword(k)"
          >
            #{{ k }}
          </button>
        </p>
      </div>
    </section>

    <main class="container">
      <p v-if="dict.error" class="notice" role="status">{{ dict.error }}</p>

      <!-- 검색한 향수 분석 -->
      <template v-if="selected">
        <section ref="analysisRef" class="analysis" aria-labelledby="analysis-title">
          <header class="analysis-head">
            <div class="analysis-bottle">
              <BottleIllust
                :color="selectedProfile.mainColor"
                :label="selected.brand"
                :image="selected.image"
              />
            </div>
            <div class="analysis-info">
              <p class="eyebrow">검색한 향수</p>
              <h2 id="analysis-title" class="analysis-name">{{ selected.name }}</h2>
              <p class="analysis-meta">
                {{ selected.brand }}
                <template v-if="selected.year"> · {{ selected.year }}</template>
                <template v-if="selected.concentration"> · {{ selected.concentration }}</template>
                <template v-if="selected.rating">
                  · <span class="star" aria-hidden="true">★</span> {{ selected.rating }}
                </template>
              </p>
              <div v-if="selectedProfile.mood.headline" class="mood">
                <p class="mood-label">이 향수의 분위기</p>
                <p class="mood-headline">{{ selectedProfile.mood.headline }}</p>
                <ul class="mood-words">
                  <li v-for="w in selectedProfile.mood.words" :key="w">#{{ w }}</li>
                </ul>
              </div>
            </div>
            <button type="button" class="close-btn" aria-label="분석 닫기" @click="clearSelected">
              <AppIcon name="close" :size="22" />
            </button>
          </header>

          <div class="analysis-grid">
            <section class="panel">
              <h3 class="panel-title">노트 피라미드</h3>
              <NotePyramid :notes-by-type="selectedProfile.notesByType" />
            </section>
            <section class="panel">
              <h3 class="panel-title">계열 비중(추정)</h3>
              <FamilyComposition
                :segments="selectedProfile.segments"
                :unclassified="selectedProfile.unclassified"
              />
              <h3 class="panel-title accord-title">메인 어코드</h3>
              <AccordBars :accords="selected.accords || []" />
            </section>
          </div>
        </section>

        <section class="section" aria-labelledby="similar-title">
          <div class="section-head">
            <h2 id="similar-title">
              <b>{{ selected.name }}</b> 기준, 같은 향료가 많은 향수
            </h2>
          </div>
          <p v-if="similarLoading" class="empty" role="status">비슷한 향수를 찾고 있어요…</p>
          <p v-else-if="similarFailed" class="empty">비슷한 향수를 불러오지 못했어요.</p>
          <ul v-else-if="similar.length" class="grid">
            <li v-for="r in similar" :key="r.perfume.id">
              <PerfumeCard
                :perfume="r.perfume"
                :profile="r.profile"
                :badge="r.sharedNotes.length ? `같은 향료 ${r.sharedNotes.length}개` : ''"
                :chips="r.sharedNotes.map(noteLabel)"
                @select="select"
              />
            </li>
          </ul>
          <p v-else class="empty">비슷한 향수를 찾지 못했어요.</p>
        </section>
      </template>

      <!-- 필터 -->
      <section class="filters" aria-label="향수 필터">
        <div class="filter-row">
          <span class="filter-label">
            <AppIcon name="menu" :size="16" />
            노트 계열
          </span>
          <div class="chips">
            <button
              v-for="f in dict.families"
              :key="f.familyCode"
              type="button"
              class="chip"
              :class="{ on: selFamilies.includes(f.familyCode) }"
              :aria-pressed="selFamilies.includes(f.familyCode)"
              @click="toggle(selFamilies, f.familyCode)"
            >
              <i :style="{ background: f.hexColorCode }" aria-hidden="true" />{{ f.familyNameKo }}
            </button>
          </div>
        </div>
        <div class="filter-row">
          <span class="filter-label">계절 / 상황</span>
          <div class="chips">
            <button
              v-for="s in SEASON_OPTIONS"
              :key="s.value"
              type="button"
              class="chip"
              :class="{ on: selSeasons.includes(s.value) }"
              :aria-pressed="selSeasons.includes(s.value)"
              @click="toggle(selSeasons, s.value)"
            >
              {{ s.label }}
            </button>
            <span class="chip-divider" aria-hidden="true" />
            <button
              v-for="o in OCCASION_OPTIONS"
              :key="o.value"
              type="button"
              class="chip"
              :class="{ on: selOccasions.includes(o.value) }"
              :aria-pressed="selOccasions.includes(o.value)"
              @click="toggle(selOccasions, o.value)"
            >
              {{ o.label }}
            </button>
          </div>
        </div>
        <button v-if="hasFilter" type="button" class="reset-btn" @click="resetFilters">초기화</button>
      </section>

      <section class="section" aria-labelledby="list-title">
        <div class="section-head">
          <h2 id="list-title">선택한 조건이 많이 겹치는 향수</h2>
          <span v-if="selected && hasFilter" class="count">{{ filtered.length }}개</span>
        </div>
        <p v-if="!selected" class="empty">
          향수를 먼저 검색하면, 비슷한 향수 중에서 이 조건에 맞는 향수를 골라 드려요.
        </p>
        <p v-else-if="similarLoading" class="empty" role="status">비슷한 향수를 찾고 있어요…</p>
        <p v-else-if="!hasFilter" class="empty">
          계열 · 계절 · 상황을 고르면 <b>{{ selected.name }}</b>와 비슷한 향수
          {{ similarPool.length }}개를 많이 겹치는 순으로 보여드려요.
        </p>
        <ul v-else-if="filtered.length" class="grid">
          <li v-for="r in filtered" :key="r.perfume.id">
            <PerfumeCard
              :perfume="r.perfume"
              :profile="r.profile"
              :badge="`${r.matched}/${r.total} 일치`"
              :chips="r.matchedFamilies.map(familyName)"
              :active="selected?.id === r.perfume.id"
              @select="select"
            />
          </li>
        </ul>
        <p v-else class="empty">조건에 맞는 향수가 없어요. 선택을 줄여 보세요.</p>
      </section>
    </main>
  </div>
</template>

<style scoped>
.explore {
  min-height: 100vh;
  background: var(--c-cream);
}

.container {
  width: min(var(--container), 100% - 48px);
  margin-inline: auto;
  padding-bottom: 80px;
}

/* ------------------------------------------------------------------ 검색 히어로 */
.hero {
  position: relative;
  padding: calc(76px + 56px) 24px 48px;
  text-align: center;
  color: #fbf4ea;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(28, 18, 11, 0.55), rgba(28, 18, 11, 0.5)),
    url('../assets/images/home/hero-atelier.webp') 74% 40% / cover;
}

.hero-inner {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 680px;
  margin: 0 auto;
}

.hero-title {
  font-family: var(--f-serif-kr);
  font-weight: 500;
  font-size: clamp(28px, 2.6vw, 38px);
  line-height: 1.45;
}

.hero-sub {
  margin-top: 10px;
  font-family: var(--f-serif-en);
  font-size: 12px;
  letter-spacing: 0.4em;
  color: var(--c-gold-light);
}

.hero-search {
  width: 100%;
  margin-top: 28px;
}

.popular {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px 14px;
  margin-top: 16px;
  font-size: 14px;
}

.popular-label {
  opacity: 0.8;
}

.popular-item {
  color: #fbf4ea;
}

.popular-item:hover {
  text-decoration: underline;
}

.notice {
  margin-top: 20px;
  padding: 12px 16px;
  border-radius: 10px;
  background: #fbeee0;
  font-size: 14px;
  color: #8a4b12;
}

/* ------------------------------------------------------------------ 분석 */
.analysis {
  margin-top: 32px;
  padding: 28px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(91, 58, 34, 0.07);
  scroll-margin-top: 96px;
}

.analysis-head {
  position: relative;
  display: flex;
  gap: 28px;
  align-items: center;
}

.analysis-bottle {
  flex: none;
  width: 180px;
  aspect-ratio: 1 / 1.05;
  overflow: hidden;
  border-radius: 16px;
}

.analysis-info {
  min-width: 0;
}

.eyebrow {
  font-size: 13px;
  font-weight: 700;
  color: var(--c-brown-600);
}

.analysis-name {
  margin-top: 4px;
  font-family: var(--f-serif-kr);
  font-size: 30px;
  font-weight: 500;
}

.analysis-meta {
  margin-top: 6px;
  font-size: 14px;
  color: var(--c-muted);
}

.star {
  color: #d9a441;
}

.mood {
  margin-top: 18px;
}

.mood-label {
  font-size: 13px;
  color: var(--c-muted);
}

.mood-headline {
  margin-top: 2px;
  font-family: var(--f-serif-kr);
  font-size: 22px;
  color: var(--c-brown-800);
}

.mood-words {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.mood-words li {
  padding: 5px 12px;
  border-radius: 999px;
  background: var(--c-tag-bg);
  font-size: 13px;
  color: var(--c-tag-text);
}

.close-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: var(--c-muted);
}

.close-btn:hover {
  background: var(--c-cream);
  color: var(--c-text);
}

.analysis-grid {
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  gap: 28px;
  margin-top: 28px;
}

.panel-title {
  margin-bottom: 14px;
  font-size: 16px;
  font-weight: 700;
}

.accord-title {
  margin-top: 28px;
}

/* ------------------------------------------------------------------ 섹션 · 카드 목록 */
.section {
  margin-top: 40px;
}

.section-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 16px;
}

.section-head h2 {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.section-head h2 b {
  color: var(--c-brown-600);
}

.count {
  font-size: 14px;
  color: var(--c-muted);
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.empty {
  padding: 32px;
  border-radius: 14px;
  background: #fff;
  text-align: center;
  font-size: 14px;
  color: var(--c-muted);
}

/* ------------------------------------------------------------------ 필터 */
.filters {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 40px;
  padding: 20px 24px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 6px 20px rgba(91, 58, 34, 0.05);
}

.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.filter-label {
  display: inline-flex;
  flex: none;
  align-items: center;
  gap: 6px;
  width: 96px;
  padding-top: 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--c-muted);
}

.chips {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding-right: 80px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 38px;
  padding: 0 16px;
  border: 1px solid #e6dccf;
  border-radius: 999px;
  background: #fff;
  font-size: 14px;
  white-space: nowrap;
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease,
    color 0.15s ease;
}

.chip:hover {
  border-color: var(--c-brown-500);
}

.chip i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  box-shadow: 0 0 0 1px rgba(58, 36, 22, 0.15);
}

.chip.on {
  border-color: var(--c-brown-700);
  background: var(--c-brown-700);
  color: #fff;
}

.chip.on i {
  box-shadow: 0 0 0 2px #fff;
}

.chip-divider {
  width: 1px;
  height: 22px;
  margin: 0 4px;
  background: #e6dccf;
}

.reset-btn {
  position: absolute;
  top: 24px;
  right: 24px;
  font-size: 14px;
  color: var(--c-muted);
  text-decoration: underline;
}

/* ------------------------------------------------------------------ 반응형 */
@media (max-width: 1180px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1023px) {
  .hero {
    padding-top: 48px;
  }

  .analysis-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .container {
    width: calc(100% - 32px);
  }

  .hero {
    padding: 36px 16px 32px;
  }

  .hero-title {
    font-size: 24px;
  }

  .popular {
    font-size: 13px;
  }

  .analysis {
    margin-top: 20px;
    padding: 20px 16px;
  }

  .analysis-head {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .analysis-bottle {
    width: 120px;
  }

  .analysis-name {
    font-size: 24px;
  }

  .mood-headline {
    font-size: 19px;
  }

  .close-btn {
    top: -6px;
    right: -6px;
  }

  .grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .section-head h2 {
    font-size: 17px;
  }

  /* 필터 칩: 가로 스크롤 */
  .filters {
    margin-top: 24px;
    padding: 16px;
    margin-inline: -16px;
    border-radius: 0;
  }

  .filter-row {
    flex-direction: column;
    gap: 8px;
  }

  .filter-label {
    width: auto;
    padding-top: 0;
  }

  .chips {
    flex-wrap: nowrap;
    width: calc(100% + 32px);
    margin-inline: -16px;
    padding: 0 16px 4px;
    overflow-x: auto;
    scrollbar-width: none;
  }

  .chips::-webkit-scrollbar {
    display: none;
  }

  .chip {
    height: 36px;
    padding: 0 14px;
  }

  .reset-btn {
    top: 16px;
    right: 16px;
  }
}
</style>
