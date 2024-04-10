import {createRouter, createWebHistory} from 'vue-router'
import {useUserStore} from "@/store/user-calls";

const routes = [
  {
    path: '/login',
    name: 'login-view',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/panel',
    name: 'panel-view',
    component: () => import('../views/PanelView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach( async (to, from, next) => {
  if (to.path !== '/login' && !isAuthenticated()) {
    next('/login');
  } else {
    next();
  }
})

const isAuthenticated = () => {
  useUserStore().checkAuthenticated();
  return useUserStore().isAuthenticated;
}

export default router
