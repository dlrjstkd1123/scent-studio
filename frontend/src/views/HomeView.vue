<script setup>
import AppIcon from '@/components/common/AppIcon.vue'
import SiteHeader from '@/components/layout/SiteHeader.vue'
import citrusForest from '@/assets/images/home/citrus-forest.webp'
import woodyAmber from '@/assets/images/home/woody-amber.webp'
import whiteFloral from '@/assets/images/home/white-floral.webp'
import greenTea from '@/assets/images/home/green-tea.webp'
import atelierToday from '@/assets/images/home/atelier-today.webp'
import blending from '@/assets/images/home/blending.webp'
import gift from '@/assets/images/home/gift.webp'

// 인트로에서 문을 열고 들어온 경우: 어두운 화면에서 공방이 밝아지며 시작
const fromIntro = Boolean(window.history.state?.fromIntro)

// TODO: 추후 백엔드 API(향수 목록) 연동
const RECOMMENDS = [
  { id: 1, name: '시트러스 포레스트', desc: '상쾌한 아침의 시작', tags: ['시트러스', '#산뜻함'], image: citrusForest },
  { id: 2, name: '우디 앰버', desc: '차분한 깊이의 여운', tags: ['우디', '#묵직함'], image: woodyAmber },
  { id: 3, name: '화이트 플로럴', desc: '순수한 하루의 온기', tags: ['플로럴', '#사랑스러움'], image: whiteFloral },
  { id: 4, name: '그린 티 무드', desc: '편안한 일상의 쉼표', tags: ['그린', '#편안함'], image: greenTea }
]

const STORIES = [
  {
    id: 'atelier',
    title: '오늘의 공방',
    desc: ['향이 만들어지는 이야기,', '지금 만나보세요.'],
    link: '공방 둘러보기',
    image: atelierToday
  },
  {
    id: 'blending',
    title: '나만의 향, 블렌딩',
    desc: ['당신의 이야기를 담은', '향을 만들어보세요.'],
    link: '블렌딩 시작하기',
    image: blending
  },
  {
    id: 'gift',
    title: '특별한 선물',
    desc: ['소중한 사람에게,', '향으로 마음을 전하세요.'],
    link: '쇼핑하기',
    image: gift
  }
]
</script>

<template>
  <div class="home" :class="{ 'from-intro': fromIntro }">
    <SiteHeader />

    <main>
      <!-- 히어로 -->
      <section class="hero">
        <div class="hero-bg" aria-hidden="true" />
        <div class="hero-inner container">
          <p class="hero-eyebrow">FIND YOUR OWN SCENT</p>
          <h1 class="hero-title">당신만의 향을<br />찾는 공간</h1>
          <p class="hero-desc">향이 머무는 곳에서,<br />더 특별한 일상이 시작됩니다.</p>
          <div class="hero-actions">
            <RouterLink to="/perfumes" class="btn btn-primary">
              향수 탐색하기
              <AppIcon name="arrow" :size="18" />
            </RouterLink>
            <a href="#" class="btn btn-outline" @click.prevent>
              나만의 향 만들기
              <AppIcon name="arrow" :size="18" />
            </a>
          </div>
        </div>
      </section>

      <!-- 추천 향수 -->
      <section class="section container" aria-labelledby="recommend-title">
        <div class="section-head">
          <h2 id="recommend-title">지금, 이 향을 추천해요</h2>
          <RouterLink to="/perfumes" class="more-link">
            전체보기
            <AppIcon name="arrow" :size="16" />
          </RouterLink>
        </div>

        <ul class="product-list">
          <li v-for="item in RECOMMENDS" :key="item.id">
            <a href="#" class="product-card" @click.prevent>
              <img
                class="product-thumb"
                :src="item.image"
                :alt="item.name"
                width="240"
                height="240"
                loading="lazy"
              />
              <div class="product-body">
                <h3 class="product-name">{{ item.name }}</h3>
                <p class="product-desc">{{ item.desc }}</p>
                <ul class="tags">
                  <li v-for="tag in item.tags" :key="tag">{{ tag }}</li>
                </ul>
              </div>
            </a>
          </li>
        </ul>
      </section>

      <!-- 공방 · 블렌딩 · 선물 -->
      <section class="section section-stories container" aria-label="서비스 소개">
        <ul class="story-list">
          <li v-for="story in STORIES" :key="story.id">
            <a href="#" class="story-card" @click.prevent>
              <img
                class="story-thumb"
                :src="story.image"
                alt=""
                width="330"
                height="276"
                loading="lazy"
              />
              <div class="story-body">
                <h3 class="story-title">{{ story.title }}</h3>
                <p class="story-desc">{{ story.desc[0] }}<br />{{ story.desc[1] }}</p>
                <span class="story-link">
                  {{ story.link }}
                  <AppIcon name="arrow" :size="16" />
                </span>
              </div>
              <span class="story-go" aria-hidden="true">
                <AppIcon name="arrow" :size="14" />
              </span>
            </a>
          </li>
        </ul>
      </section>
    </main>

    <div v-if="fromIntro" class="intro-curtain" aria-hidden="true" />
  </div>
