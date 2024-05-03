import {createRouter, createWebHistory} from 'vue-router'
import {useUserStore} from "@/store/user-calls";

const soulsyncTitle = 'SoulSync';
const routes = [
  {
    path: '/login',
    name: 'login-view',
    component: () => import('../views/LoginView.vue'),
    beforeEnter: (to, from, next) => {
      if (isAuthenticated())
        next('/panel');
      else
        next();
    },
    meta: {
      title: 'Login Panel'
    }
  },
  {
    path: '/panel',
    name: 'panel-view',
    component: () => import('../views/PanelView.vue'),
    beforeEnter: (to, from, next) => {
      if (isAuthenticated())
        next();
      else
        next('/login');
    },
    meta: {
      title: 'Bot Panel'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

router.beforeEach((to, from, next) => {
  document.title = `${soulsyncTitle} - ${to.meta.title}`;
  next();
});

const isAuthenticated = () => {
  useUserStore().checkAuthenticated();
  return useUserStore().isAuthenticated;
};

export default router;
