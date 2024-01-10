<template>
  <div class="w-full my-6 flex justify-content-center align-items-center">
    <Card class="border-1 border-black-alpha-90 shadow-2 p-3">
      <template #content>
        <div class="flex flex-column gap-4 mb-1">
          <div class="inline-flex flex-column gap-2">
            <label for="username" class="font-semibold">Username</label>
            <InputText v-model="loginPayload.username" id="username" class="p-3 border-1 border-black-alpha-90"></InputText>
          </div>
          <div class="inline-flex flex-column gap-2">
            <label for="password" class="font-semibold">Password</label>
            <InputText v-model="loginPayload.password" id="password" class="p-3 border-1 border-black-alpha-90" @keyup.enter="send" type="password"></InputText>
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
              class="border-1 border-black-alpha-90 shadow-1"
              @click="reset"
          />
          <Button
              severity="success"
              label="Login"
              type="button"
              icon="pi pi-check"
              class="border-1 border-black-alpha-90 shadow-1"
              @click="send"
          />
        </div>
      </template>
    </Card>
  </div>
  <div class="w-full" v-if="demoMode.isDemoMode">
    <Message
        class="border-1 border-black-alpha-90 shadow-2 "
        severity="info"
        icon="pi pi-info-circle"
        :closable="false"
    >
      <div class="m-1 lg:text-xl leading-6" style="line-height: 1.2;">
        <strong>Demo mode: </strong>ON (You can play around as much as you want!)
        <br>
        <strong class="mt-1">Credentials: </strong> admin / admin
        <br>
        <strong>Next restart:</strong> {{ demoMode.nextReset }} minutes
      </div>
    </Message>
  </div>
</template>
<script>
import {mapActions} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";

export default {
  name: "LoginView",
  data: () => ({
    loginPayload: {
      user: null,
      pass: null
    },
    demoMode: {
      isDemoMode: true,
      nextReset: 0
    }
  }),
  created() {
    this.isDemoMode();
  },
  methods: {
    reset(){
      this.loginPayload = {
        username: null,
        password: null
      };
    },
    isDemoMode(){
      this.axios.get('/configuration/demo-enabled')
          .then( res => this.demoMode = res.data );
    },
    send(){
      this.axios.post('/login',this.loginPayload)
          .then( (res) => {
            this.emitter.emit('show-alert', {
              info: 'You have successfully logged in',
              icon: 'pi-exclamation-circle',
              severity: 'success'
            })
            this.setToken(res.data.token)
            this.reset();
            this.$router.push("/")
          })
          .catch( (err) => {
            console.error(err)
            if (err.code == 'ERR_NETWORK') {
              this.emitter.emit('show-alert', {
                info: 'No connection with SoulSync API',
                icon: 'pi-check',
                severity: 'error'
              })
            } else {
              this.emitter.emit('show-alert', {
                info: err.response.data.message,
                icon: 'pi-check',
                severity: 'error'
              })
            }

          });
    },
    ...mapActions(useUserCfgStore, [
      'setToken',
    ])
  }
}
</script>
<style scoped>

</style>