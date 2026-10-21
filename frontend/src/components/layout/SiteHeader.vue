<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '@/components/common/AppIcon.vue'

const router = useRouter()

// 로고 클릭: 인트로(문 닫힌 첫 화면)로 이동
const goHome = () => {
  router.push({ name: 'home' })
}

// to 가 없는 메뉴는 아직 화면이 없는 메뉴
const NAV = [
  { label: '홈', to: '/home' },
  { label: '향수 탐색', to: '/perfumes' },
  { label: '나의 향' },
  { label: '향 아우라' },
  { label: '블렌딩' },
  { label: '아틀리에' },
  { label: '쇼핑' }
]

const scrolled = ref(false)
const menuOpen = ref(false)

const onScroll = () => {
  scrolled.value = window.scrollY > 10
}
const onKeydown = (e) => {
  if (e.key === 'Escape') menuOpen.value = false
}

watch(menuOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : ''
})

onMounted(() => {
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('keydown', onKeydown)
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <header class="site-header" :class="{ 'is-scrolled': scrolled }">
    <div class="header-inner">
      <button type="button" class="icon-btn menu-btn" aria-label="메뉴 열기" @click="menuOpen = true">
        <AppIcon name="menu" :size="24" />
      </button>

      <button type="button" class="logo" @click="goHome">SCENT STUDIO</button>

      <nav class="gnb" aria-label="주요 메뉴">
        <ul>
          <li v-for="item in NAV" :key="item.label">
            <RouterLink v-if="item.to" :to="item.to" class="gnb-link" active-class="is-active">
              {{ item.label }}
            </RouterLink>
            <a v-else href="#" class="gnb-link" @click.prevent>{{ item.label }}</a>
          </li>
        </ul>
      </nav>

      <div class="header-util">
        <button type="button" class="icon-btn" aria-label="검색">
          <AppIcon name="search" :size="21" />
        </button>
        <a href="#" class="login-link" @click.prevent>로그인</a>
        <a href="#" class="my-btn" @click.prevent>마이 아틀리에</a>
        <button type="button" class="icon-btn bag-btn" aria-label="장바구니">
          <AppIcon name="bag" :size="22" />
        </button>
      </div>
    </div>
  </header>

  <!-- 모바일 메뉴 -->
  <Transition name="drawer">
    <div v-if="menuOpen" class="drawer-wrap" @click.self="menuOpen = false">
      <aside class="drawer" aria-label="전체 메뉴">
        <div class="drawer-head">
          <span class="logo">SCENT STUDIO</span>
          <button type="button" class="icon-btn" aria-label="메뉴 닫기" @click="menuOpen = false">
            <AppIcon name="close" :size="24" />
          </button>
        </div>
        <ul class="drawer-nav">
          <li v-for="item in NAV" :key="item.label">
            <RouterLink
              v-if="item.to"
              :to="item.to"
              active-class="is-active"
              @click="menuOpen = false"
            >
              {{ item.label }}
            </RouterLink>
            <a v-else href="#" @click.prevent>{{ item.label }}</a>
          </li>
        </ul>
        <div class="drawer-foot">
          <a href="#" class="drawer-login" @click.prevent>로그인</a>
          <a href="#" class="my-btn" @click.prevent>마이 아틀리에</a>
        </div>
      </aside>
    </div>
  </Transition>
</template>

<style scoped>
.site-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  color: #f5ece0;
  background: linear-gradient(180deg, rgba(22, 14, 8, 0.72), rgba(22, 14, 8, 0.25));
  transition: background-color 0.3s ease;
}

.site-header.is-scrolled {
  background: rgba(30, 19, 12, 0.94);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 32px;
  height: 76px;
  padding: 0 clamp(24px, 3.4vw, 56px);
}

.logo {
  font-family: var(--f-serif-en);
  font-size: 27px;
  font-weight: 500;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

.gnb {
  flex: 1;
  display: flex;
  justify-content: center;
}

.gnb ul {
  display: flex;
  gap: clamp(22px, 2.8vw, 44px);
}

.gnb-link {
  position: relative;
  display: block;
  padding: 26px 0;
  font-size: 15px;
  color: rgba(245, 236, 224, 0.82);
  white-space: nowrap;
  transition: color 0.2s ease;
}

.gnb-link:hover,
.gnb-link.is-active {
  color: #fff8ee;
}

.gnb-link.is-active::after {
  content: '';
  position: absolute;
  left: -4px;
  right: -4px;
  bottom: 18px;
  height: 2px;
  background: #efe2cf;
}

.header-util {
  display: flex;
  align-items: center;
  gap: 22px;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin: -8px;
}

.login-link {
  font-size: 15px;
  white-space: nowrap;
}

.my-btn {
  display: inline-flex;
  align-items: center;
  height: 44px;
  padding: 0 24px;
  border-radius: 999px;
  background: #efe2cf;
  color: var(--c-brown-800);
  font-size: 15px;
  font-weight: 700;
  white-space: nowrap;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
  transition: background-color 0.2s ease;
}

.my-btn:hover {
  background: #f7ecdc;
}

.menu-btn,
.bag-btn {
  display: none;
}

/* ------------------------------------------------------------------ 모바일/태블릿 헤더 */
@media (max-width: 1023px) {
  .site-header,
  .site-header.is-scrolled {
    position: sticky;
    color: var(--c-text);
    background: #f8f2ea;
    backdrop-filter: none;
    box-shadow: 0 1px 0 rgba(91, 58, 34, 0.08);
  }

  .header-inner {
    height: 58px;
    padding: 0 20px;
    gap: 0;
  }

  .gnb,
  .login-link,
  .header-util .my-btn {
    display: none;
  }

  .menu-btn,
  .bag-btn {
    display: inline-flex;
  }

  .logo {
    flex: 1;
    text-align: center;
    font-size: 23px;
  }

  .header-util {
    gap: 18px;
  }
}

/* ------------------------------------------------------------------ 드로어 */
.drawer-wrap {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(20, 12, 7, 0.45);
}

.drawer {
  display: flex;
  flex-direction: column;
  width: min(84vw, 340px);
  height: 100%;
  padding: 0 24px max(28px, env(safe-area-inset-bottom));
  background: var(--c-cream);
  box-shadow: 8px 0 30px rgba(0, 0, 0, 0.2);
}

.drawer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.drawer-head .logo {
  font-size: 22px;
}

.drawer-nav {
  flex: 1;
  margin-top: 8px;
}

.drawer-nav a {
  display: block;
  padding: 15px 0;
  border-bottom: 1px solid rgba(91, 58, 34, 0.1);
  font-size: 17px;
}

.drawer-nav a.is-active {
  color: var(--c-brown-600);
  font-weight: 700;
}

.drawer-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.drawer-foot .my-btn {
  background: var(--c-brown-500);
  color: #fff;
  box-shadow: none;
}

.drawer-enter-active,
.drawer-leave-active {
  transition: background-color 0.3s ease;
}

.drawer-enter-active .drawer,
.drawer-leave-active .drawer {
  transition: transform 0.35s var(--ease-out);
}

.drawer-enter-from,
.drawer-leave-to {
  background-color: rgba(20, 12, 7, 0);
}

.drawer-enter-from .drawer,
.drawer-leave-to .drawer {
  transform: translateX(-100%);
}
</style>
