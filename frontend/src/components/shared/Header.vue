<template>
  <header class="flex justify-content-center">
    <Toolbar class="w-full p-4">
      <template #start>
        <div class="w-full mr-4 cursor-pointer" @click="goTo('panel-view')">
          <span>S O U L  <b>S Y N C</b></span>
        </div>
        <div class="flex align-items-center gap-2">
          <Button
              v-for="route in routes"
              :key="route.route"
              :label="route.label"
              text
              plain
              @click="goTo(route.isInternal,route.route)"
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
        route: 'https://soulsync.xavi.tech/manual',
        isInternal: false
      },
      {
        label: 'GitHub',
        route: 'https://github.com/xaviqo/SoulSyncBot',
        isInternal: false
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
    goTo(isInternal, route) {
      if (isInternal)
        this.$router.push({ name: route })
      else
        window.open(route, '_blank');
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