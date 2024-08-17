<template>
  <header class="flex justify-content-center">
    <Toolbar class="w-full p-4">
      <template #start>
        <div class="w-full mr-4 cursor-pointer" @click="goTo(routes[0])">
          <span>S O U L  <b>S Y N C</b></span>
        </div>
        <div class="flex align-items-center gap-2">
          <Button
              v-for="route in routes"
              :key="route.route"
              :label="route.label"
              text
              plain
              @click="goTo(route)"
              :class="{'active-button': isActive(route.route)}"
          />
        </div>
      </template>
      <template #end>
        <Button
            icon="pi pi-sign-out"
            label="Log Out"
            severity="secondary"
            @click="logOut"
        />
      </template>
    </Toolbar>
  </header>
</template>
<script>
import {useUserStore} from "@/store/user-calls";

export default {
  name: "Header",
  data: () => ({
    routes: [
      {
        label: 'Playlists',
        route: 'panel-view',
        isInternal: true
      },
      {
        label: 'Configuration',
        route: 'configuration-view',
        isInternal: true
      },
      {
        label: 'Statistics',
        route: 'stats-view',
        isInternal: true
      },
      {
        label: 'Help',
        isInternal: false,
        go: () => {
          const current = JSON.parse(localStorage.getItem('version')).current;
          window.open(`https://soulsync.xavi.tech/manual?v=${current}`, '_blank');
        }
      },
      {
        label: 'GitHub',
        isInternal: false,
        go: () => window.open('https://github.com/xaviqo/SoulSyncBot', '_blank')
      }
    ]
  }),
  computed: {
    userCalls: () => useUserStore()
  },
  methods: {
    logOut(){
      this.userCalls.logOut();
      this.emitter.emit('alert',{
        severity: 'success',
        message: 'Session successfully closed'
      });
      this.$router.push("/");
      window.location.reload();
    },
    goTo(route) {
      if (route.isInternal)
        this.$router.push({ name: route.route })
      else
        route.go();
    },
    isActive(routeName) {
      return this.$route.name === routeName;
    }
  }
}
</script>
<style scoped>
.active-button {
  font-weight: bold;
  color: var(--primary-color); /* Ajusta el color según el tema que uses */
}
</style>