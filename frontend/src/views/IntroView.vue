<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'
import bgOpen from '@/assets/images/intro/storefront-open.webp'
import panelLeft from '@/assets/images/intro/door-panel-left.webp'
import panelRight from '@/assets/images/intro/door-panel-right.webp'
import panelLeftBack from '@/assets/images/intro/door-panel-left-back.webp'
import panelRightBack from '@/assets/images/intro/door-panel-right-back.webp'

const router = useRouter()

/**
 * 입장 단계
 * 0: 문 닫힘 → 1: 문 열림 → 2: 줌 → 3: 홈 전환
 */
const stage = ref(0)
const ready = ref(false)

// 줌(3.6s)이 끝나기 전, 아직 걸어 들어가는 도중에 커튼을 덮고 홈으로 넘어간다
const TIMELINE = { zoom: 1900, leave: 3700, route: 4700 }

const timers = []
const later = (ms, fn) => {
  timers.push(setTimeout(fn, ms))
}

const goHome = () => {
  router.push({ name: 'home', state: { fromIntro: true } })
}

const enter = () => {
  if (stage.value > 0) return

  const noMotion = window
    .matchMedia('(prefers-reduced-motion: reduce)').matches
  if (noMotion) {
    stage.value = 3
    later(500, goHome)
    return
  }

  stage.value = 1
  later(TIMELINE.zoom, () => (stage.value = 2))
  later(TIMELINE.leave, () => (stage.value = 3))
  later(TIMELINE.route, goHome)
}

const onWheel = (e) => {
  if (e.deltaY > 20) enter()
}
let touchStartY = 0
const onTouchStart = (e) => {
  touchStartY = e.touches[0].clientY
}
const onTouchEnd = (e) => {
  const dy = touchStartY - e.changedTouches[0].clientY
  if (dy > 40) enter()
}

let pendingImages = 3
const onImgLoad = () => {
  pendingImages -= 1
  if (pendingImages === 0) ready.value = true
}

onMounted(() => later(2000, () => (ready.value = true)))
onBeforeUnmount(() => timers.forEach(clearTimeout))
</script>

<template>
  <main
    class="intro"
    :class="{
      'is-ready': ready,
      'is-open': stage >= 1,
      'is-inside': stage >= 2,
      'is-leaving': stage >= 3
    }"
    @wheel.passive="onWheel"
    @touchstart.passive="onTouchStart"
    @touchend.passive="onTouchEnd"
  >
    <div class="scene">
      <div class="scene-inner">
        <img
          class="scene-bg"
          :src="bgOpen"
          alt="SCENT STUDIO 매장"
          @load="onImgLoad"
        />

        <div class="doors">
          <div class="door door-left">
            <img
              :src="panelLeft"
              alt=""
              @load="onImgLoad"
            />
            <img
              class="door-back"
              :src="panelLeftBack"
              alt=""
            />
            <span class="door-edge" aria-hidden="true" />
          </div>
          <div class="door door-right">
            <img
              :src="panelRight"
              alt=""
              @load="onImgLoad"
            />
            <img
              class="door-back"
              :src="panelRightBack"
              alt=""
            />
            <span class="door-edge" aria-hidden="true" />
          </div>
        </div>
      </div>
    </div>

    <div class="vignette" aria-hidden="true" />

    <section class="intro-copy">
      <p class="copy-lead">
        향이 머무는 공간,<br />
        당신의 이야기가 시작됩니다.
      </p>
      <h1 class="copy-title">SCENT STUDIO</h1>
      <p class="copy-sub">FIND YOUR OWN SCENT</p>
      <button
        type="button"
        class="enter-btn"
        @click="enter"
      >
        들어가기
        <AppIcon name="arrow" :size="18" />
      </button>
    </section>

    <button
      type="button"
      class="scroll-hint"
      aria-label="들어가기"
      @click="enter"
    >
      SCROLL DOWN
      <AppIcon name="chevron-down" :size="16" />
    </button>

    <div class="curtain" aria-hidden="true" />
  </main>
</template>

<style scoped>
/* ------------------------------------------- 기본 */
.intro {
  position: fixed;
  inset: 0;
  overflow: hidden;
  background: var(--c-brown-950);
  color: #f4e9da;
  user-select: none;
}

/* ------------------------------------------- 장면 */
.scene {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  opacity: 0;
  transition: opacity 1s ease;
}

.is-ready .scene {
  opacity: 1;
}

.scene-inner {
  flex: 0 0 auto;
  position: relative;
  width: max(100vw, calc(100vh * 1122 / 1402));
  height: max(100vh, calc(100vw * 1402 / 1122));
  transform-origin: 49.9% 44.9%;
  /* 걸음 속도에 맞춰 일정하게 천천히 들어감 */
  transition:
    transform 3.6s cubic-bezier(0.33, 0.08, 0.35, 0.96);
}

