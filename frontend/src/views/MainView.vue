<template>
  <div class="flex flex-wrap gap-3">
    <AddNewPlaylistCard
        class="w-12"
    />
    <PlaylistCarousel
        class="w-12"
        :playlists="getPlaylists"
    />
  </div>
</template>
<script>
import AddNewPlaylistCard from "@/components/panel/AddNewPlaylistCard.vue";
import PlaylistCarousel from "@/components/shared/PlaylistCarousel.vue";
import {mapActions, mapState} from "pinia";
import {usePlaylistStore} from "@/store/playlist-calls";

export default {
  name: "MainView",
  components: {PlaylistCarousel, AddNewPlaylistCard},
  created() {
    this.loadPlaylists();
  },
  methods: {
    ...mapActions(usePlaylistStore,[
      'setSelectedPlaylist',
      'getSelectedPlaylist',
      'loadPlaylists'
    ])
  },
  computed: {
    ...mapState(usePlaylistStore,['getPlaylists'])
  },
}
</script>
<style scoped>

</style>