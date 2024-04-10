<template>
  <Toolbar>
    <template #center>
        <InputText
            class="min-w-0"
            type="text"
            variant="filled"
            v-model="playlistPayload.url"
        />
        <Button
            severity="success"
            label="Add Playlist"
            type="button"
            icon="pi pi-plus"
            class="shadow-1 ml-4"
            @click="submitPlaylist"
        />
    </template>
  </Toolbar>
</template>
<script>
export default {
  name: 'AddNewPlaylistCard',
  data: () =>  ({
    playlistPayload: {
      url: null
    }
  }),
  methods: {
    submitPlaylist() {
      this.emitter.emit('loading',{show: true, text: 'Loading playlist data...'});
      if (this.playlistPayload.url) {
        return this.$axios
            .post('/playlist', this.playlistPayload)
            .then( () => {
              this.emitter.emit('loading',{show: false});
              this.playlistPayload.url = null;
            })
      }
    }
  }
}
</script>
<style scoped>

</style>