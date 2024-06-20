<template>
  <PlaylistBody
      :playlist="getCurrentPlaylist"
      :timer="getLastCurrentRefresh"
      :timer-threshold="getRefreshRateThreshold"
  >
    <template v-slot:content>
      <DownloadListDataView
          class="w-12"
          :download-lists="getPlaylistDownloadLists"
          :playlist-id="getCurrentPlaylist?.id"
          :show-glass="true"
      />
      <PlaylistSongsTable
          v-if="!getCurrentPlaylist.playlistType || getCurrentPlaylist.playlistType !== 'DISCOGRAPHY'"
          class="w-12"
          :playlist="getCurrentPlaylist"
      />
      <PlaylistDiscography
          v-else
          class="w-12"
          :playlist-id="getCurrentPlaylist?.id"
      />
    </template>
  </PlaylistBody>
</template>
<script>
import PlaylistBody from "@/components/shared/PlaylistBody.vue";
import DownloadListDataView from "@/components/downloadlist/DownloadListDataView.vue";
import PlaylistSongsTable from "@/components/playlist/PlaylistSongsTable.vue";
import PlaylistDiscography from "@/components/playlist/PlaylistDiscography.vue";
import {usePlaylistStore} from "@/store/playlist-calls";
import {mapActions, mapState} from "pinia";

export default {
  name: "PlaylistView",
  data: () => ({
    timer: null
  }),
  components: {
    PlaylistDiscography,
    PlaylistSongsTable,
    DownloadListDataView,
    PlaylistBody
  },
  mounted() {
    const playlistId = this.$route.params.id;
    if (playlistId)
      this.loadPlaylistData(playlistId);
  },
  created() {
    this.timer = setInterval(this.updateTimer, 1000);
  },
  methods: {
    async loadPlaylistData(playlistId) {
      await this.fetchCurrentPlaylist(playlistId);
      await this.fetchPlaylistDownloadLists(playlistId);
    },
    ...mapActions(usePlaylistStore, [
        'fetchCurrentPlaylist',
        'fetchPlaylistDownloadLists',
        'updateTimer'
    ])
  },
  computed: {
    ...mapState(usePlaylistStore, {
      getCurrentPlaylist: 'getCurrentPlaylist',
      getPlaylistDownloadLists: 'getPlaylistDownloadLists',
      getLastCurrentRefresh: 'getLastCurrentRefresh',
      getRefreshRateThreshold: 'getRefreshRateThreshold'
    })
  },
  unmounted() {
    if (this.timer) clearInterval(this.timer);
  }
}
</script>