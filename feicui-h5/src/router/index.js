import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // ---- 游客可访问 ----
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: 'AI翡翠匹配' } },
  { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue'), meta: { title: '商品详情' } },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { title: '商家入驻' } },

  // ---- 需登录 (商家后台) ----
  { path: '/dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '商家后台', requireAuth: true } },
  { path: '/profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心', requireAuth: true } },
  { path: '/publish', name: 'PublishUpload', component: () => import('@/views/PublishUpload.vue'), meta: { title: '发布商品', requireAuth: true } },
  { path: '/publish/edit', name: 'PublishEdit', component: () => import('@/views/PublishEdit.vue'), meta: { title: '编辑商品', requireAuth: true } },
  { path: '/products', name: 'ProductManage', component: () => import('@/views/ProductManage.vue'), meta: { title: '商品管理', requireAuth: true } },
  { path: '/leads', name: 'LeadList', component: () => import('@/views/LeadList.vue'), meta: { title: '客资列表', requireAuth: true } },
  { path: '/lead/:id', name: 'LeadDetail', component: () => import('@/views/LeadDetail.vue'), meta: { title: '客资详情', requireAuth: true } },
  { path: '/account', name: 'AccountPermission', component: () => import('@/views/AccountPermission.vue'), meta: { title: '账户权限', requireAuth: true } },
  { path: '/notifications', name: 'Notifications', component: () => import('@/views/Notifications.vue'), meta: { title: '系统通知', requireAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 全局前置守卫: 需登录页面校验
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} · 高翠网` : '高翠网'
  if (to.meta.requireAuth) {
    const auth = useAuthStore()
    if (!auth.isLogin) {
      return next({ path: '/login', query: { redirect: to.fullPath } })
    }
  }
  next()
})

export default router
