<template>
  <div class="w-full flex justify-content-around gap-4 p-3">
    <DataTable :value="searchPolicies" size="large">
      <Column field="name" header="Name"></Column>
      <Column field="fileFormatsByComa" header="Formats"></Column>
      <Column field="inputStrategy" header="Input Strat."></Column>
      <Column field="downloadPriority" header="Priority">
        <template #body="slotProps">
          <div
              class="border-round text-center font-bold p-1"
              :style="{ backgroundColor: PriorityColor[slotProps.data?.downloadPriority] }"
          >
            {{ slotProps.data?.downloadPriority ?? 'N/A' }}
          </div>
        </template>
      </Column>
      <Column header="Actions" class="flex justify-content-center gap-3">
        <template #body="slotProps">
          <Button
              size="small"
              severity="secondary"
              type="button"
              icon="pi pi-search"
              class="shadow-1"
              @click="showDownloadListsDialog(slotProps.data)"
          />
          <Button
              size="small"
              severity="secondary"
              type="button"
              icon="pi pi-cog"
              class="shadow-1"
              @click="showPolicyDialog(slotProps.data.id)"
          />
        </template>
      </Column>
    </DataTable>
  </div>
  <div class="w-full flex justify-content-center my-3">
    <Button
        severity="secondary"
        label="Create new search policy"
        icon="pi pi-plus"
        size="small"
        @click="showPolicyDialog(null)"
    />
  </div>
  <SearchPolicyConfigurationDialog />
  <SearchPolicyDownloadListsDialog />
</template>
<script>
import PriorityColor from "@/model/PriorityColor";
import SearchPolicyConfigurationDialog from "@/components/panel/SearchPolicyConfigurationDialog.vue";
import SearchPolicyDownloadListsDialog from "@/components/downloadlist/SearchPolicyDownloadListsDialog.vue";

export default {
  name: "SearchPolicyConfiguration",
  components: {SearchPolicyDownloadListsDialog, SearchPolicyConfigurationDialog},
  computed: {
    PriorityColor() {
      return PriorityColor
    }
  },
  data: () => ({
    searchPolicies: []
  }),
  methods: {
    fetchSearchPolicies() {
      this.$axios
          .get('/playlist/search-policy')
          .then(res => this.searchPolicies = res.data);
    },
    showPolicyDialog(policyId) {
      this.emitter.emit('policies-dialog',policyId);
    },
    showDownloadListsDialog(searchPolicy) {
      this.emitter.emit('policy-download-lists-dialog',searchPolicy);
    }
  },
  created() {
    this.fetchSearchPolicies();
    this.emitter.on('fetch-policies', () => this.fetchSearchPolicies());
  },
  beforeUnmount() {
    this.emitter.off('fetch-policies', this.fetchSearchPolicies);
  },
}
</script>