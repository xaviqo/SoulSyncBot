<template>
  <div class="w-full border-1 bg-white p-2 border-black-alpha-90 shadow-2 m-2">
    <div class="flex justify-content-between">
      <div class="flex">
        <div class="ml-2 w-2rem h-2rem surface-50 border-1 border-black-alpha-90 flex align-items-center justify-content-center mr-3">
          <i class="pi pi-list" style="font-size: 1.2rem"></i>
        </div>
        <div class="flex align-items-center text-xl font-bold">
          Songs from {{ playlist.name }}
        </div>
      </div>
      <div class="flex align-items-center">
        <Button
            @click="goBack()"
            label="Explore Playlists"
            icon="pi pi-chevron-left"
        />
      </div>
    </div>
  </div>
  <div class="border-1 border-black-alpha-90 shadow-2 w-full m-2">
    <DataTable
        :value="playlist.songs"
        class="p-datatable-sm"
        showGridlines
        paginator
        :rows="10"
        :rowsPerPageOptions="[5, 10, 20, 50]"
    >
      <Column header="Name" sortable>
        <template #body="slotProps">
          <strong>{{ slotProps.data.name }}</strong>
        </template>
      </Column>
      <Column header="Artists">
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
      <Column field="status" header="Status" sortable>
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
      <Column field="file" header="File" style="max-width: 20%" sortable>
        <template #body="slotProps">
        <span v-if="slotProps.data.filename">
          {{ getFile(slotProps.data.filename)  }}
        </span>
        <span v-else class="text-gray-400">
          {{ slotProps.data.attempts < 2 ? 'Waiting...' : 'Not Found' }}
        </span>
        </template>
      </Column>
      <Column field="size" header="Size" style="width: 12%" sortable>
        <template #body="slotProps">
          <div v-if="slotProps.data.size">
            {{ fromBytesToMB(slotProps.data.size)+'MB'  }}
          </div>
          <span v-else class="text-gray-400">
          {{ slotProps.data.attempts < 2 ? 'Waiting...' : 'Not Found' }}
        </span>
        </template>
      </Column>
      <Column field="lastCheck" header="Last Check" sortable>
        <template #body="slotProps">
          <span>
            {{ getElapsedTime(slotProps.data.lastCheck) }}
          </span>
        </template>
      </Column>
      <Column field="attempts" header="Att." style="width: 1% " sortable>
      </Column>
      <template #footer> <strong>Total: </strong>{{ playlist.songs.length }} songs. </template>
    </DataTable>
  </div>
  <SongsStatsComp
      :playlist-id="playlist.id"
      :total="playlist.songs.length"
  />
</template>
<script>
import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";
import SongStatusIcons from "@/models/SongStatusIcons";
import SongStatusSeverity from "@/models/SongStatusSeverity";
import {soulmixin} from "@/mixins/soulmixin";
import {FilterMatchMode, FilterOperator} from "primevue/api";
import SongsStatsComp from "@/components/SongsStatsComp.vue";

export default {
  name: "SongsTableComp",
  components: {SongsStatsComp},
  mixins: [soulmixin],
  props: {
    plProp: Object
  },
  watch: {
    plProp:  {
      handler(newPl){
        this.playlist = {
          name: newPl.name,
          lastUpdate: newPl.lastUpdate,
          id: newPl.id,
          songs: []
        };
        this.fetchSongs(newPl.id);
      },
      immediate: true
    }
  },
  methods: {
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
      if (this.isApiAlive) {
        this.axios.get(
            '/playlist/songs',
            { params: { playlistId } }
        )
        .then( res => {
          this.playlist.songs = res.data;
        })
        .catch( e => console.log(e))
      }
    },
    goBack(){
      this.emitter.emit('load-songs', null);
    }
  },
  data: () => ({
    playlist: {
      id: null,
      name: '',
      lastUpdate: 0,
      songs: []
    },
    filters: {
      global: { value: null, matchMode: FilterMatchMode.CONTAINS },
      name: { operator: FilterOperator.AND, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
      'country.name': { operator: FilterOperator.AND, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
      representative: { value: null, matchMode: FilterMatchMode.IN },
      status: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.EQUALS }] }
    }
  }),
  created() {
    this.$subscribe( (mutation, state) => {
      if (state.interval.sec === 0) {
        this.fetchSongs(this.playlist.id);
      }
    });
  },
  computed: {
    ...mapState(useUserCfgStore, ['isApiAlive','$subscribe'])
  }
}
</script>
<style scoped>

</style>