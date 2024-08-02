<template>
  <div class="w-full flex flex-nowrap gap-3">
    <Card class="w-4">
      <template #content>
        <InputGroup>
          <Button
              icon="pi pi-times"
              severity="danger"
              @click="searchByName(false)"
          />
          <InputText
              placeholder="Search by name"
              v-model="nameContainsInput"
          />
          <Button
              icon="pi pi-search"
              severity="success"
              @click="searchByName(true)"
              @keydown.enter="searchByName(true)"
          />
        </InputGroup>
      </template>
    </Card>
    <Card class="w-8">
      <template #content>
        <div class="w-full flex gap-1">
          <div class="w-3 flex align-items-center text-xl text-gray-400">
            Filter by status:
          </div>
          <div class="w-9 flex justify-content-evenly gap-1">
            <Button
                v-for="status in Object.keys(DownloadStatus)"
                :key="status"
                :severity="getSeverity(status)"
                :label="status"
                type="button"
                class="shadow-1"
                :class="processArr.includes(status) ? 'opacity-100' : 'opacity-40'"
                size="small"
                @click="addStatusFilter(processArr.includes(status),status)"
            />
          </div>
        </div>
      </template>
    </Card>
  </div>
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
        <template #loading>Loading download lists... Please wait.</template>
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
        <Column field="status" header="Status" >
          <template #body="slotProps">
            <Tag
                :value="slotProps.data.status"
                :severity="getSeverity(slotProps.data.status)"
                class="w-full"
            />
          </template>
        </Column>
        <Column field="name" header="Name">
        </Column>
        <Column header="Artists">
          <template #body="slotProps">
            {{ getArtists(slotProps.data.artists) }}
          </template>
        </Column>
        <Column field="album" header="Album"></Column>
        <Column field="attempts" header="Attempts" ></Column>
        <Column field="bitRate" header="Bit Rate"></Column>
        <Column field="size" header="Size">
          <template #body="slotProps">
            {{ getSize(slotProps.data.size) }}
          </template>
        </Column>
        <Column field="lastCheck" header="Last Check" >
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
import DownloadStatus from "@/model/DownloadStatus";
import {mapActions, mapState} from "pinia";
import {usePlaylistStore} from "@/store/playlist-calls";
import {utilsMixin} from "@/mixin/utils";
import SongDataDialog from "@/components/playlist/SongDataDialog.vue";

export default {
  name: "DownloadListSongTable",
  components: {SongDataDialog},
  mixins: [utilsMixin],
  data: () => ({
    nameContainsInput: '',
    nameContains: '',
    showDialog: false,
    songData: null,
    currentEvent: null,
    processArr: []
  }),
  created() {
    Object.keys(DownloadStatus).forEach(ds => this.addStatusFilter(false,ds));
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
      return this.getByComa(artists);
    },
    showSongDialog(data) {
      this.emitter.emit('song-data-dialog', data);
    },
    getSeverity(status) {
      return this.getStatusSeverity(status);
    },
    getDate(ts){
      return this.timestampToDate(ts);
    },
    getSize(bits){
      return this.bitsToSize(bits);
    },
    searchByName(isSearch) {
      if (!isSearch) {
        this.nameContains = "";
        this.nameContainsInput = "";
      } else {
        this.nameContains = this.nameContainsInput;
      }
      this.onPage(null);
    },
    addStatusFilter(isPresent, status) {
      if (!isPresent)
        this.processArr.push(status);
      else
        this.processArr = this.processArr.filter( s => s !== status);

      this.onPage(this.currentEvent);
    },
    async onPage(event) {
      if (event) {
        this.currentEvent = event;
        this.page = event.page;
        this.rows = event.rows;
      }
      const params = {
        page: this.page ,
        size: this.rows,
        nameContains: this.nameContains,
        processesByComa: this.processArr.map(p => DownloadStatus[p]).join(",")
      };
      await this.fetchDownloadListSongs(
          this.downloadList?.id,
          params
      );
    },
    ...mapActions(usePlaylistStore, [
      'fetchDownloadListSongs',
    ])
  },
  computed: {
    DownloadStatus() {
      return DownloadStatus
    },
    ...mapState(usePlaylistStore, {
      getDownloadListSongs: 'getDownloadListSongs',
    })
  },
  beforeUnmount() {
    this.emitter.off('refresh', this.handleRefresh);
  }
}
</script>
