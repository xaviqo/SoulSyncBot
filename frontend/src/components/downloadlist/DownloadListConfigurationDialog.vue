<template>
  <Dialog
      v-model:visible="visible"
      class="p-1 col-12 lg:col-10 xl:col-8"

      closable
      :pt="{
        root: 'border-none',
        mask: { style: 'backdrop-filter: blur(3px)' }
    }"
  >
    <template #header>
      <div class="w-full flex justify-content-start text-xl my-3">
        <div class="w-full flex align-items-center justify-content-between">
          Search Policy Configuration
        </div>
        <div class="w-full flex  justify-content-end mr-4">
          <div class="flex align-items-center mr-2">
            Current Policy
          </div>
          <div>
            <InputGroup>
              <Dropdown
                  :options="searchPolicies"
                  optionLabel="name"
                  class="input-right-squared"
                  v-model="selectedPolicy"
              />
              <Button
                  class="input-left-squared"
                  icon="pi pi-check"
                  severity="success"
                  v-tooltip="{ value: 'Confirm change of search policy for this download list', showDelay:20, hideDelay:50 }"
                  :disabled="selectedPolicy === vModelPolicy"
              />
            </InputGroup>
          </div>
        </div>
      </div>
    </template>
    <ConfigurationFields :fields="policyFields" :disabled="true"/>
  </Dialog>
</template>
<script>
import ConfigurationFields from "@/components/shared/ConfigurationFields.vue";

export default {
  name: "DownloadListConfigurationDialog",
  components: {ConfigurationFields},
  data: () => ({
    visible: false,
    downloadListData: null,
    searchPolicies: [],
    selectedPolicy: null,
    vModelPolicy: null,
    policyFields: []
  }),
  created() {
    this.emitter.on('download-list-data-dialog', downloadListData => {
      this.visible = true;
      this.fetchFields(downloadListData.policyId);
      this.fetchSearchPolicies(downloadListData.policyId);
    });
  },
  methods: {
    fetchFields(policyId) {
      this.$axios
          .get('/cfg/fields', {params: {sections: 'search_policy'}})
          .then(res => {
            this.fetchPolicyConfiguration(policyId, res.data);
          });
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
            this.policyFields = fields;

          });
    },
    fetchSearchPolicies(policyId) {
      this.$axios
          .get('/playlist/search-policy')
          .then(res => {
            this.searchPolicies = res.data;
            this.selectedPolicy = this.searchPolicies.find(policy =>
                policy.id === policyId
            );
            this.vModelPolicy = this.selectedPolicy;
          });
    },
  }
}
</script>