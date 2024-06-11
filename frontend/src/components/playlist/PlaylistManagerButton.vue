<template>
  <Card>
    <template #content>
      <div class="flex justify-content-around gap-3">
        <Button
            v-for="btn in buttons"
            :key="btn.action"
            class="w-full"
            :label="btn.label"
            severity="secondary"
            @click="emitAction(isDownloadManager)"
        />
      </div>
    </template>
  </Card>
</template>
<script>
export default {
  name: "PlaylistManagerButton",
  props: {
    playlistType: String,
    // Force Update - Delete Playlist - Create Download - Show Stats
  },
  data: () => ({
    isDownloadManager: false,
    buttons: [
      { label: 'Download', action: 'downloadPlaylist' },
      { label: 'Show Stats', action: 'showStats' },
      { label: 'Force Update', action: 'forceUpdate' },
      { label: 'Delete', action: 'deletePlaylist' }
    ]
  }),
  methods: {
    emitAction(isDownloadManager) {
      this.isDownloadManager = !isDownloadManager;
      this.$emit(
          'manager',
          this.isDownloadManager ? 'playlist' : 'downloads'
      );
    }
  }
}
</script>
<style scoped>

</style>