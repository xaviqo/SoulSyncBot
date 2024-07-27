<template>
  <Dialog
      v-model:visible="visible"
      closable
      header="Search Policy Configuration"
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
    downloadLists: []
  }),
  created() {
    this.emitter.on('policies-dialog', this.handlePoliciesDialog);
    this.emitter.on('delete-policy', this.handleDeletePolicy);
    this.emitter.on('delete-policy-after-dls', this.handleDeletePolicyAfterDls);
  },
  methods: {
    handlePoliciesDialog(policyId) {
      this.visible = true;
      this.policyName = null;
      this.policyId = policyId;
      this.fetchFields(policyId);
    },
    handleDeletePolicy(isDelete) {
      if (isDelete) {
        this.deletePolicy(true);
        this.emitter.emit('fetch-policies');
      }
    },
    handleDeletePolicyAfterDls(isDelete) {
      if (isDelete) this.deletePolicyDownloadLists();
      this.visible = false;
    },
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
              this.resetFields();
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
              this.emitter.emit('alert', {
                severity: 'success',
                message: `Search policy successfully saved`
              });
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
      this.fields.forEach( f => {
        if (f.dataType === 'BOOLEAN') f.value = false;
        else f.value = null;
      });
    },
    deletePolicy(isDelete){
      if (isDelete) {
        this.$axios
            .get(`/download-list/by-policy/${this.policyId}`)
            .then( res => {
              this.downloadLists = res.data;
              const isEmpty = this.downloadLists?.length === 0;
              if (isEmpty) {
                this.$axios
                    .delete(`/playlist/search-policy/${this.policyId}`)
                    .then(res => {
                      if (res.status >= 200) {
                        this.emitter.emit('alert', {
                          severity: 'info',
                          message: `Search policy ${this.policyName} deleted`
                        });
                        this.visible = false;
                        this.emitter.emit('fetch-policies')
                      }
                    });
              } else {
                this.emitter.emit('confirm',{
                  header: ` Delete download lists using this policy?`,
                  message: `Deleting this search policy will delete the (${res.data?.length}) download lists that have it configured. Do you want to continue?`,
                  reject: 'Cancel',
                  accept: 'Delete',
                  listenerLabel: 'delete-policy-after-dls'
                });
              }
            });
      } else {
        this.emitter.emit('confirm',{
          header: `Delete policy ${this.policyName}?`,
          message: 'Once deleted, it cannot be restored',
          reject: 'Cancel',
          accept: 'Delete',
          listenerLabel: 'delete-policy'
        });
      }
    },
    async deletePolicyDownloadLists() {
      this.emitter.emit('loading', { show: true, text: 'Deleting policy and download lists...' });

      if (this.downloadLists) {
        const deletePromises = this.downloadLists.map(dl =>
            this.$axios.delete(`/download-list/${dl.id}`)
        );
        await Promise.all(deletePromises);
      }
      this.deletePolicy(true);
      this.emitter.emit('fetch-policies');
      this.emitter.emit('loading', { show: false });
    },
  },
  beforeUnmount() {
    this.emitter.off('policies-dialog', this.handlePoliciesDialog);
    this.emitter.off('delete-policy', this.handleDeletePolicy);
    this.emitter.off('delete-policy-after-dls', this.handleDeletePolicyAfterDls);
  }
}
</script>