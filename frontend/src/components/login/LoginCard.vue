<template>
  <div class="max-w-lg">
    <Card class="shadow-2 p-3">
      <template #content>
        <div class="flex flex-column gap-4 mb-1">
          <div class="inline-flex flex-column gap-2">
            <label for="username" class="font-semibold">Username</label>
            <InputText id="username" class="p-3" v-model="loginPayload.username"></InputText>
          </div>
          <div class="inline-flex flex-column gap-2">
            <label for="password" class="font-semibold">Password</label>
            <InputText id="password" class="p-3" type="password" v-model="loginPayload.password" @keydown.enter="submitLogin"></InputText>
          </div>
        </div>
      </template>
      <template #footer>
        <div class="flex justify-content-between">
          <Button
              severity="warning"
              label="Reset"
              type="button"
              icon="pi pi-times"
              class="shadow-1"
              @click="resetInputs"
          />
          <Button
              severity="success"
              label="Login"
              type="button"
              icon="pi pi-check"
              class="shadow-1"
              @click="submitLogin"
          />
        </div>
      </template>
    </Card>
  </div>
</template>
<script>
import {useUserStore} from "@/store/user-calls";
export default {
  name: 'LoginCard',
  computed: {
    userCalls: () => useUserStore()
  },
  data: () =>  ({
    loginPayload: {
      username: '',
      password: ''
    }
  }),
  methods: {
    submitLogin(){
      return this.$axios
          .post('/account/sign-in', this.loginPayload)
          .then(res => {
            this.userCalls.saveLoginResponse(res.data);
            this.$router.push({ name : 'panel-view'});
          })
          .catch( () => { this.resetInputs() });
    },
    resetInputs(){
      this.loginPayload = {
        username: '',
        password: ''
      };
    }
  }
}
</script>
<style scoped>

</style>