<script setup>
import { ref } from 'vue'

/**
 * 계열 비중(추정): 노트 위치 · 어코드 강도로 매긴 점수의 비율 → 100% 가로 막대. 실제 배합량이 아니다.
 * 계열 색은 관리자가 정하는 값이라 색만으로 구분하지 않도록 이름·% 를 항상 함께 표시한다.
 * 비율은 노트 + 메인 어코드로 계산한다 (scentProfile.js).
 */
defineProps({
  /** buildProfile().segments */
  segments: { type: Array, required: true },
  unclassified: { type: Number, default: 0 }
})

const hovered = ref(null)

/** 툴팁: 이 계열에 들어간 노트 · 어코드 수 */
const sourceText = (s) => {
  const parts = []
  if (s.noteCount) parts.push(`노트 ${s.noteCount}개`)
  if (s.accordCount) parts.push(`어코드 ${s.accordCount}개`)
  return parts.join(' · ')
}
</script>

<template>
  <div class="composition">
    <div
      v-if="segments.length"
      class="bar"
      role="img"
      :aria-label="segments.map((s) => `${s.label} ${s.pct}%`).join(', ')"
    >
      <div
        v-for="s in segments"
        :key="s.code"
        class="segment"
        :class="{ dim: hovered && hovered !== s.code }"
        :style="{ flexGrow: s.pct, background: s.color }"
        tabindex="0"
        @mouseenter="hovered = s.code"
        @mouseleave="hovered = null"
        @focus="hovered = s.code"
        @blur="hovered = null"
      >
        <span v-if="s.pct >= 16" class="segment-label">{{ s.pct }}%</span>
        <span v-if="hovered === s.code" class="tooltip" role="tooltip">
          <b>{{ s.label }}</b> {{ s.pct }}%
          <small>
            {{ sourceText(s) }}
            <template v-if="s.members"> · {{ s.members.join(', ') }}</template>
          </small>
        </span>
      </div>
    </div>
    <p v-else class="empty">계열을 알 수 있는 노트 · 어코드가 없어 비율을 계산할 수 없어요.</p>

    <!-- 범례 겸 표 (값을 글자로 확인) -->
    <ul class="legend">
      <li
        v-for="s in segments"
        :key="s.code"
        :class="{ active: hovered === s.code }"
        @mouseenter="hovered = s.code"
        @mouseleave="hovered = null"
      >
        <span class="swatch" :style="{ background: s.color }" aria-hidden="true" />
        <span class="legend-name">{{ s.label }}</span>
        <span class="legend-pct">{{ s.pct }}%</span>
      </li>
    </ul>
    <p v-if="unclassified" class="note">
      * 계열이 정해지지 않은 노트 {{ unclassified }}개는 비율에서 제외했어요.
    </p>
  </div>
</template>

<style scoped>
.bar {
  display: flex;
  gap: 2px;
  height: 36px;
}

.segment {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 10px;
  border-radius: 4px;
  box-shadow: inset 0 0 0 1px rgba(58, 36, 22, 0.12);
  cursor: default;
  outline: none;
  transition: opacity 0.15s ease;
}

.segment:focus-visible {
  box-shadow: 0 0 0 2px var(--c-brown-600);
}

.segment.dim {
  opacity: 0.45;
}

.segment-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--c-text);
  background: rgba(255, 253, 250, 0.82);
  padding: 1px 6px;
  border-radius: 999px;
}

.tooltip {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  z-index: 5;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 120px;
  padding: 8px 10px;
  transform: translateX(-50%);
  border-radius: 8px;
  background: var(--c-brown-900);
  color: #fff;
  font-size: 13px;
  white-space: nowrap;
  pointer-events: none;
}

.tooltip small {
  font-size: 11px;
  opacity: 0.75;
}

.legend {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 6px 14px;
  margin-top: 14px;
}

.legend li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 6px;
  border-radius: 6px;
  font-size: 14px;
}

.legend li.active {
  background: var(--c-cream);
}

.swatch {
  flex: none;
  width: 12px;
  height: 12px;
  border-radius: 3px;
  box-shadow: inset 0 0 0 1px rgba(58, 36, 22, 0.18);
}

.legend-name {
  flex: 1;
  color: var(--c-text);
}

.legend-pct {
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--c-text);
}

.empty,
.note {
  font-size: 12px;
  color: var(--c-muted);
}

.note {
  margin-top: 8px;
}
</style>
