<template>
  <Toolbar class="p-4 border-1 border-bottom-1 border-black-alpha-90 shadow-1" style="background-color: #f8f9fa;">
    <template #start>
      <div
          class="flex justify-content-between"
          v-if="isConnected"
      >
        <router-link
            v-for="item in menu"
            :key="item.name"
            :to="item.to"
        >
          <div
              class="p-2 border-1 border-black-alpha-90 hover:surface-100 cursor-pointer shadow-1 mr-4"
              :class="$route.path === item.to ? 'surface-100' : 'surface-50'"
          >
            <i :class="'mr-2 pi '+item.icon" style="font-size: .9rem" />
            {{ item.name }}
          </div>
        </router-link>
        <div
            class="p-2 border-1 border-black-alpha-90 surface-50 hover:surface-100 cursor-pointer shadow-1 mr-4"
            @click="logout"
        >
          <i class="mr-2 pi pi-reply" style="font-size: .9rem" />
          Log Out
        </div>
      </div>
    </template>
    <template #center >
      <span v-if="!isConnected" class="text-2xl">S O U L  <b>S Y N C</b></span>
    </template>
    <template #end>
      <HealthStatusComp v-if="mq.mdPlus && isConnected"/>
    </template>
  </Toolbar>
</template>
<script>
import HealthStatusComp from "@/components/HealthStatusComp.vue";
import {mapActions, mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";

export default {
  name: 'NavBar',
  inject: ["mq"],
  components: {HealthStatusComp},
  data: () => ({
    isBurgerOpen: false,
    menu: [
      { name: 'Manager', icon: 'pi-sync', to: '/' },
      { name: 'Configuration', icon: 'pi-wrench', to: '/configuration' },
      // { name: 'Browse', icon: 'pi-folder', to: '/browse' },
    ]
  }),
  methods: {
    logout(){
      this.emitter.emit('show-alert', {
        info: 'See you soon. I will keep on doing my thing in the background...',
        icon: 'pi-info-circle',
        severity: 'success'
      })
      this.deleteToken();
      this.$router.push("/login");
    },
    ...mapActions(useUserCfgStore, [
      'deleteToken',
    ])
  },
  computed: {
    ...mapState(useUserCfgStore, ['isConnected'])
  }
}
</script>
<style>
a {
  text-decoration: none;
  color: black;
}
</style>