</template>

<style scoped>
.container {
  width: min(var(--container), 100% - 48px);
  margin-inline: auto;
}

/* ------------------------------------------------------------------ 히어로 */
.hero {
  position: relative;
  display: flex;
  align-items: center;
  height: clamp(540px, 70vh, 660px);
  padding-top: 76px;
  overflow: hidden;
  background: var(--c-brown-950);
  color: #fbf4ea;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(
      90deg,
      rgba(20, 12, 6, 0.8) 0%,
      rgba(20, 12, 6, 0.5) 38%,
      rgba(20, 12, 6, 0.05) 72%
    ),
    linear-gradient(0deg, rgba(20, 12, 6, 0.35), transparent 30%),
    url('../assets/images/home/hero-atelier.webp') 70% center / cover;
  animation: hero-in 1.8s var(--ease-out) both;
}

.from-intro .hero-bg {
  animation-duration: 2.4s;
  --hero-from: 1.18;
}

@keyframes hero-in {
  from {
    transform: scale(var(--hero-from, 1.06));
  }
  to {
    transform: scale(1);
  }
}

.hero-inner {
  position: relative;
  padding-left: min(10vw, 150px);
  animation: rise 1s var(--ease-out) 0.25s both;
}

.from-intro .hero-inner {
  animation-delay: 0.9s;
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.hero-eyebrow {
  font-family: var(--f-serif-en);
  font-size: 13px;
  letter-spacing: 0.36em;
  color: var(--c-gold-light);
}

.hero-title {
  margin-top: 18px;
  font-family: var(--f-serif-kr);
  font-weight: 500;
  font-size: clamp(40px, 3.6vw, 56px);
  line-height: 1.32;
  letter-spacing: -0.01em;
}

.hero-desc {
  margin-top: 20px;
  font-size: 17px;
  line-height: 1.7;
  color: rgba(251, 244, 234, 0.86);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 36px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 54px;
  padding: 0 30px;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 500;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease;
}

.btn svg {
  transition: transform 0.25s ease;
}

.btn:hover svg {
  transform: translateX(3px);
}

.btn-primary {
  border: 1px solid rgba(255, 228, 196, 0.35);
  background: var(--c-brown-500);
  color: #fff;
}

.btn-primary:hover {
  background: #9b6a42;
}

.btn-outline {
  border: 1px solid rgba(255, 244, 230, 0.82);
  color: #fff;
}

.btn-outline:hover {
  background: rgba(255, 244, 230, 0.12);
}

/* ------------------------------------------------------------------ 섹션 공통 */
.section {
  padding-top: 40px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-head h2 {
  font-size: 21px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.more-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--c-muted);
}

.more-link:hover {
  color: var(--c-brown-600);
}

/* ------------------------------------------------------------------ 추천 향수 */
.product-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 100%;
  padding: 14px;
  border-radius: 16px;
  background: var(--c-ivory);
  box-shadow: 0 8px 26px rgba(91, 58, 34, 0.07);
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 32px rgba(91, 58, 34, 0.12);
}

.product-thumb {
  flex: none;
  width: 92px;
  height: 92px;
  border-radius: 12px;
  object-fit: cover;
}

.product-body {
  min-width: 0;
}

.product-name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.product-desc {
  margin-top: 4px;
  font-size: 14px;
  color: var(--c-muted);
}

.tags {
  display: flex;
  flex-wrap: nowrap;
  gap: 6px;
  margin-top: 12px;
}

.tags li {
  padding: 4px 11px;
  border-radius: 999px;
  background: var(--c-tag-bg);
  font-size: 12px;
  color: var(--c-tag-text);
  white-space: nowrap;
}

/* ------------------------------------------------------------------ 공방 · 블렌딩 · 선물 */
.section-stories {
  padding-top: 24px;
  padding-bottom: 80px;
}

