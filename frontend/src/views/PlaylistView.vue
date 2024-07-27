<template>
  <PlaylistBody
      :playlist="getCurrentPlaylist"
      :is-download-manager="false"
  >
    <template v-slot:content>
      <DownloadListDataView
          class="w-12"
          :download-lists="getPlaylistDownloadLists"
          :playlist-id="getCurrentPlaylist?.id"
          :show-glass="true"
          show-cog
          show-pause
          show-trash
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
  <DownloadListCreationDialog />
</template>
<script>
import PlaylistBody from "@/components/shared/PlaylistBody.vue";
import DownloadListDataView from "@/components/downloadlist/DownloadListDataView.vue";
import PlaylistSongsTable from "@/components/playlist/PlaylistSongsTable.vue";
import PlaylistDiscography from "@/components/playlist/PlaylistDiscography.vue";
import {usePlaylistStore} from "@/store/playlist-calls";
import {mapActions, mapState} from "pinia";
import DownloadListCreationDialog from "@/components/downloadlist/DownloadListCreationDialog.vue";

export default {
  name: "PlaylistView",
  components: {
    DownloadListCreationDialog,
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
  methods: {
    async loadPlaylistData(playlistId) {
      await this.fetchCurrentPlaylist(playlistId);
      await this.fetchPlaylistDownloadLists(playlistId);
    },
    ...mapActions(usePlaylistStore, [
        'fetchCurrentPlaylist',
        'fetchPlaylistDownloadLists'
    ])
  },
  computed: {
    ...mapState(usePlaylistStore, {
      getCurrentPlaylist: 'getCurrentPlaylist',
      getPlaylistDownloadLists: 'getPlaylistDownloadLists',
    })
  },
  created() {
    this.emitter.on('refresh', () =>
        this.fetchPlaylistDownloadLists(this.$route.params.id)
    );
    this.emitter.on('pause-downloadlist', id => {
      const dl = this.getPlaylistDownloadLists.find( dl => dl.id === id);
      dl.isActive = !dl.isActive;
    });
  }
}
</script>