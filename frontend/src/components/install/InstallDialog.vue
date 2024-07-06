<template>
  <Dialog
      v-model:visible="visible"
      modal
      class="w-10 md:w-4 xl:w-3 text-sm"
      :pt="{
        root: 'border-none',
        mask: { style: 'backdrop-filter: blur(3px)' }
    }"
  >
    <template #container>
      <Steps v-model:activeStep="activeStep" :model="steps" class="my-2 pt-4"/>
      <div class="flex m-4 p-4 justify-content-around bg-black-alpha-10 border-round-2xl">
        <div v-for="(value,api) in apiStatus" :key="api">
          <span :class="value ? 'text-primary-400' : 'text-red-400' "> {{ api.toUpperCase() }} </span>
          {{" --> " + (value ? '👌' : '👎') }}
        </div>
      </div>
      <div v-if="setupFields.length > 0" class="flex flex-column p-4 gap-4">
        <div
            v-for="field in setupFields"
            class="w-full inline-flex flex-column mb-3 gap-2"
            :key="field.fieldName"
        >
          <div class="w-full flex align-content-center">
            <i class="pi pi-chevron-right mr-2 text-white-alpha-40" style="font-size: 1rem"></i>
            <label :for="field.name" class="text-primary-50 font-semibold">{{ field.fieldName.toUpperCase() }}</label>
          </div>
          <InputText
              :id="field.name"
              class="w-full bg-white-alpha-20 border-none p-3 text-primary-50"
              v-model="field.value"
          ></InputText>
        </div>
        <div class="flex gap-4">
          <Button
              label="Reset"
              :disabled="!isInputFilled()"
              @click="resetFields()"
              severity="secondary"
              class="p-3 w-full text-primary-50 border-1 border-white-alpha-30 hover:bg-white-alpha-10"
          ></Button>
          <Button
              :label="isReady ? 'Finish' : 'Connect' "
              @click="install()"
              :severity="isReady ? 'success' : 'secondary'"
              :class="isReady ? 'text-black-alpha-80' : 'text-primary-50'"
              class="p-3 w-full border-1 border-white-alpha-30 hover:bg-green-alpha-10"
          ></Button>
        </div>
      </div>
    </template>
  </Dialog>
</template>
<script>
export default {
  name: "InstallDialog",
  data: () => ({
    visible: false,
    isReady: false,
    setupFields: [],
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
    activeStep: 0,
    apiStatus: {
      SPOTIFY: false,
      SLSKD: false
    }
  }),
  props: {
    showDialog: {
      required: true,
      type: Boolean
    }
  },
  created() {
    this.visible = this.showDialog;
  },
  methods: {
    install(){
      const payload = this.getPayloadAndCheckValues();
      if (payload.length === this.setupFields.length) {
        this.visible = false;
        this.$axios
            .post('/cfg/initial-setup',payload)
            .then( res => {
              this.setApiStatus(res.data);
              this.isReady = Object
                  .values(this.apiStatus)
                  .every( f => f === true );
              if (this.isReady) {
                this.emitter.emit('alert',{
                  severity: 'success',
                  message: `Setup completed! 🥳`
                });
                this.emitter.emit('loading',{show: false, text: null});
              } else {
                this.visible = true;
              }
            });
      }
    },
    setApiStatus(apiStatusResponse){
      Object.keys(apiStatusResponse).forEach(key => {
        const isConnected = apiStatusResponse[key];
        this.apiStatus[key] = isConnected; {
          if (!isConnected) {
            this.emitter.emit('alert',{
              severity: 'warn',
              message: `Connection to ${key} failed`
            });
          }
        }
      });
    },
    getPayloadAndCheckValues(){
      return this.setupFields
          .filter( sf => {
            if (sf.value == null || sf.value?.length < 1) {
              this.emitter.emit('alert',{
                severity: 'error',
                message: `${sf.fieldName.toUpperCase()} can not be empty`
              });
            } else {
              return sf;
            }
          });
    },
    getSetupFields(){
      this.$axios
          .get('/cfg/initial-setup')
          .then( res => {
            if (res) this.setupFields = res.data
          });
    },
    resetFields() {
      this.setupFields.forEach(field => field.value = null);
    },
    isInputFilled() {
      return this.setupFields?.some(field => {
        return field.value != null && field.value.trim() !== '';
      });
    }
  },
  watch: {
    showDialog(newVal,oldVal){
      if (newVal !== oldVal){
        this.getSetupFields();
        this.visible = newVal;
      }
    }
  }
}
</script>
<style scoped>

</style>