<script setup>
import { NOTE_TYPES, UNCLASSIFIED_COLOR } from '@/utils/scentProfile'

defineProps({
  /** buildProfile().notesByType */
  notesByType: { type: Object, required: true }
})
</script>

<template>
  <div class="pyramid">
    <section v-for="(type, i) in NOTE_TYPES" :key="type.key" class="tier" :class="`tier-${i}`">
      <header class="tier-head">
        <span class="tier-label">{{ type.label }}</span>
        <span class="tier-desc">{{ type.desc }}</span>
      </header>
      <ul class="notes">
        <li
          v-for="note in notesByType[type.key]"
          :key="note.nameEn"
          class="note"
          :style="{ '--dot': note.family?.hexColorCode || UNCLASSIFIED_COLOR }"
          :title="`${note.nameEn} · ${note.family ? note.family.familyNameKo : '계열 미분류'}`"
        >
          <span class="dot" aria-hidden="true" />
          <span class="note-name">{{ note.nameKo || note.nameEn }}</span>
          <span class="note-family">{{ note.family ? note.family.familyNameKo : '미분류' }}</span>
        </li>
        <li v-if="!notesByType[type.key].length" class="empty">정보 없음</li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.pyramid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 위로 갈수록 좁아지는 피라미드 느낌 */
.tier {
  display: grid;
  grid-template-columns: 92px 1fr;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--c-cream);
}

.tier-0 {
  margin-inline: 8%;
}

.tier-1 {
  margin-inline: 4%;
}

.tier-head {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tier-label {
  font-family: var(--f-serif-en);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.14em;
  color: var(--c-brown-700);
}

.tier-desc {
  font-size: 11px;
  color: var(--c-muted);
}

.notes {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.note {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px 6px 8px;
  border: 1px solid #ece2d5;
  border-radius: 999px;
  background: #fff;
  font-size: 14px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--dot);
  box-shadow: 0 0 0 1px rgba(58, 36, 22, 0.15);
}

.note-name {
  font-weight: 700;
}

.note-family {
  font-size: 12px;
  color: var(--c-muted);
}

.empty {
  font-size: 13px;
  color: var(--c-muted);
}

@media (max-width: 767px) {
  .tier {
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 12px 14px;
  }

  .tier-0,
  .tier-1 {
    margin-inline: 0;
  }

  .tier-head {
    flex-direction: row;
    align-items: baseline;
    gap: 8px;
  }
}
</style>
