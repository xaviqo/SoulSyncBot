<template>
  <Card>
    <template #content>
      <DataTable :value="songs"
                 paginator
                 :rows="table.size"
                 :totalRecords="table.totalElements"
                 :rowsPerPageOptions="[10, 15, 20, 30, 50]"
                 lazy
                 @page="onPage"
                 @update:rows="onUpdateRows"
                 size="small"

      >
        <Column field="name" header="Name"></Column>
        <Column field="album" header="Album"></Column>
      </DataTable>
    </template>
  </Card>
</template>
<script>

export default {
  name: "PlaylistSongsTable",
  data: () => ({
    songs: [],
    table: {
      page: 0,
      size: 15,
      totalPages: 0,
      totalElements: 0
    }
  }),
  props: {
    playlist: Object
  },
  mounted() {
    this.fetchPlaylistSongs();
  },
  methods: {
    fetchPlaylistSongs(){
      this.$axios
          .get(`/playlist/${this.playlistId}/songs`, {
            params: {
              page: this.table.page,
              size: this.table.size
            }
          })
          .then((res) => {
            this.songs = res.data.content;
            this.table.totalPages = res.data.totalPages;
            this.table.totalElements = res.data.totalElements;
          });
    },
    onPage(event) {
      this.table.page = event.page;
      this.table.size = event.rows;
      this.fetchPlaylistSongs();
    }
  }
}
</script>
<style scoped>

</style>