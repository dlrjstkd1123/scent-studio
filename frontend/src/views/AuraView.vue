<script setup>
import { computed, onMounted, ref } from 'vue'
import ScentAura from '@/components/aura/ScentAura.vue'
import SiteHeader from '@/components/layout/SiteHeader.vue'
import { useNoteDictionaryStore } from '@/stores/noteDictionary'

/**
 * 향 아우라 (테스트 화면).
 * 계열을 고르고 비중을 조절하면, 그 조합의 연기 불꽃 아우라를 실시간으로 보여준다.
 * 색은 DB 의 향 계열 색(scent_family.hex_color_code)을 쓴다.
 */

/** 한 번에 섞을 수 있는 계열 수 (아우라가 상위 4개까지 표현) */
const MAX_PICK = 4
/** 새로 고른 향의 처음 값 (0~100). 100 이면 고르자마자 다른 향과 같은 비율로 들어간다 */
const DEFAULT_WEIGHT = 100

const PRESETS = [
  { name: '우디 · 머스크 · 플로럴', mix: { WOODY: 40, MUSK: 35, FLORAL: 25 } },
  { name: '시트러스 · 그린 · 아쿠아틱', mix: { CITRUS: 45, GREEN: 30, AQUATIC: 25 } },
  { name: '앰버 · 구르망 · 스파이시', mix: { AMBER: 45, GOURMAND: 35, SPICY: 20 } },
  { name: '플로럴 · 파우더리 · 머스크', mix: { FLORAL: 45, POWDERY: 30, MUSK: 25 } }
]

const dict = useNoteDictionaryStore()

/** 계열 코드 → 비중 (0~100, 화면에서 합이 100 이 되도록 다시 계산) */
/** 처음에는 아무것도 고르지 않은 상태 (별빛 배경만 보인다) */
const mix = ref({})

const picked = computed(() => Object.keys(mix.value))

/** 아우라 · 범례 · 비중 목록 (고른 순서 그대로, % 는 합 100 기준) */
/**
 * 슬라이더 값(0~100, 향마다 따로) → 비율(%).
 * 예: 100 · 100 · 100 → 33 · 33 · 34, 50 · 100 · 100 → 20 · 40 · 40. 합이 정확히 100 이 되게 반올림한다
 */
const toShares = (values) => {
  const total = values.reduce((a, b) => a + b, 0)
  if (!total) return values.map(() => 0)
  const raw = values.map((v) => (v / total) * 100)
  const shares = raw.map(Math.floor)
  let rest = 100 - shares.reduce((a, b) => a + b, 0)
  raw
    .map((r, i) => [r - shares[i], i])
    .sort((a, b) => b[0] - a[0])
    .forEach(([, i]) => {
      if (rest > 0) {
        shares[i] += 1
        rest -= 1
      }
    })
  return shares
}

const shares = computed(() => toShares(picked.value.map((code) => mix.value[code])))

const auraFamilies = computed(() =>
  picked.value
    .map((code, i) => {
      const family = dict.familyByCode.get(code)
      return {
        code,
        name: family?.familyNameKo || code,
        color: family?.hexColorCode || '#C9A46A',
        value: mix.value[code],
        pct: shares.value[i]
      }
    })
)

const auraLabel = computed(() =>
  `향 아우라: ${auraFamilies.value.map((f) => `${f.name} ${f.pct}%`).join(', ')}`
)

const activePreset = computed(() =>
  PRESETS.findIndex((p) => {
    const keys = Object.keys(p.mix)
    const sameKeys = keys.length === picked.value.length && keys.every((k) => k in mix.value)
    return sameKeys && keys.every((k) => p.mix[k] === mix.value[k])
  })
)

const applyPreset = (preset) => {
  mix.value = { ...preset.mix }
}

const toggle = (code) => {
  const next = { ...mix.value }
  if (code in next) {
    delete next[code]
  } else {
    if (picked.value.length >= MAX_PICK) return
    next[code] = DEFAULT_WEIGHT
  }
  mix.value = next
}

const setWeight = (code, value) => {
  mix.value = { ...mix.value, [code]: Number(value) }
}

const isFull = (code) => !(code in mix.value) && picked.value.length >= MAX_PICK

onMounted(() => {
  dict.load()
})
</script>

