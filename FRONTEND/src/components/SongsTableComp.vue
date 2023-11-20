<template>
  <SongsStatsComp
      v-show="playlist.songs.length > 1"
  />
  <div class="border-1 border-black-alpha-90 shadow-2 w-full mt-1">
    <div
        v-if="playlist.songs.length < 1"
        class="text-xl bg-white text-gray-700 py-2 w-full text-center"
    >
      Choose a playlist to check the song statuses
    </div>
    <DataTable
        v-else
        :value="playlist.songs"
        class="p-datatable-sm"
        showGridlines
        paginator
        :rows="5"
        :rowsPerPageOptions="[5, 10, 20, 50]"
    >
      <template #header>
        <div class="flex flex-wrap align-items-center justify-content-between gap-2" v-if="playlist.lastUpdate > 0">
          <span class="text-xl text-900 font-bold">Songs from {{ playlist.name }}</span>
          <span>
          <i class="pi pi-calendar mr-1" style="color: #708090"></i>
          <strong class="mr-2">Last Update:</strong>
          <span class="text-gray-500">{{ getTimestampFormatted(playlist.lastUpdate) }}</span>
        </span>
        </div>
      </template>
      <Column header="Name" sortable>
        <template #body="slotProps">
          <strong>{{ slotProps.data.name }}</strong>
        </template>
      </Column>
      <Column header="Artists" sortable>
        <template #body="slotProps">
          <Dropdown
              :options="slotProps.data.artists"
              class="w-full md:w-8rem"
              v-model="slotProps.data.artists[0]"
          />
        </template>
      </Column>
      <Column field="searchInput" header="Search" sortable>
      </Column>
      <Column header="Status" sortable>
        <template #body="slotProps">
          <div class="w-full flex justify-content-center align-items-center">
            <Button
                :icon="getStatusIcon(slotProps.data.status)"
                :severity="getStatusSeverity(slotProps.data.status)"
                v-tooltip.top="{ value: slotProps.data.status, showDelay: 10, hideDelay: 10 }"
                outlined
            />
          </div>
        </template>
      </Column>
      <Column header="File" style="max-width: 20%" sortable>
        <template #body="slotProps">
        <span v-if="slotProps.data.filename">
          {{ getFile(slotProps.data.filename)  }}
        </span>
          <span v-else class="text-gray-400">
          Not Found
        </span>
        </template>
      </Column>
      <Column header="Size" style="width: 12%" sortable>
        <template #body="slotProps">
          <div v-if="slotProps.data.size">
            {{ fromBytesToMB(slotProps.data.size)+'MB'  }}
          </div>
          <span v-else class="text-gray-400">
          Not Found
        </span>
        </template>
      </Column>
      <Column field="attempts" header="Attempts" style="width: 3% " sortable>
      </Column>
      <template #footer> <strong>Total: </strong>{{ playlist.songs.length }} songs. </template>
    </DataTable>
  </div>
</template>
<script>
import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";
import SongsStatsComp from "@/components/SongsStatsComp.vue";
import SongStatusIcons from "@/models/SongStatusIcons";
import SongStatusSeverity from "@/models/SongStatusSeverity";

export default {
  name: "SongsTableComp",
  components: {SongsStatsComp},
  methods: {
    getTimestampFormatted(timestamp){
      const date = new Date(timestamp);
      const year = date.getFullYear();
      const month = date.getMonth() + 1; // Los meses se cuentan desde 0 (enero) hasta 11 (diciembre)
      const day = date.getDate();
      const hours = date.getHours();
      const minutes = date.getMinutes();
      const seconds = date.getSeconds();
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    getStatusIcon(status){
      return 'pi '+SongStatusIcons[status];
    },
    getStatusSeverity(status){
      return SongStatusSeverity[status]
    },
    getFile(folderAndFile){
      const arr = folderAndFile.split(/[\\/]/);
      if (arr.length > 0) {
        return arr[arr.length - 1];
      } else {
        return folderAndFile;
      }
    },
    fromBytesToMB(bytes){
      return Math.round(bytes / 1048576 );
    },
    fetchSongs(playlistId){
      this.axios.get(
          '/playlist/songs',
          { params: { playlistId } }
      )
          .then( res => {
            this.playlist.songs = res.data;
          })
          .catch( e => console.log(e))
    },
  },
  data: () => ({
    playlist: {
      id: null,
      name: '',
      lastUpdate: 0,
      songs: []
    }
  }),
  created() {
    this.emitter.on('load-songs', playlist => {
      if (playlist != null) {
        this.emitter.emit('load-stats', playlist);
        const { id, name, lastUpdate } = playlist;
        this.playlist.name = name;
        this.playlist.lastUpdate = lastUpdate;
        this.playlist.id = id;
        this.fetchSongs(id);
      } else {
        this.playlist.songs = [];
      }
    })
    this.emitter.on('trigger-refresh', () => {
      if (this.playlist.id) this.fetchSongs(this.playlist.id)
      if (!this.isApiAlive) this.playlist.songs = [];
    });
  },
  computed: {
    ...mapState(useUserCfgStore, ['isApiAlive'])
  }
}
</script>
<style scoped>

</style>