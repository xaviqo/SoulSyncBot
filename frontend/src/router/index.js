import {createRouter, createWebHistory} from 'vue-router'
import {useUserStore} from "@/store/user-calls";

const soulsyncTitle = 'SoulSync';

const goToLogin = (to, from, next) => {
  if (isAuthenticated()) {
    next();
  } else {
    next('/');
  }
};

const goToPlaylists = (to, from, next) => {
  if (isAuthenticated()) {
    next('/playlist');
  } else {
    next();
  }
};

const routes = [
  {
    path: '/login',
    name: 'login-view',
    component: () => import('../views/LoginView.vue'),
    beforeEnter: goToPlaylists,
    meta: {
      title: 'Login Panel'
    }
  },
  {
    path: '/configuration',
    name: 'configuration-view',
    component: () => import('../views/ConfigurationView.vue'),
    beforeEnter: goToLogin,
    meta: {
      title: 'Configuration Panel'
    }
  },
  {
    path: '/playlist',
    name: 'panel-view',
    component: () => import('../views/MainView.vue'),
    beforeEnter: goToLogin,
    meta: {
      title: 'Manager'
    }
  },
  {
    path: '/playlist/:id',
    name: 'playlist-view',
    component: () => import('../views/PlaylistView.vue'),
    beforeEnter: goToLogin,
    meta: {
      title: "Playlist View",
    }
  },
  {
    path: '/playlist/:pl/download-list/:dl',
    name: 'download-list-view',
    component: () => import('../views/DownloadListView.vue'),
    beforeEnter: goToLogin,
    meta: {
      title: "Playlist View",
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
