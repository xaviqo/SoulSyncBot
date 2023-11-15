<template>
  <div class="w-18rem grid grid-nogutter">
    <div v-for="api in status" :key="api.name" class="col-6 flex justify-content-center align-items-center">
      <div class="grid grid-nogutter border-1 border-black-alpha-90 shadow-1">
        <div
            class="col-2 w-2rem h-2rem border-right-1 border-black-alpha-90 flex align-items-center justify-content-center text-white"
            :class="api.loading ? 'bg-primary' : (api.alive ? 'bg-green-300' : 'bg-red-300')"
        >
          <i class="pi pi-spin pi-spinner" v-if="api.loading"></i>
          <i class="pi pi-thumbs-up text-black-alpha-80" v-if="!api.loading && api.alive"></i>
          <i class="pi pi-thumbs-down" v-if="!api.loading && !api.alive"></i>
        </div>
        <div class="surface-50 flex align-items-center justify-content-start">
          <span class="mx-2 font-medium">{{ api.name.toUpperCase() }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import {mapActions, mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";

export default {
  name: "HealthStatusComp",
  data: () => ({
    status: [
      {
        name: 'SoulSync',
        loading: true,
        alive: false,
        route: '/health/soulsync'
      },
      {
        name: 'Slskd',
        loading: true,
        alive: false,
        route: '/health/slskd'
      }
    ],
    currentStatus: false
  }),
  methods: {
    async checkApis() {
      for (const api of this.status) {
        await this.check(api);
      }

      const hasStatusChanged = this.status.some(api => api.alive !== this.currentStatus);
      if (hasStatusChanged) {
        this.currentStatus = !this.currentStatus;
        this.setApiStatus(this.currentStatus);
        this.emitter.emit('show-alert', {
          info: this.currentStatus
              ? 'API connection active'
              : 'API connection failed'
          ,
          icon: 'pi-sync'
          ,
          severity: this.currentStatus
              ? 'success'
              : 'error'
        })
      }
    },
    async check(api) {
      api.loading = true;
      try {
        const res = await this.axios.get(api.route);
        const isOK = res.status === 200 && res.data === 'OK';
        if (isOK) {
          api.alive = true;
        } else {
          api.alive = false;
        }
      } catch (e) {
        console.log(e);
        api.alive = false;
      } finally {
        api.loading = false;
      }
    },
    ...mapActions(useUserCfgStore, [
      'setApiStatus',
    ])
  },
  created() {
    this.emitter.on('trigger-refresh', () => {
      this.checkApis();
    });
  },
  computed: {
    ...mapState(useUserCfgStore, ['refresh'])
  }
}
</script>
<style scoped>

</style>