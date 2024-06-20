<template>
  <PlaylistBody
      :playlist="getCurrentPlaylist"
      :timer="getLastCurrentRefresh"
      :timer-threshold="getRefreshRateThreshold"
  >
    <template v-slot:content>
     <DownloadListDataView
          class="w-12"
          :download-lists="getCurrentDownloadList"
          :playlist-id="getCurrentPlaylist?.id"
          :show-glass="false"
      />
      <DownloadListSongTable
          class="w-12"
          :download-list="getCurrentDownloadList[0]"
      />
    </template>
  </PlaylistBody>
</template>
<script>
import {mapActions, mapState} from "pinia";
import {usePlaylistStore} from "@/store/playlist-calls";
import PlaylistBody from "@/components/shared/PlaylistBody.vue";
import DownloadListDataView from "@/components/downloadlist/DownloadListDataView.vue";
import DownloadListSongTable from "@/components/downloadlist/DownloadListSongTable.vue";

export default {
  name: "DownloadListView",
  components: {
    DownloadListSongTable,
    DownloadListDataView,
    PlaylistBody
  },
  mounted() {
    const playlistId = this.$route.params.pl;
    const downloadListId = this.$route.params.dl;
    if (playlistId && downloadListId) {
      this.loadPlaylistData(playlistId,downloadListId);
    }
  },
  created() {
    this.timer = setInterval(this.updateTimer, 1000);
  },
  methods: {
    async loadPlaylistData(playlistId,downloadListId) {
      await this.fetchCurrentPlaylist(playlistId);
      await this.fetchPlaylistDownloadLists(playlistId);
      this.setCurrentDownloadList(downloadListId);
    },
    ...mapActions(usePlaylistStore, [
      'fetchCurrentPlaylist',
      'fetchPlaylistDownloadLists',
      'setCurrentDownloadList',
      'updateTimer'
    ])
  },
  computed: {
    ...mapState(usePlaylistStore, {
      getCurrentPlaylist: 'getCurrentPlaylist',
      getCurrentDownloadList: 'getCurrentDownloadList',
      getLastCurrentRefresh: 'getLastCurrentRefresh',
      getRefreshRateThreshold: 'getRefreshRateThreshold'
    })
  },
  unmounted() {
    if (this.timer) clearInterval(this.timer);
  }
}
</script>