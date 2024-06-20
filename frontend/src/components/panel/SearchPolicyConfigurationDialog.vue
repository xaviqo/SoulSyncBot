<template>
  <Dialog
      v-model:visible="visible"
      closable
      :header="`Search Policy Configuration`"
      class="w-12 md:w-10 lg:w-6"
      :pt="{
        root: 'border-none',
        mask: { style: 'backdrop-filter: blur(3px)' }
    }"
  >
    <ConfigurationFields :fields="fields" :disabled="false"/>
    <template #footer>
      <div class="w-full flex justify-content-between gap-3">
        <Button
            label="Close"
            severity="secondary"
            class="w-full text-primary-50 border-1 border-white-alpha-30"
            @click="visible = false"
        ></Button>
        <Button
            label="Delete"
            severity="danger"
            class="w-full text-primary-50 border-1 border-white-alpha-30"
            :disabled="policyId == null"
            @click="deletePolicy()"
        ></Button>
        <Button
            :label="selectedPolicy ? 'Delete' : 'Reset'"
            :severity="selectedPolicy ? 'danger' : 'warning'"
            class="w-full text-primary-50 border-1 border-white-alpha-30"
            @click="resetFields()"
        ></Button>
        <Button
            label="Save"
            security="success"
            class="w-full border-1 border-white-alpha-30 hover:bg-green-alpha-10"
            @click="saveConfiguration()"
        ></Button>
      </div>
    </template>
  </Dialog>
</template>
<script>

import ConfigurationFields from "@/components/shared/ConfigurationFields.vue";

export default {
  name: "SearchPolicyConfigurationDialog",
  components: {ConfigurationFields},
  data: () => ({
    visible: false,
    policyName: null,
    policyId: null,
    inputStrategyNames: [],
    fields: [],
    selectedPolicy: null,
  }),
  created() {
    this.emitter.on('policies-dialog', policyId => {
      this.visible = true;
      this.policyName = null;
      this.policyId = policyId;
      this.fetchFields(policyId);
    });
    this.emitter.on('delete-policy', isDelete => {
      if (isDelete) {
        this.deletePolicy(true);
        this.emitter.emit('fetch-policies');
      }
    })
  },
  methods: {
    fetchPolicyConfiguration(policyId, fields) {
      this.$axios
          .get(`/playlist/search-policy/${policyId}`)
          .then((res) => {
            const policyConfiguration = res.data;
            this.policyName = policyConfiguration.name;
            delete policyConfiguration.id
            Object.keys(policyConfiguration)
                .forEach(fieldName => {
                  fields.find(f => {
                    return f.objectFieldName == fieldName
                  }).value = policyConfiguration[fieldName];
                })
            this.fields = fields;
          });
    },
    fetchFields(policyId) {
      this.$axios
          .get('/cfg/fields', {params: {sections: 'search_policy'}})
          .then(res => {
            if (policyId != null) {
              this.fetchPolicyConfiguration(policyId, res.data);
            } else {
              this.fields = res.data
            }
          });
    },
    saveConfiguration() {
      const payload = this.getPolicyCfgPayload();
      if (payload !== null) {
        this.emitter.emit('loading', {show: true, text: 'Saving search policy...'});
        this.$axios
            .post('/playlist/search-policy', payload)
            .then(() => {
              this.emitter.emit('fetch-policies');
              this.emitter.emit('loading', {show: false});
              this.fields = this.configurationFields;
              this.visible = false;
            })
      }
    },
    getPolicyCfgPayload() {
      const policyCfgPayload = this.fields.reduce((payload, curr) => {
        if (!this.isValueValid(curr.value)) {
          this.emitter.emit('alert', {
            severity: 'warn',
            message: `Field ${curr.name} cannot be empty`
          });
        }
        payload[curr.objectFieldName] = curr.value;
        return payload;
      }, {});
      if (Object.values(policyCfgPayload).some(val => val === null)) {
        return null;
      }
      policyCfgPayload.id = this.policyId;
      return policyCfgPayload;
    },
    isValueValid(value) {
      return value !== null && value !== undefined && value !== '';
    },
    resetFields(){
      this.fields.forEach(field => field.value = null)
    },
    deletePolicy(isDelete){
      if (isDelete) {
        this.$axios
            .delete(`/playlist/search-policy/${this.policyId}`)
            .then(res => {
              if (res.status >= 200) {
                this.emitter.emit('alert', {
                  severity: 'info',
                  message: `Search policy ${this.policyName} deleted`
                });
                this.visible = false;
              }
            });
      } else {
        this.emitter.emit('confirm',{
          header: `Delete policy ${this.policyName}?`,
          message: 'Once deleted, it cannot be restored',
          reject: 'Cancel',
          accept: 'Delete',
          listenerLabel: 'delete-policy'
        })
      }
    }
  }
}
</script>
<style scoped>

</style>