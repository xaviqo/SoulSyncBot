<template>
  <Card class="lg:w-8 sm:w-12">
    <template #content>
      <div class="grid -mt-1">
        <InputGroup class="md:col-4 col-12">
          <FloatLabel>
            <Dropdown
                :options="searchPolicies"
                optionLabel="name"
                placeholder=""
                v-model="inputs.searchPolicy"
                class="input-left-rounded"
            />
            <label
                v-if="!inputs.searchPolicy"
                for="url"
            >
              Select Download Policy
            </label>
          </FloatLabel>
          <Button
              :disabled="inputs.searchPolicy == null"
              severity="secondary"
              type="button"
              icon="pi pi-cog"
              class="shadow-1"
              @click="showPolicyDialog(true)"
          />
          <Button
              severity="secondary"
              type="button"
              icon="pi pi-plus"
              class="shadow-1"
              @click="showPolicyDialog(false)"
          />
        </InputGroup>
        <InputGroup class="md:col-8 col-12">
          <FloatLabel>
            <InputText
                id="url"
                type="text"
                variant="filled"
                v-model="inputs.url"
            />
            <label
                for="url"
                v-if="!inputs.url"
            >Playlist URL</label>
          </FloatLabel>
          <Button
              severity="success"
              type="button"
              icon="pi pi-download"
              class="shadow-1"
              :disabled="!inputs.url"
              @click="submitPlaylist"
          />
        </InputGroup>
      </div>
    </template>
  </Card>
  <SearchPolicyConfigurationDialog />
</template>
<script>
import SearchPolicyConfigurationDialog from "@/components/panel/SearchPolicyConfigurationDialog.vue";
import {request} from "axios";

export default {
  name: 'AddNewPlaylistCard',
  components: {SearchPolicyConfigurationDialog},
  data: () =>  ({
    searchPolicies: [],
    inputs: { url: null, searchPolicy: null }
  }),
  created() {
    //this.inputs.searchPolicy = ;
    this.fetchSearchPolicies();
    this.emitter.on('fetch-policies', () => {
      this.fetchSearchPolicies();
    });
  },
  methods: {
    request,
    submitPlaylist() {
      if (this.isPlaylistRequestOk()) {
        this.emitter.emit('loading',{show: true, text: 'Loading playlist data...'});
        return this.$axios
            .post('/playlist', {
              url : this.inputs.url,
              searchPolicy : this.inputs.searchPolicy?.id
            })
            .then( () => {
              this.emitter.emit('loading',{show: false});
              this.inputs.url = null;
            })
      }
    },
    fetchSearchPolicies() {
      this.$axios
          .get('/playlist/search-policy')
          .then(res => this.searchPolicies = res.data);
    },
    showPolicyDialog(isEditPolicy) {
      const policyId = isEditPolicy
          ? this.inputs.searchPolicy?.id
          : null
      this.emitter.emit('policies-dialog',policyId);
    },
    isPlaylistRequestOk(){
      if (this.inputs.searchPolicy == null) {
        this.emitter.emit('alert',{
          severity: 'warn',
          message: 'You must select a download policy'
        });
        return false;
      }
      if (!this.isUrl(this.inputs.url)) {
        this.emitter.emit('alert',{
          severity: 'warn',
          message: 'You must provide a valid URL'
        });
        return false;
      }
      return true;
    },
    isUrl(str) {
      try {
        new URL(str);
        return true;
      } catch (e) {
        return false;
      }
    }
  },
}
</script>
<style scoped>

</style>