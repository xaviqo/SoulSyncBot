<template>
  <Dialog
      v-model:visible="visible"
      closable
      :header="dialogHeader"
      class="w-12 md:w-10 lg:w-6"
      :pt="{
        root: 'border-none',
        mask: { style: 'backdrop-filter: blur(3px)' }
    }"
  >
    <DownloadListDataView
        :download-lists="downloadLists"
        show-glass
        :show-cog="false"
        :show-pause="false"
        :show-trash="false"
    />
  </Dialog>
</template>
<script>
import DownloadListDataView from "@/components/downloadlist/DownloadListDataView.vue";

export default {
  name: "SearchPolicyDownloadListsDialog",
  components: {DownloadListDataView},
  data: () => ({
    visible: false,
    dialogHeader: "",
    searchPolicy: null,
    downloadLists: []
  }),
  created() {
    this.emitter.on('policy-download-lists-dialog', this.handleDialogEvent);
  },
  methods: {
    handleDialogEvent(searchPolicy) {
      this.dialogHeader = `Search Policy '${searchPolicy?.name}' Download Lists`;
      this.fetchDownloadListsByPolicyId(searchPolicy?.id);
    },
    fetchDownloadListsByPolicyId(policyId) {
      this.downloadLists = [];
      this.$axios
          .get(`/download-list/by-policy/${policyId}`)
          .then(res => {
            const isEmpty = res.data.length === 0;
            if (isEmpty) {
              this.emitter.emit('alert',{
                severity: 'info',
                message: `There are no associated download lists`,
              });
              this.visible = false;
            } else {
              this.visible = true;
            }
            this.downloadLists = res.data
          });
    }
  },
  beforeUnmount() {
    this.emitter.off('policy-download-lists-dialog', this.handleDialogEvent);
  }
}
</script>