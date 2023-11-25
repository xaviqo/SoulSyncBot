<template>
  <div class="w-full grid grid">
    <div v-for="api in status" :key="api.name" class="col-3 flex justify-content-center align-items-center">
      <div class="grid grid-nogutter border-1 border-black-alpha-90 shadow-1">
        <div
            class="col-2 p-1 border-right-1 border-black-alpha-90 flex align-items-center justify-content-center text-white"
            :class="api.status ? 'bg-green-300' : 'bg-red-300'"
        >
          <i :class=" api.status ? 'pi pi-thumbs-up' : 'pi pi-thumbs-down' "  style="font-size: 0.7rem" ></i>
        </div>
        <div class="col-10 surface-50 flex align-items-center justify-content-end text-overflow-ellipsis">
          <span class="mx-2 font text-xs white-space-nowrap overflow-hidden">{{ api.name }}</span>
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
      { key: "slskd-response", name: "SLSKD API" , status: false },
      { key: "slskd-cfg-ok", name: "SLSKD CFG" , status: false },
      { key: "spotify-response", name: "SPTF API" , status: false },
      { key: "spotify-cfg-ok", name: "SPTF CFG" , status: false }
    ],
    currentStatus: false
  }),
  methods: {
    async checkApis() {
      try {
        const response = (await this.axios.get("/health")).data;
        const lastCurrentStatus = this.currentStatus;
        this.currentStatus = response['all-ok'];
        for (const key in response) {
          const check = this.status.find( o => o.key === key);
          if (check) check.status = response[key];
        }
        if (this.currentStatus !== lastCurrentStatus) {
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
      } catch (err) {
        const statusCode = err?.response?.status;
        if (!statusCode || statusCode >= 400) {
          this.deleteToken();
          this.$router.push("/login");
          this.emitter.emit('show-alert',{
            info: 'Lost connection to SoulSync API',
            icon: 'pi-exclamation-circle',
            severity: 'error'
          });
        }
      }
    },
    ...mapActions(useUserCfgStore, [
      'setApiStatus', 'deleteToken'
    ])
  },
  mounted() {
    this.checkApis();
    this.$subscribe( (mutation) => {
      const refresh = mutation.events.key === 'call';
      if (refresh) {
        this.checkApis();
      }
    });
  },
  computed: {
    ...mapState(useUserCfgStore, ['$subscribe'])
  }
}
</script>
<style scoped>

</style>