<template>
  <div
      class="min-h-screen"
      :style="'background-color:'+GlobalColors.background"
  >
    <NavBar></NavBar>
    <div class="w-full flex align-items-center justify-content-center">
      <Message
          v-if="message.show"
          class="border-1 border-black-alpha-90 shadow-3"
          :severity="message.severity"
          :icon="message.icon"
          closable
          @close="closeMessage()"
          style="position: absolute; top: 55px; right: 85px"
      >
        <span class="mr-2">{{ message.info }}</span>
      </Message>
      <div class="w-full p-2" style="max-width: 1350px">
        <router-view/>
      </div>
    </div>
  </div>
</template>
<script>
import NavBar from "@/components/NavBar.vue";
import GlobalColors from "@/models/GlobalColors";
import {mapActions} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";
/*
{ "current": "xl", "xs": false, "smMinus": false, "smPlus": true, "sm": false, "mdMinus": false, "mdPlus": true, "md": false, "lgMinus": false, "lgPlus": true, "lg": false, "xl": true, "orientation": "landscape", "isLandscape": true, "isPortrait": false, "theme": "dark", "isDark": true, "isLight": false, "motionPreference": "no-preference", "isMotion": true, "isInert": false }
*/
const msgModel = {
  show: false,
  info: null,
  icon: null,
  severity: null
};
export default {
  name: 'SoulSyncApp',
  computed: {
    GlobalColors() {
      return GlobalColors
    },
  },
  inject: ["mq"],
  components: {NavBar},
  data: () => ({
    message: msgModel
  }),
  methods: {
    showAlert(payload){
      this.message = {
        show: true,
        info: payload.info,
        icon: `pi ${payload.icon}`,
        severity: payload.severity
      };
      setTimeout(() => this.message.show = false,2000)
    },
    closeMessage(){
      setTimeout(() => this.message.show = false,1000)
    },
    ...mapActions(useUserCfgStore, [
      'checkStatus',
    ])
  },
  created() {
    this.checkStatus();
    this.emitter.on('show-alert', payload => this.showAlert(payload));
  },
  mounted() {
    this.emitter.emit('trigger-refresh');
  }
}
</script>
<style>
body{
  margin: 0;
}
</style>
