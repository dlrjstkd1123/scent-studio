<script setup>
import { useToastStore } from '@/stores/toast'

/**
 * 토스트 안내. App.vue 에 한 번만 둔다. 문구는 useToastStore().show() 로 띄운다.
 */
const toast = useToastStore()
</script>

<template>
  <div class="toast-area" role="status" aria-live="polite">
    <Transition name="toast">
      <p v-if="toast.message" class="toast">{{ toast.message }}</p>
    </Transition>
  </div>
</template>

<style scoped>
.toast-area {
  position: fixed;
  left: 50%;
  bottom: 32px;
  z-index: 1000;
  width: max-content;
  max-width: calc(100% - 32px);
  transform: translateX(-50%);
  pointer-events: none;
}

.toast {
  padding: 12px 20px;
  border-radius: 999px;
  background: var(--c-brown-900);
  box-shadow: 0 10px 30px rgba(28, 18, 11, 0.25);
  color: #fff;
  font-size: 14px;
  line-height: 1.5;
  text-align: center;
}

.toast-enter-active,
.toast-leave-active {
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

@media (max-width: 767px) {
  .toast-area {
    bottom: 20px;
  }

  .toast {
    border-radius: 14px;
    font-size: 13px;
  }
}
</style>
