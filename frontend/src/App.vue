<template>
  <div class="p-2">
    <Header v-if="isAuthenticated"></Header>
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
    <router-view/>
  </div>
  <InstallDialog
      :show-dialog="showInstallDialog"
  />
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
</template>
<script>
import Header from "@/components/shared/Header.vue";
import {mapActions, mapState} from "pinia";
import {useUserStore} from "@/store/user-calls";
import {useInitStore} from "@/store/init-calls";
import InstallDialog from "@/components/install/InstallDialog.vue";

export default {
  name: 'SoulSync',
  data: () => ({
    showInstallDialog: false,
    showLoadingDialog: false,
    loadingText: null,
    messages: [],
    count: 0
  }),
  components: {
    InstallDialog,
    Header
  },
  created() {
    this.isInstalled();
    this.emitter.on(
        'alert',
        alert => this.showAlert(alert)
    );
    this.emitter.on(
        'loading',
        loading => this.showLoading(loading)
    );
  },
  methods:{
    async isInstalled() {
      await useInitStore().checkInstall();
      const isInstalled = useInitStore().isInstalled;
      if (!isInstalled) {
        this.showInstallDialog = true;
      }
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
        id: this.count++
      })
      setTimeout(() => this.messages.pop(),4000);
    },
    ...mapActions(useUserStore,['checkAuthenticated'])
  },
  computed: {
    ...mapState(useUserStore,['isAuthenticated'])
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
</style>