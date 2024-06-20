<template>
  <header class="flex justify-content-center">
    <Toolbar class="w-full p-4">
      <template #start>
        <div class="w-full mr-4 cursor-pointer" @click="goTo('panel-view')">
          <span>S O U L  <b>S Y N C</b></span>
        </div>
        <div class="flex align-items-center gap-2">
          <Button label="Playlists" text plain @click="goTo('panel-view')" />
          <Button label="Configuration" text plain @click="goTo('configuration-view')" />
          <Button label="Statistics" text plain @click="goTo('stats-view')" />
          <Button label="Github" text plain @click="goToRepo()" />
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
      this.$router.push("/")
    },
    goTo(name) {
      this.$router.push({ name })
    },
    goToRepo(){
      window.open('https://github.com/xaviqo/SoulSyncBot', '_blank');
    }
  }
}
</script>
<style scoped>

</style>