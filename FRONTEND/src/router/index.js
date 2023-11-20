import { createRouter, createWebHistory } from 'vue-router'
import MainView from "@/views/MainView.vue";
import ConfigurationView from "@/views/ConfigurationView.vue";
import LoginView from "@/views/LoginView.vue";
const routes = [
  {
    path: '/',
    name: 'home',
    component: MainView,
    meta: {
      needsAuth: true
    }
  },
  {
    path: '/configuration',
    name: 'configuration',
    component: ConfigurationView,
    meta: {
      needsAuth: true
    }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      needsAuth: false
    }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});


router.beforeEach((to, from, next) => {
  const hasToken = localStorage.getItem("token") != null;
  if (to.meta.needsAuth){
    if (hasToken) next();
    else next("/login");
  }
  next();
});

export default router
