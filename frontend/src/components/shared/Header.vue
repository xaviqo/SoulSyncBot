<template>
  <header class="w-full flex justify-content-center mb-6">
    <Toolbar class="w-full">
      <template #start>
        <div class="w-full mr-4">
          <span>S O U L  <b>S Y N C</b></span>
        </div>
        <div class="flex align-items-center gap-2">
          <Button label="Files" text plain />
          <Button label="Edit" text plain />
          <Button label="View" text plain />
        </div>
      </template>
      <template #end>
        <Button
            icon="pi pi-angle-right"
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
    }
  }
}
</script>
<style scoped>

</style>