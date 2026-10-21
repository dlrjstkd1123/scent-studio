<script setup>
import { computed } from 'vue'

/**
 * 향수 이미지 대신 쓰는 병 일러스트. 향의 대표 계열 색으로 향수 액체를 칠한다.
 * (API 연동 후 실제 이미지가 있으면 이미지로 대체)
 */
const props = defineProps({
  color: { type: String, default: '#C9BFB4' },
  label: { type: String, default: '' },
  image: { type: String, default: '' }
})

const initial = computed(() =>
  (props.label || '')
    .replace(/[^A-Za-z가-힣]/g, '')
    .slice(0, 1)
    .toUpperCase()
)
</script>

<template>
  <div class="bottle" :style="{ '--tint': color }">
    <img v-if="image" :src="image" alt="" loading="lazy" />
    <svg v-else viewBox="0 0 120 150" aria-hidden="true">
      <!-- 캡 -->
      <rect x="47" y="10" width="26" height="24" rx="3" fill="#2a1b11" />
      <rect x="51" y="34" width="18" height="8" fill="#b89a6a" />
      <!-- 병 -->
      <rect
        x="24"
        y="42"
        width="72"
        height="92"
        rx="10"
        fill="#ffffff"
        fill-opacity="0.55"
        stroke="#ffffff"
        stroke-opacity="0.9"
        stroke-width="2"
      />
      <!-- 향수 -->
      <rect x="28" y="58" width="64" height="72" rx="7" fill="var(--tint)" fill-opacity="0.72" />
      <!-- 라벨 -->
      <rect x="41" y="74" width="38" height="34" rx="2" fill="#fffdf8" fill-opacity="0.92" />
      <text
        x="60"
        y="97"
        text-anchor="middle"
        font-family="Cormorant Garamond, serif"
        font-size="16"
        fill="#3a2416"
      >
        {{ initial }}
      </text>
      <!-- 반사광 -->
      <rect x="31" y="48" width="6" height="78" rx="3" fill="#ffffff" fill-opacity="0.45" />
    </svg>
  </div>
</template>

<style scoped>
.bottle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 50% 40%, rgba(255, 255, 255, 0.75), transparent 70%),
    color-mix(in srgb, var(--tint) 22%, #f6efe6);
}

.bottle svg {
  width: 62%;
  height: auto;
  max-height: 86%;
  filter: drop-shadow(0 6px 10px rgba(58, 36, 22, 0.18));
}

.bottle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
