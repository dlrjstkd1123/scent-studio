<script setup>
defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 18 }
})

// 선 아이콘 (stroke = currentColor)
const PATHS = {
  arrow: 'M4 12h15M13 6l6 6-6 6',
  search: 'M10.5 17a6.5 6.5 0 1 0 0-13 6.5 6.5 0 0 0 0 13ZM20 20l-4.8-4.8',
  bag: 'M6 8h12l-1 12H7L6 8ZM9 8V6.5a3 3 0 0 1 6 0V8',
  menu: 'M4 7h16M4 12h16M4 17h16',
  close: 'M6 6l12 12M18 6 6 18',
  'chevron-down': 'M6 9l6 6 6-6'
}
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.6"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    focusable="false"
  >
    <path :d="PATHS[name]" />
  </svg>
</template>
