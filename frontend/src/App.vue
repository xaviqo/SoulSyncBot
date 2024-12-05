<template>
  <div class="flex flex-wrap mt-3 justify-content-center">
    <div class="w-12 md:w-10 xl:w-8 flex flex-wrap gap-3 mx-3">
      <Header v-if="isAuthenticated" class="w-12"></Header>
      <transition-group name="p-message" tag="div" class="absolute mt-8 right-0" style="z-index: 9999">
        <Message
            class="right-0 mr-6"
            v-for="msg of messages"
            :key="msg.id"
            :severity="msg.severity"
        >
          {{ msg.message }}
        </Message>
      </transition-group>
      <router-view class="w-12" :key="$route.fullPath"/>
      <Dialog
          v-model:visible="showLoadingDialog"
          :pt="{
        root: 'border-none',
        mask: { style: 'backdrop-filter: blur(2px)' }
    }"
      >
        <template #container>
          <div class="p-4">
            <div v-if="loadingText" class="flex justify-content-center align-items-center text-xl">
              <i class="pi pi-spin pi-cog mr-2 text-white-alpha-80" style="font-size: 1.1rem"></i>
              <span class="font-bold white-space-nowrap blink">{{ loadingText }}</span>
            </div>
            <div class="w-full flex flex-wrap justify-content-center p-6">
              <ProgressSpinner class="m-2"/>
            </div>
          </div>
        </template>
      </Dialog>
      <ConfirmDialog>
        <template #message="slotProps">
          <div class="flex align-items-center w-full gap-3 border-bottom-1 surface-border">
            <div class="w-2 flex align-items-center justify-content-center">
              <i :class="slotProps.message.icon" class="text-6xl text-primary-500"></i>
            </div>
            <div class="w-10">
              <p>{{ slotProps.message.message }}</p>
            </div>
          </div>
        </template>
      </ConfirmDialog>
    </div>
    <div v-if="isDemo" class="fixed p-3 w-full flex justify-content-center bg-gray-900 text-gray-300 shadow-2" style="bottom: 0; letter-spacing: .05em; opacity: .85">
      <div class="fade-effect">
        <span>
          SoulSync is running in demo mode. Some features are hidden or limited.
        </span>
        <span v-if="isAuthenticated" class="cursor-pointer" @click="()  => window.open('https://github.com/xaviqo/SoulSyncBot', '_blank')">
          - v{{ getVersion() }}
        </span>
      </div>
    </div>
  </div>
</template>
<script>
import Header from "@/components/shared/Header.vue";
import {mapActions, mapState} from "pinia";
import {useUserStore} from "@/store/user-calls";
import {usePlaylistStore} from "@/store/playlist-calls";

export default {
  name: 'SoulSync',
  data: () => ({
    isDemo: false,
    isInstalled: false,
    showLoadingDialog: false,
    loadingText: null,
    messages: [],
    alertId: 0
  }),
  components: {
    Header
  },
  created() {
    this.checkIsDemoMode();
    this.checkInstalled();
    this.emitter.on('alert', this.showAlert);
    this.emitter.on('loading', this.showLoading);
    this.emitter.on('confirm', this.showConfirm);
  },
  methods:{
    showConfirm(confirm){
      const { header, message, reject, accept, listenerLabel } = confirm;
      this.$confirm.require({
        header: header,
        message: message,
        icon: 'pi pi-exclamation-circle',
        rejectClass: 'p-button-secondary p-button-outlined',
        rejectLabel: reject,
        acceptLabel: accept,
        accept: () => this.emitter.emit(listenerLabel,true),
        reject: () => this.emitter.emit(listenerLabel,false)
      });
    },
    showLoading(loading){
      const { show, text } = loading;
      this.showLoadingDialog = show;
      this.loadingText = text;
    },
    showAlert({severity,message}){
      this.messages.push({
        severity: severity,
        message: message,
        id: this.alertId++
      })
      setTimeout(() => this.messages = [],3500);
    },
    checkInstalled() {
      if (!this.isInstalled) {
        this.$axios.get('/cfg/is-installed')
            .then(res => {
              const isInstalled = res.data?.isInstalled;
              this.isInstalled = isInstalled;
              if (!isInstalled){
                this.$router.push({ name : 'install-view'});
              }
            })
      }
    },
    checkIsDemoMode() {
      this.$axios.get('/cfg/is-demo')
          .then(res => {
            this.isDemo = res.data
          })
    },
    getVersion() {
      return JSON.parse(localStorage.getItem('version'))?.current;
    },
    ...mapActions(usePlaylistStore,['loadPlaylists']),
  },
  computed: {
    ...mapState(useUserStore,['isAuthenticated'])
  },
  beforeUnmount() {
    this.emitter.off('alert', this.showAlert);
    this.emitter.off('loading', this.showLoading);
    this.emitter.off('confirm', this.showConfirm);
  }
}
</script>
<style>
.p-toolbar{
  border: none !important;
  border-radius: 1rem;
  padding: 1rem 1rem 1rem 1.5rem
}
.p-message {
  z-index: 9999
}
.input-left-rounded {
  border-radius: 5px 0 0 5px;
}
.input-left-squared {
  border-radius: 0 5px 5px 0;
}
.input-right-squared {
  border-radius: 5px 0 0 0;
}
.fade-effect {
  animation: fadeInOut 3s infinite;
}
@keyframes fadeInOut {
  0% { opacity: .1; }
  50% { opacity: .6; }
  100% { opacity: .1; }
}
</style>