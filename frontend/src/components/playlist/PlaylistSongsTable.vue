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
        <Column header="Artists">
          <template #body="slotProps">
            {{ getArtists(slotProps.data.artists) }}
          </template>
        </Column>
        <Column field="album" header="Album"></Column>
      </DataTable>
    </template>
  </Card>
</template>
<script>
import {mapActions, mapState} from "pinia";
import {usePlaylistStore} from "@/store/playlist-calls";
import {utilsMixin} from "@/mixin/utils";

export default {
  name: "PlaylistSongsTable",
  mixins: [utilsMixin],
  data: () => ({
    page: 0,
    rows: 10
  }),
  props: {
    playlist: Object,
  },
  created() {
    this.clearSongs();
    this.onPage(null);
    this.emitter.on('refresh', this.handleRefresh);
  },
  watch: {
    playlist(newVal) {
      if (newVal && newVal.id)
        this.onPage(null);
    }
  },
  methods: {
    ...mapActions(usePlaylistStore, {
      fetchPlaylistSongs: 'fetchPlaylistSongs',
      clearSongs: 'clearSongs'
    }),
    handleRefresh() {
      this.onPage(null);
    },
    getArtists(artists){
      return this.getByComa(artists);
    },
    async onPage(event) {
      if (event) {
        this.page = event.page;
        this.rows = event.rows;
      }
      await this.fetchPlaylistSongs(
          this.playlist?.id,
          this.page,
          this.rows,
      );
    }
  },
  computed: {
    ...mapState(usePlaylistStore, {
      getPlaylistSongs: 'getPlaylistSongs',
    })
  },
  beforeUnmount() {
    this.emitter.off('refresh', this.handleRefresh);
  }
}
</script>