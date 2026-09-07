import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/layout/AppLayout.vue'
import { SITE_NAME } from '@/constants/site'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/AuthView.vue'),
    meta: { title: '登录', guestOnly: true },
  },
  {
    path: '/articles/:id',
    component: () => import('@/layout/ArticleLayout.vue'),
    children: [
      {
        path: '',
        name: 'ArticleDetail',
        component: () => import('@/views/ArticleDetailView.vue'),
        meta: { title: '文章详情' },
      },
    ],
  },
  {
    path: '/',
    component: AppLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HotView.vue'),
        meta: { title: '热门', menu: 'hot' },
      },
      {
        path: 'hot',
        redirect: '/',
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/CategoryView.vue'),
        meta: { title: '分类', menu: 'category' },
      },
      {
        path: 'favorite',
        name: 'Favorite',
        component: () => import('@/views/FavoritesView.vue'),
        meta: { title: '收藏', menu: 'favorite' },
      },
      {
        path: 'about',
        name: 'About',
        component: () => import('@/views/AboutView.vue'),
        meta: { title: '关于我', menu: 'about' },
      },
      {
        path: 'articles/latest',
        name: 'LatestArticles',
        component: () => import('@/views/LatestArticlesView.vue'),
        meta: { title: '最新文章' },
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/SearchView.vue'),
        meta: { title: '搜索' },
      },
      {
        path: 'articles/tag/:tag',
        name: 'TagArticles',
        component: () => import('@/views/TagArticlesView.vue'),
        meta: { title: '标签文章' },
      },
      {
        path: 'manage',
        component: () => import('@/layout/ManageLayout.vue'),
        meta: { title: '管理', menu: 'manage', requiresAdmin: true },
        redirect: '/manage/stats',
        children: [
          {
            path: 'stats',
            name: 'ManageStats',
            component: () => import('@/views/manage/StatsView.vue'),
            meta: { title: '统计', manageMenu: 'stats', requiresAdmin: true },
          },
          {
            path: 'publish',
            name: 'ManagePublish',
            component: () => import('@/views/manage/PublishArticleView.vue'),
            meta: { title: '发布文章', manageMenu: 'publish', requiresAdmin: true },
          },
          {
            path: 'my-articles',
            name: 'ManageMyArticles',
            component: () => import('@/views/manage/MyArticlesView.vue'),
            meta: { title: '我的文章', manageMenu: 'my-articles', requiresAdmin: true },
          },
          {
            path: 'comments',
            name: 'ManageComments',
            component: () => import('@/views/manage/CommentsView.vue'),
            meta: { title: '评论管理', manageMenu: 'comments', requiresAdmin: true },
          },
          {
            path: 'articles',
            name: 'ManageArticles',
            component: () => import('@/views/manage/ArticlesView.vue'),
            meta: { title: '文章管理', manageMenu: 'articles', requiresAdmin: true },
          },
          {
            path: 'users',
            name: 'ManageUsers',
            component: () => import('@/views/manage/UsersView.vue'),
            meta: { title: '用户管理', manageMenu: 'users', requiresAdmin: true },
          },
          {
            path: 'logs',
            name: 'ManageLogs',
            component: () => import('@/views/manage/LogsView.vue'),
            meta: { title: '日志记录', manageMenu: 'logs', requiresAdmin: true },
          },
        ],
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.afterEach((to) => {
  const pageTitle = to.meta.title
  document.title = pageTitle ? `${pageTitle} - ${SITE_NAME}` : SITE_NAME
})

router.beforeEach((to) => {
  if (to.meta.guestOnly) {
    const token = localStorage.getItem('blog_token')
    if (token) {
      return typeof to.query.redirect === 'string' ? to.query.redirect : '/'
    }
  }

  const requiresAdmin = to.matched.some((record) => record.meta.requiresAdmin)
  if (requiresAdmin) {
    const stored = localStorage.getItem('blog_user')
    if (stored) {
      try {
        const user = JSON.parse(stored)
        const roles = user.roles || []
        const isAdmin = roles.some((r) => ['ADMIN', 'MASTER'].includes(String(r).toUpperCase()))
        if (!isAdmin) return { name: 'Home' }
      } catch {
        return { name: 'Home' }
      }
    } else {
      return { name: 'Home' }
    }
  }
})

export default router