<template>
  <div class="aura-page">
    <SiteHeader />

    <main class="container">
      <header class="page-head">
        <p class="eyebrow">SCENT AURA</p>
        <h1 class="title">
          향 아우라
          <span class="badge">테스트</span>
        </h1>
        <p class="desc">향 계열을 고르고 비중을 조절해 보세요. 조합에 따라 연기의 색과 움직임이 달라져요.</p>
      </header>

      <div class="layout">
        <section class="stage" aria-label="아우라 미리보기">
          <ScentAura :families="auraFamilies" :label="auraLabel" />
          <ul class="legend">
            <li v-for="f in auraFamilies" :key="f.code">
              <i :style="{ background: f.color }" aria-hidden="true" />
              {{ f.name }} <b>{{ f.pct }}%</b>
            </li>
          </ul>
        </section>

        <section class="panel" aria-label="아우라 조합">
          <h2 class="panel-title">추천 조합</h2>
          <div class="presets">
            <button
              v-for="(p, i) in PRESETS"
              :key="p.name"
              type="button"
              class="preset"
              :class="{ on: activePreset === i }"
              :aria-pressed="activePreset === i"
              @click="applyPreset(p)"
            >
              {{ p.name }}
            </button>
          </div>

          <h2 class="panel-title">
            향 계열
            <small>최대 {{ MAX_PICK }}개</small>
          </h2>
          <p v-if="dict.error" class="notice" role="status">{{ dict.error }}</p>
          <div class="chips">
            <button
              v-for="f in dict.families"
              :key="f.familyCode"
              type="button"
              class="chip"
              :class="{ on: picked.includes(f.familyCode) }"
              :aria-pressed="picked.includes(f.familyCode)"
              :disabled="isFull(f.familyCode)"
              @click="toggle(f.familyCode)"
            >
              <i :style="{ background: f.hexColorCode }" aria-hidden="true" />{{ f.familyNameKo }}
            </button>
          </div>

          <h2 class="panel-title">비중</h2>
          <p v-if="!auraFamilies.length" class="empty">향 계열을 골라 보세요. 고른 향의 연기가 피어올라요.</p>
          <ul v-else class="sliders">
            <li v-for="f in auraFamilies" :key="f.code">
              <label :for="`w-${f.code}`">
                <i :style="{ background: f.color }" aria-hidden="true" />
                {{ f.name }}
                <span class="value">{{ f.value }}</span>
                <b class="share" :title="`전체 중 ${f.pct}%`">{{ f.pct }}%</b>
              </label>
              <input
                :id="`w-${f.code}`"
                type="range"
                min="0"
                max="100"
                step="5"
                :value="mix[f.code]"
                @input="setWeight(f.code, $event.target.value)"
              />
            </li>
          </ul>
          <p class="hint">
            계열마다 연기의 성격이 달라요. 시트러스는 빠르고 가볍게, 우디 · 레더는 느리고 묵직하게,
            머스크 · 파우더리는 부드럽게 퍼져요.
          </p>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.aura-page {
  min-height: 100vh;
  background: var(--c-brown-950);
  color: #f3e9dc;
}

.container {
  width: min(var(--container), 100% - 48px);
  margin-inline: auto;
  padding: calc(76px + 48px) 0 80px;
}

.page-head {
  text-align: center;
}

.eyebrow {
  font-family: var(--f-serif-en);
  font-size: 12px;
  letter-spacing: 0.4em;
  color: var(--c-gold);
}

.title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  font-family: var(--f-serif-kr);
  font-weight: 500;
  font-size: clamp(28px, 2.6vw, 38px);
}

.badge {
  padding: 3px 10px;
  border: 1px solid rgba(201, 164, 106, 0.6);
  border-radius: 999px;
  font-family: var(--f-sans);
  font-size: 12px;
  font-weight: 700;
  color: var(--c-gold);
}

.desc {
  margin-top: 10px;
  font-size: 15px;
  color: rgba(243, 233, 220, 0.7);
}

.layout {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 1fr);
  gap: 28px;
  margin-top: 36px;
  align-items: start;
}

/* ------------------------------------------------------------------ 아우라 */
.stage {
  position: relative;
  height: min(72vh, 680px);
  min-height: 460px;
  overflow: hidden;
  border: 1px solid rgba(201, 164, 106, 0.18);
  border-radius: 20px;
}

.legend {
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: 16px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px 14px;
  font-size: 13px;
  color: rgba(243, 233, 220, 0.85);
  pointer-events: none;
}

.legend li,
.sliders label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend i,
.sliders i,
.chip i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.15);
}

/* ------------------------------------------------------------------ 조합 패널 */
.panel {
  padding: 24px;
  border: 1px solid rgba(201, 164, 106, 0.18);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.03);
}

.panel-title {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 22px 0 12px;
  font-size: 15px;
  font-weight: 700;
}

.panel-title:first-child {
  margin-top: 0;
}

.panel-title small {
  font-size: 12px;
  font-weight: 400;
  color: rgba(243, 233, 220, 0.55);
}

.presets,
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preset,
.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 14px;
  border: 1px solid rgba(243, 233, 220, 0.18);
  border-radius: 999px;
  background: transparent;
  color: rgba(243, 233, 220, 0.85);
  font-size: 13px;
  transition:
    background 0.2s ease,
    border-color 0.2s ease;
}

.preset:hover,
.chip:hover:not(:disabled) {
  border-color: var(--c-gold);
}

.preset.on,
.chip.on {
  border-color: var(--c-gold);
  background: rgba(201, 164, 106, 0.18);
  color: #fff;
}

.chip:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.sliders {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sliders label {
  width: 100%;
  font-size: 14px;
}

.sliders .value {
  margin-left: auto;
  font-variant-numeric: tabular-nums;
  color: rgba(243, 233, 220, 0.6);
}

.sliders .share {
  min-width: 48px;
  margin-left: 10px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(201, 164, 106, 0.16);
  font-size: 13px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}

.sliders label b {
  margin-left: auto;
  color: var(--c-gold);
}

.sliders input {
  width: 100%;
  margin-top: 6px;
  accent-color: var(--c-gold);
}

.empty {
  font-size: 13px;
  color: rgba(243, 233, 220, 0.55);
}

.hint,
.notice {
  margin-top: 18px;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(243, 233, 220, 0.55);
}

@media (max-width: 1023px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .stage {
    height: 62vh;
    min-height: 420px;
  }
}

@media (max-width: 767px) {
  .container {
    width: calc(100% - 32px);
    padding-top: calc(60px + 32px);
  }

  .desc {
    font-size: 14px;
  }

  .stage {
    height: 58vh;
    min-height: 380px;
    border-radius: 16px;
  }

  .panel {
    padding: 18px;
  }
}
</style>
