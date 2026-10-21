<script setup>
import { ACCORD_LEVELS } from '@/utils/fragella'

/**
 * 메인 어코드 강도. Fragella 는 %가 아니라 단어(Dominant/Prominent/Moderate/Subtle)로 주므로 4단계 막대로 표시.
 */
defineProps({
  /** [{ name, level: 'Dominant' | 'Prominent' | 'Moderate' | 'Subtle' }] */
  accords: { type: Array, default: () => [] }
})
</script>

<template>
  <ul v-if="accords.length" class="accords">
    <li v-for="a in accords" :key="a.name" class="accord">
      <span class="accord-name">{{ a.name }}</span>
      <span class="steps" aria-hidden="true">
        <span
          v-for="i in 4"
          :key="i"
          class="step"
          :class="{ on: i <= (ACCORD_LEVELS[a.level]?.step || 0) }"
        />
      </span>
      <span class="accord-level">{{ ACCORD_LEVELS[a.level]?.label || '-' }}</span>
    </li>
  </ul>
  <p v-else class="empty">어코드 정보가 없어요.</p>
</template>

<style scoped>
.accords {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.accord {
  display: grid;
  grid-template-columns: 110px 1fr 64px;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.accord-name {
  text-transform: capitalize;
  color: var(--c-text);
}

.steps {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2px;
  height: 10px;
}

.step {
  border-radius: 2px;
  background: #efe6da;
}

.step:first-child {
  border-radius: 4px 2px 2px 4px;
}

.step:last-child {
  border-radius: 2px 4px 4px 2px;
}

.step.on {
  background: var(--c-brown-500);
}

.accord-level {
  text-align: right;
  font-weight: 700;
}

.empty {
  font-size: 13px;
  color: var(--c-muted);
}
</style>
