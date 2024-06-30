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
      { label: 'Download', action: 'download', showWhenDownloadManager: false },
      { label: 'Show Stats', action: 'showStats', showWhenDownloadManager: false },
      { label: 'Force Update', action: 'forceUpdate', showWhenDownloadManager: true },
      { label: 'Delete', action: 'delete', showWhenDownloadManager: false }
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
<style scoped>

</style>