<template>
  <Card class="w-full">
    <template #content>
      <DataTable :value="getDownloadListSongs.content"
                 paginator
                 :rows="getDownloadListSongs.size"
                 :totalRecords="getDownloadListSongs.totalElements"
                 :rowsPerPageOptions="[10, 15, 20, 30, 50]"
                 lazy
                 @page="onPage"
                 size="small"
      >
        <Column header="Info">
          <template #body="slotProps">
            <Tag
                class="cursor-pointer p-1 text-white border-round border-0 bg-blue-500 hover:bg-blue-400"
                @click="showSongDialog(slotProps.data)"
            >
              <i class="pi pi-info-circle" style="font-size: 1.4em"></i>
            </Tag>
          </template>
        </Column>
        <Column field="status" header="Status">
          <template #body="slotProps">
            <Tag
                :value="slotProps.data.status"
                :severity="getSeverity(slotProps.data.status)"
                class="w-full"
            />
          </template>
        </Column>
        <Column field="name" header="Name"></Column>
        <Column header="Artists">
          <template #body="slotProps">
            {{ getArtists(slotProps.data.artists) }}
          </template>
        </Column>
        <Column field="album" header="Album"></Column>
        <Column field="attempts" header="Attempts"></Column>
        <Column field="bitRate" header="Bit Rate"></Column>
        <Column field="size" header="Size">
          <template #body="slotProps">
            {{ getSize(slotProps.data.size) }}
          </template>
        </Column>
        <Column field="lastCheck" header="Last Check">
          <template #body="slotProps">
            {{ getDate(slotProps.data.lastCheck) }}
          </template>
        </Column>
      </DataTable>
    </template>
  </Card>
  <SongDataDialog />
</template>
<script>
import {mapActions, mapState} from "pinia";
import {usePlaylistStore} from "@/store/playlist-calls";
import {utilsMixin} from "@/mixin/utils";
import SongDataDialog from "@/components/playlist/SongDataDialog.vue";

export default {
  name: "DownloadListSongTable",
  components: {SongDataDialog},
  mixins: [utilsMixin],
  data: () => ({
    showDialog: false,
    songData: null
  }),
  created() {
    this.onPage(null);
    this.emitter.on('refresh', this.handleRefresh);
  },
  props: {
    downloadList: Object
  },
  watch: {
    downloadList(newVal) {
      if (newVal && newVal.id)
        this.onPage(null);
    }
  },
  methods: {
    handleRefresh() {
      this.onPage(null);
    },
    getArtists(artists) {
      return this.getArtistsByComa(artists);
    },
    showSongDialog(data) {
      this.emitter.emit('song-data-dialog', data);
    },
    getSeverity(status) {
      switch (status) {
        case 'COMPLETED':
        case 'COPIED':
          return 'success';
        case 'SEARCHING':
        case 'FINDING_FILE':
          return 'info';
        case 'DOWNLOADING':
          return 'warning';
        case 'WAITING':
          return 'secondary';
      }
    },
    getDate(ts){
      return this.timestampToDate(ts);
    },
    getSize(bits){
      return this.bitsToSize(bits);
    },
    async onPage(event) {
      if (event) {
        this.page = event.page;
        this.rows = event.rows;
      }
      await this.fetchDownloadListSongs(
          this.downloadList?.id,
          this.page,
          this.rows,
      );
    },
    ...mapActions(usePlaylistStore, [
      'fetchDownloadListSongs',
    ])
  },
  computed: {
    ...mapState(usePlaylistStore, {
      getDownloadListSongs: 'getDownloadListSongs',
    })
  },
  beforeUnmount() {
    this.emitter.off('refresh', this.handleRefresh);
  }
}
</script>
