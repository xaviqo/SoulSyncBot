<template>
  <div class="flex gap-3">
    <div class="w-2 flex flex-wrap gap-3">
      <PlaylistCover
          :playlist-cover="playlist.cover"
      />
      <PlaylistType
          :playlist-type="playlist.playlistType"
      />
    </div>
    <div class="w-10 flex flex-wrap gap-3">
      <PlaylistPanel
          class="w-12"
          :playlist-name="playlist.name"
      />
      <PlaylistSongsTable
          class="w-12"
          v-if="playlist.playlistType !== 'DISCOGRAPHY'"
          :playlist="playlist"
      />
      <PlaylistDiscography
          class="w-12"
          v-else
          :playlist-id="playlist.id"
      />
    </div>
  </div>
</template>
<script>
import PlaylistSongsTable from "@/components/playlist/PlaylistSongsTable.vue";
import PlaylistType from "@/components/playlist/PlaylistType.vue";
import PlaylistPanel from "@/components/playlist/PlaylistPanel.vue";
import PlaylistCover from "@/components/playlist/PlaylistCover.vue";
import PlaylistDiscography from "@/components/playlist/PlaylistDiscography.vue";

export default {
  name: "PlaylistView",
  components: {PlaylistDiscography, PlaylistCover, PlaylistPanel, PlaylistType, PlaylistSongsTable},
  data: () => ({
    playlist: {
      id:null,
      playlistType:null,
      cover:null,
      name:null,
      owner:null,
      totalTracks:0,
      lastUpdate:0,
      isUpdatable:false,
      shouldRenameRelocated:false
    }
  }),
  created() {
    this.playlist.id = this.$route.params.id;
    this.emitter.emit('loading',{show: true, text: 'Loading playlist data...'});
    this.fetchPlaylistData();
  },
  methods: {
    fetchPlaylistData() {
      this.$axios
          .get(`/playlist/${this.playlist.id}`)
          .then( (res) => {
            this.playlist = res.data;
            this.emitter.emit('loading',{show: false});
          })
          .catch( () => {
            this.emitter.emit('loading',{show: false});
            setTimeout(() => this.$router.push("/"), 1200);
          })
    },
  }
}
</script>
<style scoped>

</style>