<template>
  <Dialog
      v-model:visible="visible"
      class="p-1 col-12 lg:col-10 xl:col-8"
      :header="`Create new download list for playlist ${playlist?.name}`"
      closable
      :pt="{
        root: 'border-none',
        mask: { style: 'backdrop-filter: blur(3px)' }
    }"
  >
    <div class="w-full flex justify-content-around">
      <div class="flex justify-content-center flex-column gap-3 p-3 border-round" :class="mode == 0 ? 'bg-black-alpha-10' : ''">
        <div>
          Create new policy
        </div>
        <Button
            icon="pi pi-plus"
            label="Create"
            severity="secondary"
            @click="createNewPolicy()"
        />
      </div>
      <div class="flex justify-content-center flex-column gap-3 p-3 border-round" :class="mode == 1 ? 'bg-black-alpha-10' : ''">
        <div class="flex align-items-center mr-2">
          Or use existing policy
        </div>
        <div>
          <InputGroup>
            <Dropdown
                :options="searchPolicies"
                optionLabel="name"
                v-model="selectedPolicy"
                @change="fetchPolicyConfiguration()"
            />
          </InputGroup>
        </div>
      </div>
    </div>
    <div class="w-full flex justify-content-center flex-column mt-3">
      <ConfigurationFields
          v-show="showFields"
          :fields="policyFields"
          :disabled="selectedPolicy != null"
      />
    </div>
    <div class="w-full flex justify-content-center flex-column mt-3">
      <Button
          label="Save Download list"
          severity="success"
          @click="saveDownloadList()"
          :disabled="isConfigurationReady()"
      />
    </div>
  </Dialog>
</template>
<script>
import ConfigurationFields from "@/components/shared/ConfigurationFields.vue";

export default {
  name: "DownloadListCreationDialog",
  components: {ConfigurationFields},
  data: () => ({
    mode: null,
    showFields: false,
    playlist: null,
    visible: false,
    searchPolicies: [],
    selectedPolicy: {},
    policyFields: []
  }),
  created() {
    this.fetchSearchPolicies();
    this.fetchFields();
    this.emitter.on('download-dialog', playlist => {
      this.visible = true;
      this.playlist = {
        name: playlist.name,
        id: playlist.id,
      };
    });
  },
  methods: {
    createNewPolicy(){
      this.mode = 0;
      this.selectedPolicy = null;
      this.policyFields.forEach( pol => pol.value = null );
      this.showFields = true;
    },
    fetchPolicyConfiguration() {
      this.mode = 1;
      this.policyFields = this.policyFields.map(field => {
        const matchingPolicyValue = this.selectedPolicy[field.objectFieldName];
        return {
          ...field,
          value: matchingPolicyValue !== undefined ? matchingPolicyValue : field.value
        };
      });
      this.showFields = true;
    },
    fetchSearchPolicies() {
      this.$axios
          .get('/playlist/search-policy')
          .then(res => this.searchPolicies = res.data);
    },
    fetchFields() {
      this.$axios
          .get('/cfg/fields', {params: {sections: 'search_policy'}})
          .then(res => this.policyFields = res.data);
    },
    isConfigurationReady() {
      return this.policyFields.every( f => f.value == null)
    },
    saveDownloadList() {
      this.emitter.emit('loading',{show: true, text: 'Saving new download list...'});
      this.$axios
          .post('/download-list', this.createDownloadListPayload())
          .then( () => {
            this.emitter.emit('loading',{show: false});
            this.emitter.emit('refresh');
          });
      this.createNewPolicy();
      this.showFields = false;
      this.visible = false;
    },
    createDownloadListPayload(){
      return {
        playlistId: this.playlist?.id,
        searchPolicy: this.selectedPolicy?.id,
        newSearchPolicy: this.getPolicyConfiguration(),
      }
    },
    getPolicyConfiguration(){
      return this.policyFields.reduce((acc, field) => {
        acc[field.objectFieldName] = field.value;
        return acc;
      }, {});
    }
  }
}
</script>