.story-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.story-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 24px;
  height: 100%;
  padding: 14px;
  border-radius: 18px;
  background: var(--c-beige);
  transition: background-color 0.25s ease;
}

.story-card:hover {
  background: #ece1d2;
}

.story-thumb {
  flex: none;
  width: 44%;
  max-width: 170px;
  height: auto;
  aspect-ratio: 330 / 276;
  border-radius: 12px;
  object-fit: cover;
}

.story-title {
  font-size: 19px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.story-desc {
  margin-top: 8px;
  font-size: 15px;
  line-height: 1.6;
  color: var(--c-muted);
}

.story-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 16px;
  font-size: 15px;
  font-weight: 700;
  color: var(--c-brown-700);
}

.story-go {
  display: none;
}

/* 인트로에서 넘어올 때 덮개 */
.intro-curtain {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: var(--c-brown-950);
  pointer-events: none;
  animation: curtain-out 1.1s ease 0.1s both;
}

@keyframes curtain-out {
  to {
    opacity: 0;
    visibility: hidden;
  }
}

/* ------------------------------------------------------------------ 태블릿 */
@media (max-width: 1180px) {
  .product-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1023px) {
  .hero {
    padding-top: 0;
    height: clamp(460px, 64vh, 560px);
  }

  .hero-inner {
    padding-left: 0;
  }

  .story-list {
    grid-template-columns: 1fr;
  }

  .story-thumb {
    max-width: 220px;
  }
}

/* ------------------------------------------------------------------ 모바일 */
@media (max-width: 767px) {
  .container {
    width: calc(100% - 40px);
  }

  .hero {
    height: auto;
    min-height: 440px;
    padding: 56px 0 48px;
    text-align: center;
  }

  .hero-bg {
    background:
      linear-gradient(
        180deg,
        rgba(20, 12, 6, 0.55),
        rgba(20, 12, 6, 0.4) 50%,
        rgba(20, 12, 6, 0.6)
      ),
      url('../assets/images/home/hero-atelier.webp') 74% center / cover;
  }

  .hero-eyebrow {
    font-size: 11px;
    letter-spacing: 0.3em;
  }

  .hero-title {
    margin-top: 14px;
    font-size: 31px;
  }

  .hero-desc {
    margin-top: 14px;
    font-size: 15px;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
    gap: 12px;
    margin-top: 28px;
  }

  .btn {
    width: min(100%, 320px);
    min-height: 52px;
  }

  .section {
    padding-top: 28px;
  }

  .section-head {
    margin-bottom: 14px;
  }

  .section-head h2 {
    font-size: 18px;
  }

  /* 추천 향수: 가로 스와이프 */
  .product-list {
    display: flex;
    gap: 12px;
    margin-inline: -20px;
    padding: 0 20px 4px;
    overflow-x: auto;
    scroll-snap-type: x mandatory;
    scroll-padding-inline: 20px;
    scrollbar-width: none;
  }

  .product-list::-webkit-scrollbar {
    display: none;
  }

  .product-list > li {
    flex: 0 0 42%;
    min-width: 148px;
    scroll-snap-align: start;
  }

  .product-card {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    padding: 0;
    background: none;
    box-shadow: none;
  }

  .product-card:hover {
    transform: none;
    box-shadow: none;
  }

  .product-thumb {
    width: 100%;
    height: auto;
    aspect-ratio: 5 / 4;
    border-radius: 12px;
  }

  .product-name {
    font-size: 15px;
  }

  .product-desc {
    margin-top: 2px;
    font-size: 13px;
  }

  .tags {
    flex-wrap: nowrap;
    margin-top: 8px;
  }

  .tags li {
    font-size: 11px;
    padding: 4px 9px;
  }

  .section-stories {
    padding-top: 20px;
    padding-bottom: 48px;
  }

  .story-list {
    gap: 12px;
  }

  .story-card {
    gap: 16px;
    padding: 0 16px 0 0;
    overflow: hidden;
    border-radius: 16px;
  }

  .story-thumb {
    width: 42%;
    border-radius: 0;
    align-self: stretch;
    height: auto;
    aspect-ratio: auto;
    min-height: 118px;
  }

  .story-body {
    flex: 1;
    padding: 16px 0;
  }

  .story-title {
    font-size: 17px;
  }

  .story-desc {
    font-size: 14px;
  }

  .story-link {
    display: none;
  }

  .story-go {
    display: inline-flex;
    flex: none;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: var(--c-brown-600);
    color: #fff;
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero-bg,
  .hero-inner {
    animation: none;
  }
}
</style>
