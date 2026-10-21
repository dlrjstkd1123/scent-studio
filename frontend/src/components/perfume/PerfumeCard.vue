<script setup>
import BottleIllust from './BottleIllust.vue'

defineProps({
  perfume: { type: Object, required: true },
  /** buildProfile() 결과 */
  profile: { type: Object, required: true },
  /** 좌상단 배지 (예: "같은 향료 3개") */
  badge: { type: String, default: '' },
  /** 배지 아래 겹치는 항목 칩 */
  chips: { type: Array, default: () => [] },
  active: { type: Boolean, default: false }
})

defineEmits(['select'])
</script>

<template>
  <article
    class="card"
    :class="{ active }"
    role="button"
    tabindex="0"
    :aria-label="`${perfume.brand} ${perfume.name} 분석 보기`"
    :aria-pressed="active"
    @click="$emit('select', perfume)"
    @keydown.enter.prevent="$emit('select', perfume)"
    @keydown.space.prevent="$emit('select', perfume)"
  >
    <div class="thumb">
      <BottleIllust :color="profile.mainColor" :label="perfume.brand" :image="perfume.image" />
      <span v-if="badge" class="badge">{{ badge }}</span>
    </div>
    <div class="body">
      <p class="brand">{{ perfume.brand }}</p>
      <h3 class="name">{{ perfume.name }}</h3>
      <p class="families">
        <span
          v-for="f in profile.families.slice(0, 3)"
          :key="f.family.familyCode"
          class="family"
        >
          <i :style="{ background: f.family.hexColorCode }" aria-hidden="true" />
          {{ f.family.familyNameKo }}
        </span>
      </p>
      <ul v-if="chips.length" class="chips">
        <li v-for="c in chips.slice(0, 4)" :key="c">{{ c }}</li>
        <li v-if="chips.length > 4">+{{ chips.length - 4 }}</li>
      </ul>
      <p v-if="perfume.rating" class="rating">
        <span aria-hidden="true">★</span> {{ perfume.rating }}
      </p>
    </div>
  </article>
</template>

<style scoped>
.card {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border: 1px solid #efe6da;
  border-radius: 16px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(91, 58, 34, 0.1);
}

.card.active {
  border-color: var(--c-brown-600);
  box-shadow: 0 0 0 2px var(--c-brown-600);
}

.thumb {
  position: relative;
  aspect-ratio: 1 / 0.9;
}

.badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--c-brown-800);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 16px 16px;
}

.brand {
  font-size: 12px;
  color: var(--c-muted);
}

.name {
  font-size: 16px;
  font-weight: 700;
  line-height: 1.35;
}

.families {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 10px;
  font-size: 13px;
  color: var(--c-muted);
}

.family {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.family i {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  box-shadow: 0 0 0 1px rgba(58, 36, 22, 0.15);
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}

.chips li {
  padding: 3px 9px;
  border-radius: 999px;
  background: var(--c-tag-bg);
  font-size: 12px;
  color: var(--c-tag-text);
}

.rating {
  margin-top: 6px;
  font-size: 13px;
  font-weight: 700;
}

.rating span[aria-hidden] {
  color: #d9a441;
}

@media (max-width: 767px) {
  .body {
    padding: 12px;
  }

  .name {
    font-size: 15px;
  }
}
</style>
