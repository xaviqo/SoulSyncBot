<template>
  <div class="w-full text-center p-5">
    <span class="text-2xl">S O U L  <b>S Y N C</b></span> v.2
  </div>
  <div class="w-full flex justify-content-center">
    <LoginCard></LoginCard>
  </div>
  <div v-if="isDemo" class="w-full flex justify-content-center flex-wrap mt-6 gap-6">
    <div class="w-full flex justify-content-center">
      <span class="text-lg text-gray-400">This is a demo of SoulSync. For more information:</span>
    </div>
    <div class="w-full flex justify-content-center">
      <Button
          severity="secondary"
          label="GitHub Repository"
          size="large"
          type="button"
          icon="pi pi-github"
          class="shadow-1 mr-4"
          @click="goTo('github')"
      />
      <Button
          severity="secondary"
          label="Online Documentation"
          size="large"
          type="button"
          icon="pi pi-book"
          class="shadow-1"
          @click="goTo('manual')"
      />
    </div>
  </div>
</template>
<script>
import LoginCard from "@/components/login/LoginCard.vue";

export default {
  name: "LoginView",
  components: {LoginCard},
  data: () =>  ({
    isDemo: false,
  }),
  mounted() {
    this.checkIsDemoMode()
  },
  methods: {
    checkIsDemoMode() {
      this.$axios.get('/cfg/is-demo')
          .then(res => {
            this.isDemo = res.data
          })
    },
    goTo(route) {
      switch (route) {
        case "manual": {
          window.open(`https://manual.soulsync.fyi/`, '_blank');
          break;
        }
        case "github": {
          window.open('https://github.com/xaviqo/SoulSyncBot', '_blank');
          break;
        }
      }
    }
  }
}
</script>
<style scoped>

</style>