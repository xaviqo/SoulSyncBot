<template>
  <Card>
    <template #content>
      <div class="flex justify-content-around gap-3">
        <template v-for="btn in buttons">
          <Button
              v-if="showBtn(btn)"
              :key="btn.action"
              class="w-full"
              :label="btn.label"
              severity="secondary"
              @click="onAction(btn.action)"
          />
        </template>
      </div>
    </template>
  </Card>
</template>
<script>

export default {
  name: "PlaylistManagerButton",
  props: {
    playlistName: {
      type: String,
      required: true
    },
    playlistId: {
      type: String,
      required: true,
    },
    playlistType: {
      type: String,
      required: true,
    },
    isDownloadManager: {
      type: Boolean,
      required: true,
    },
  },
  data: () => ({
    buttons: [
      { label: 'Create Downloads', action: 'download', showWhenDownloadManager: false },
      { label: 'Force Update', action: 'forceUpdate', showWhenDownloadManager: true },
      { label: 'Delete Playlist', action: 'delete', showWhenDownloadManager: false }
    ]
  }),
  methods: {
    onAction(action) {
      switch (action) {
        case 'showStats':
          break;
        case 'forceUpdate':
          this.forceUpdate();
          break;
        case 'delete':
          this.$confirm.require({
            message: `Notice that deleting this playlist will also delete all the associated download lists`,
            header: `Delete playlist ${this.playlistName}`,
            icon: 'pi pi-exclamation-triangle',
            rejectLabel: 'Cancel',
            acceptLabel: 'Delete',
            accept: () => {
              this.emitter.emit('loading',{show: true, text: 'Deleting playlist...'});
              this.$axios
                  .delete(`/playlist/${this.playlistId}`)
                  .then( () => {
                    this.emitter.emit('loading',{show: false});
                    this.emitter.emit('refresh');
                    this.$router.push("/");
                  });
            }
          });
          break;
        case 'download':
          this.download();
          break;
      }
    },
    forceUpdate() {
      this.$axios
          .get(`/playlist/${this.playlistId}/force-update`);
      this.emitter.emit('refresh');
      this.emitter.emit('alert',{
        severity: 'success',
        message: `Playlist ${this.playlistName} updated successfully`,
      })
    },
    download(){
      this.emitter.emit('download-dialog', {
        id:this.playlistId,
        name:this.playlistName
      });
    },
    showBtn(btn){
      if (this.isDownloadManager)
        return btn.showWhenDownloadManager;
      return true;
    }
  }
}
</script>