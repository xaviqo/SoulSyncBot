<template>
  <Card>
    <template #content>
      <DataTable :value="getPlaylistSongs.content"
                 paginator
                 :rows="getPlaylistSongs.size"
                 :totalRecords="getPlaylistSongs.totalElements"
                 :rowsPerPageOptions="[10, 15, 20, 30, 50]"
                 lazy
                 @page="onPage"
                 size="small"
      >
        <Column field="name" header="Name"></Column>
        <Column field="album" header="Album"></Column>
      </DataTable>
    </template>
  </Card>
</template>
<script>

import {mapActions, mapState} from "pinia";
import {usePlaylistStore} from "@/store/playlist-calls";

export default {
  name: "PlaylistSongsTable",
  props: {
    playlist: Object
  },
  created() {
    this.onPage(null);
  },
  watch: {
    playlist(newVal) {
      if (newVal && newVal.id)
        this.onPage(null);
    }
  },
  methods: {
    ...mapActions(usePlaylistStore, [
      'fetchPlaylistSongs',
    ]),
    async onPage(event) {
      await this.fetchPlaylistSongs(
          this.playlist?.id,
          event?.page ? event.page : 1,
          event?.size ? event.size : 10,
      );
    }
  },
  computed: {
    ...mapState(usePlaylistStore, {
      getPlaylistSongs: 'getPlaylistSongs',
    })
  }
}
</script>