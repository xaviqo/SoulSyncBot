import { createRouter, createWebHistory } from 'vue-router'
import MainView from "@/views/MainView.vue";
import ConfigurationView from "@/views/ConfigurationView.vue";
const routes = [
  {
    path: '/',
    name: 'home',
    component: MainView
  },
  {
    path: '/configuration',
    name: 'configuration',
    component: ConfigurationView
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