.is-inside .scene-inner {
  transform: scale(2.8);
}

/*
 * 걸어 들어가는 듯한 흔들림 (줌 전체 길이에 한 번만 재생)
 * - 좌우 흔들림은 걸음마다, 위아래 들썩임은 발 디딜 때마다(2배 빈도)
 * - 걷기 시작할 땐 작게 → 중간엔 또렷하게 → 도착하며 잦아듦
 */
.is-inside .scene {
  animation: walk-bob 3.6s ease-in-out 1;
}

@keyframes walk-bob {
  0% {
    transform: translate3d(0, 0, 0);
  }
  8% {
    transform: translate3d(0.05%, 0.3%, 0);
  }
  17% {
    transform: translate3d(0, 0.75%, 0);
  }
  25% {
    transform: translate3d(-0.08%, 0.15%, 0);
  }
  33% {
    transform: translate3d(0, 0.85%, 0);
  }
  42% {
    transform: translate3d(0.08%, 0.15%, 0);
  }
  50% {
    transform: translate3d(0, 0.85%, 0);
  }
  58% {
    transform: translate3d(-0.08%, 0.15%, 0);
  }
  67% {
    transform: translate3d(0, 0.8%, 0);
  }
  75% {
    transform: translate3d(0.06%, 0.15%, 0);
  }
  83% {
    transform: translate3d(0, 0.5%, 0);
  }
  92% {
    transform: translate3d(-0.03%, 0.12%, 0);
  }
  100% {
    transform: translate3d(0, 0, 0);
  }
}

.scene-bg {
  display: block;
  width: 100%;
  height: 100%;
}

/* ------------------------------------------- 문 */
.doors {
  position: absolute;
  top: 14.98%;
  left: 34.76%;
  width: 30.30%;
  height: 59.91%;
  perspective: 650px;
}

.door {
  position: absolute;
  top: 0;
  width: 50%;
  height: 100%;
  transform-style: preserve-3d;
  /* 무거운 나무문: 천천히 시작해 길게 감속 */
  transition:
    transform 2.4s cubic-bezier(0.45, 0.05, 0.18, 1);
  will-change: transform;
}

.door img {
  display: block;
  width: 100%;
  height: 100%;
  transition: filter 2.4s ease;
}

/* 문이 열리며 빛에서 멀어질수록 어두워짐 (투명 유리는 그대로) */
.is-open .door img {
  filter: brightness(0.55);
}

/* 문 뒷면(안쪽 면): 두께만큼 뒤에 놓인 어두운 판 */
.door img.door-back {
  position: absolute;
  top: 0;
  left: 0;
  transform: translateZ(-18px);
  filter: brightness(0.45);
  transition: none;
}

.door-left {
  left: 0;
  transform-origin: left center;
}

.door-right {
  right: 0;
  transform-origin: right center;
}

/* 문 두께(옆면): 경첩 쪽은 ::before, 가운데 쪽은 .door-edge */
.door::before {
  content: '';
}

.door::before,
.door-edge {
  position: absolute;
  top: 0;
  width: 18px;
  height: 100%;
}

