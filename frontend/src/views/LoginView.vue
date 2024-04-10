<template>
  <div class="w-full text-center p-5">
    <span class="text-2xl">S O U L  <b>S Y N C</b></span> v.2
  </div>
  <div class="w-full flex justify-content-center">
    <LoginCard></LoginCard>
  </div>
</template>
<script>
import LoginCard from "@/components/login/LoginCard.vue";

export default {
  name: "LoginView",
  components: {LoginCard},
  data: () =>  ({
  })
}
</script>
<style scoped>

</style>