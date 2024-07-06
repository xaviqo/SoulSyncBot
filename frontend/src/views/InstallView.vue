<template>
  <div class="w-6 lg:w-12 flex justify-content-center align-items-center" style="height: 85vh">
    <Card class="w-full p-5 bg-gray-900 shadow-5">
      <template #header>
        <div class="border-round-lg">
          <Steps
              :model="steps"
              v-model:activeStep="activeStep"
              class="w-full mt-4 rounded-xl"
          />
        </div>
      </template>
      <template #content>
        <div class="flex flex-column justify-content-around">
          <div class="flex justify-content-center p-4 border-round-lg">
            <div v-if="activeStep === 0" class="w-8 flex gap-4">
              <Button
                  class="w-6 p-3"
                  label="Go to installation manual"
              />
              <Button
                  class="w-6 p-3"
                  label="Go to installation video"
              />
            </div>
            <div v-else-if="activeStep === 1">
              <div class="w-full flex flex-wrap align-items-start justify-content-evenly gap-8">
                <div v-for="(value,api) in apiStatus" :key="api" class="p-3 bg-black-alpha-10 border-round-2xl">
                  <span :class="value ? 'text-primary-400' : 'text-red-400' " > {{ api.toUpperCase() }} </span>
                  {{" --> " + (value ? '👌' : '👎') }}
                </div>
              </div>
              <div class="mt-5">
                <ConfigurationFields
                    :fields="setupFields"
                    :disabled="false"
                />
              </div>
            </div>
            <div v-else-if="activeStep === 2">
              <div>

              </div>
              <div class="w-full flex justify-content-center gap-4">
                <div class="flex flex-column gap-2">
                  <label for="user">Admin Username</label>
                  <InputText
                      id="user"
                      v-model="adminCredentials.username"
                  />
                </div>
                <div class="flex flex-column gap-2">
                  <label for="pass">Admin Password</label>
                  <InputText
                      id="pass"
                      v-model="adminCredentials.password"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <div class="gap-4 flex justify-content-evenly align-items-end my-3">
          <Button
              label="Back"
              :disabled="activeStep === 0"
              @click="back()"
              severity="secondary"
              class="w-full p-2 text-primary-50 border-1 border-white-alpha-30 hover:bg-white-alpha-10"
          ></Button>
          <Button
              v-if="activeStep === 1"
              label="Reset Fields"
              :disabled="!areFieldsFilled()"
              @click="resetFields()"
              severity="secondary"
              class="w-full p-2 text-primary-50 border-1 border-white-alpha-30 hover:bg-white-alpha-10"
          ></Button>
          <Button
              v-if="activeStep === 1"
              label="Check Connection"
              :disabled="!areFieldsFilled()"
              @click="saveInitialSetupFields()"
              severity="secondary"
              class="w-full p-2 text-primary-50 border-1 border-white-alpha-30 hover:bg-white-alpha-10"
          ></Button>
          <Button
              :label="activeStep === 2 ? 'Finish' : 'Next' "
              @click="next()"
              :severity="'secondary'"
              class="w-full p-2 border-1 border-white-alpha-30 hover:bg-green-alpha-10 hover:bg-white-alpha-10"
          ></Button>
        </div>
      </template>
    </Card>
  </div>
</template>
<script>
import ConfigurationFields from "@/components/shared/ConfigurationFields.vue";
import {utilsMixin} from "@/mixin/utils";

export default {
  name: "InstallView",
  mixins: [utilsMixin],
  components: {ConfigurationFields},
  data: () => ({
    activeStep: 0,
    steps: [
      {
        label: 'Setup Guide'
      },
      {
        label: 'APIs Configuration'
      },
      {
        label: 'Create Admin'
      }
    ],
    setupFields: [],
    adminCredentials: {
      username: null,
      password: null,
    },
    apiStatus: {
      SPOTIFY: false,
      SLSKD: false
    }
  }),
  methods: {
    saveAccountAndFinish() {
      this.emitter.emit('loading',{show: true, text: 'Completing setup...'});
      this.$axios
          .post('/cfg/initial-setup/admin',this.adminCredentials)
          .then( res => {
            if (res.status === 201) {
              this.emitter.emit('alert',{
                severity: 'success',
                message: `Setup completed! 🥳`
              });
              this.$router.push({ name : 'login-view'});
            }
            this.emitter.emit('loading',{show: false, text: null});
          });
    },
    saveInitialSetupFields() {
      this.emitter.emit('loading',{show: true, text: 'Verifying configuration...'});
      this.$axios
          .post('/cfg/initial-setup/apis',this.setupFields)
          .then( res => {
            if (res.status === 201) {
              this.setApiStatus(res.data);
            }
            this.emitter.emit('loading',{show: false, text: null});
          });
    },
    fetchInitialSetupFields() {
      this.setupFields = []
      this.$axios
          .get('/cfg/initial-setup')
          .then( res => {
            if (res) {
              res.data?.fields?.forEach( f => {
                f.fieldName = this.capitalizeFirstLetter(f.fieldName);
                if (f.defaultValues)
                  f.value = f.defaultValues;
                this.setupFields.push(f);
              });
              this.adminCredentials = res.data?.admin;
            }
          });
    },
    setApiStatus(apiStatusResponse){
      Object.keys(apiStatusResponse).forEach(key => {
        const isConnected = apiStatusResponse[key];
        this.apiStatus[key] = isConnected; {
          if (isConnected) {
            this.emitter.emit('alert',{
              severity: 'success',
              message: `Connection to ${key} is successful`
            });
          } else {
            this.emitter.emit('alert',{
              severity: 'warn',
              message: `Connection to ${key} failed`
            });
          }
        }
      });
    },
    resetFields() {
      this.setupFields.forEach(field => field.value = null);
    },
    areFieldsFilled() {
      return this.setupFields?.some(field => {
        return field.value != null && field.value.trim() !== '';
      });
    },
    back() {
      this.activeStep--;
      if (this.setupFields.length < 1 || !this.areFieldsFilled()) {
        this.fetchInitialSetupFields();
      }
    },
    next() {
      this.activeStep++;
      if (this.activeStep === 1)
        this.fetchInitialSetupFields();
      else if (this.activeStep === 2)
        this.saveInitialSetupFields();
      else if (this.activeStep === 3)
        this.saveAccountAndFinish();
    },
    isReady() {
      if (this.activeStep === 0)
        return true
      else if (this.activeStep === 1)
        return this.apiStatus['SLSKD'];
    }
  },
  created() {
    this.$axios.get('/cfg/is-installed')
        .then(res => {
          if (res.data?.isInstalled){
            this.emitter.emit('alert',{
              severity: 'success',
              message: `SoulSync is already configured`
            });
            this.$router.push({ name : 'login-view'});
          }
        })
  }
}
</script>