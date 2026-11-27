import { createRouter, createWebHistory } from 'vue-router'
import IntroView from '@/views/IntroView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'intro', component: IntroView },
    { path: '/home', name: 'home', component: () => import('@/views/HomeView.vue') },
    {
      path: '/perfumes',
      name: 'perfumes',
      component: () => import('@/views/PerfumeExploreView.vue')
    },
    { path: '/aura', name: 'aura', component: () => import('@/views/AuraView.vue') },
    { path: '/health', name: 'health', component: () => import('@/views/HealthView.vue') },
    { path: '/admin', name: 'admin', component: () => import('@/views/admin/AdminDataView.vue') }
  ],
  scrollBehavior: () => ({ top: 0 })
})

export default router