/* 앞면과 만나는 모서리는 빛을 받아 밝고, 안쪽으로 갈수록 어두워짐 */
/* 밝은 모서리가 왼쪽인 단면 */
.door-left::before,
.door-right .door-edge {
  background:
    linear-gradient(
      90deg,
      rgba(255, 214, 160, 0.3),
      rgba(0, 0, 0, 0) 28%,
      rgba(0, 0, 0, 0.5)
    ),
    linear-gradient(180deg, #5f4322, #3d2812 30%, #4a3318 70%, #2e1c0c);
}

/* 밝은 모서리가 오른쪽인 단면 */
.door-left .door-edge,
.door-right::before {
  background:
    linear-gradient(
      270deg,
      rgba(255, 214, 160, 0.3),
      rgba(0, 0, 0, 0) 28%,
      rgba(0, 0, 0, 0.5)
    ),
    linear-gradient(180deg, #5f4322, #3d2812 30%, #4a3318 70%, #2e1c0c);
}

.door-left::before {
  left: 0;
  transform-origin: left center;
  transform: rotateY(90deg);
}

.door-right::before {
  right: 0;
  transform-origin: right center;
  transform: rotateY(-90deg);
}

.door-left .door-edge {
  right: 0;
  transform-origin: right center;
  transform: rotateY(-90deg);
}

.door-right .door-edge {
  left: 0;
  transform-origin: left center;
  transform: rotateY(90deg);
}

.is-open .door-left {
  transform: rotateY(80deg);
}

.is-open .door-right {
  transform: rotateY(-80deg);
}

/* ------------------------------------------- 비네팅 */
.vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(
    ellipse 75% 70% at 50% 42%,
    transparent 40%,
    rgba(12, 7, 3, 0.72) 100%
  );
  transition: opacity 1.4s ease;
}

.is-inside .vignette {
  opacity: 0.2;
}

/* ------------------------------------------- 문구 · 버튼 */
.intro-copy {
  position: absolute;
  top: 43%;
  left: 50%;
  z-index: 2;
  width: min(90vw, 640px);
  transform: translate(-50%, -50%);
  text-align: center;
  opacity: 0;
  transition:
    opacity 0.9s ease 0.3s,
    transform 0.9s var(--ease-out);
}

.is-ready .intro-copy {
  opacity: 1;
}

.is-open .intro-copy {
  opacity: 0;
  transform: translate(-50%, -46%);
  pointer-events: none;
  transition-delay: 0s;
}

.copy-lead {
  font-family: var(--f-serif-kr);
  font-size: clamp(18px, 1.3vw + 1.2vh, 28px);
  line-height: 1.7;
  color: #f3e6d3;
  text-shadow:
    0 2px 14px rgba(0, 0, 0, 0.55);
}

.copy-title {
  margin: 3.4vh 0 1.4vh;
  font-family: var(--f-serif-en);
  font-weight: 600;
  font-size: clamp(34px, 2.4vw + 2.4vh, 62px);
  line-height: 1.1;
  letter-spacing: 0.14em;
  color: #fffaf0;
  text-shadow:
    0 1px 3px rgba(12, 7, 3, 0.9),
    0 3px 12px rgba(12, 7, 3, 0.75),
    0 8px 36px rgba(12, 7, 3, 0.6);
}

.copy-sub {
  font-family: var(--f-serif-en);
  font-size: clamp(11px, 0.4vw + 0.6vh, 14px);
  letter-spacing: 0.55em;
  padding-left: 0.55em;
  color: #d9c4a3;
}

.enter-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 5.4vh;
  min-height: 52px;
  padding: 0 2.3em;
  border: 1px solid rgba(235, 208, 170, 0.8);
  border-radius: 999px;
  background: rgba(30, 18, 10, 0.35);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  font-size: clamp(15px, 0.3vw + 0.9vh, 17px);
  color: #f7ecdc;
  transition:
    background-color 0.3s ease,
    border-color 0.3s ease;
}

.enter-btn:hover {
  background: rgba(201, 164, 106, 0.28);
  border-color: #efd7ae;
}

.enter-btn svg {
  transition: transform 0.3s ease;
}

.enter-btn:hover svg {
  transform: translateX(4px);
}

.scroll-hint {
  position: absolute;
  bottom: max(3.5vh, env(safe-area-inset-bottom));
  left: 50%;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  transform: translateX(-50%);
  font-family: var(--f-serif-en);
  font-size: 11px;
  letter-spacing: 0.26em;
  color: rgba(240, 225, 200, 0.82);
  opacity: 0;
  transition: opacity 0.9s ease 0.6s;
}

.is-ready .scroll-hint {
  opacity: 1;
}

.is-open .scroll-hint {
  opacity: 0;
  transition-delay: 0s;
}

.scroll-hint svg {
  animation: hint-bounce 1.8s ease-in-out infinite;
}

@keyframes hint-bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(5px);
  }
}

/* ------------------------------------------- 커튼 */
.curtain {
  position: absolute;
  inset: 0;
  z-index: 3;
  background: var(--c-brown-950);
  opacity: 0;
  pointer-events: none;
  transition: opacity 1s ease;
}

.is-leaving .curtain {
  opacity: 1;
}

/* ------------------------------------------- 모바일 */
@media (max-width: 767px) {
  /* 문이 작으므로 두께도 비율에 맞게 줄임 (회전 방식은 데스크톱과 동일) */
  .door::before,
  .door-edge {
    width: 10px;
  }

  .door img.door-back {
    transform: translateZ(-10px);
  }

  .is-inside .scene-inner {
    transform: scale(2);
  }

  .intro-copy {
    top: 44%;
  }

  .copy-title {
    letter-spacing: 0.1em;
  }

  .copy-sub {
    letter-spacing: 0.42em;
  }
}

@media (prefers-reduced-motion: reduce) {
  .scene,
  .scene-inner,
  .door {
    transition: opacity 0.4s ease;
  }

  .is-inside .scene {
    animation: none;
  }

  .scroll-hint svg {
    animation: none;
  }
}
</style>
