<template>
  <div class="border-1 border-black-alpha-90 shadow-2 w-full mt-1">
    <div v-if="playlists.length < 1"
         class="text-xl bg-white text-gray-700 py-2 w-full text-center"
    >
      <i
          class="pi pi-spin pi-spinner mr-2"
          :style="'font-size: 1rem; color:' + (waitUpdate ? 'blue' : 'lightslategray') "
      ></i>
      <span v-if="!waitUpdate">No playlists are currently available.</span>
    </div>
    <div v-else>
      <DataView
          :value="playlists"
          class="p-datatable-sm"
          paginator :rows="3"
          :sortField="lastUpdate"
      >
        <template #list="slotProps">
          <div class="col-12">
            <div class="flex flex-row xl:align-items-start p-4 gap-4">
              <img :src="slotProps.data.cover" :alt="slotProps.data.id" class="w-7rem shadow-2 border-1 border-black-alpha-90" />
              <div class="flex flex-column sm:flex-row justify-content-between align-items-center xl:align-items-start flex-1 gap-4" style="width: 55%">
                <div class="flex flex-column align-items-center sm:align-items-start gap-3" style="max-width: 70%">
                  <span class="text-2xl font-bold text-900 white-space-nowrap overflow-hidden text-overflow-ellipsis" style="max-width: 100%">
                    {{ slotProps.data.name }}
                  </span>
                  <Tag :value="slotProps.data.status" :severity="getStatusSeverity(slotProps.data)" class="border-1 border-black-alpha-90" />
                  <ProgressBar
                      :value="getProgressBarValue(slotProps.data.total,slotProps.data.totalSucceeded)"
                      style="min-width: 9rem"
                      class="border-1 border-black-alpha-90"
                  />
                </div>
                <div class="flex sm:flex-column align-items-center sm:align-items-end gap-3 sm:gap-2">
                  <Button
                      icon="pi pi-search" class="border-1 border-black-alpha-90 shadow-2"
                      @click="loadPlaylistInfo('load-songs',{
                    name: slotProps.data.name,
                    id: slotProps.data.id,
                    lastUpdate: slotProps.data.lastUpdate,
                    total: slotProps.data.total
                  })"
                  />
                </div>
              </div>
            </div>
          </div>
        </template>
      </DataView>
    </div>
  </div>
</template>
<script>
import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";

export default {
  name: "PlaylistListComp",
  methods: {
    getStatusSeverity(playlist){
      switch (playlist.status) {
        case 'WAITING':
          return 'warning';
        case 'QUEUED':
          return 'info';
        case 'COMPLETED':
          return 'success';
        default:
          return 'error';
      }
    },
    loadPlaylistInfo(source,playlist) {
      this.emitter.emit('load-songs', playlist);
    },
    getProgressBarValue(total, succeeded){
      return Math.round(( succeeded / total ) * 100);
    },
    fetchPlaylists(fetch){
      if (fetch) {
        this.axios.get('/playlist')
            .then( res => {
              this.waitUpdate = false;
              this.playlists = res.data;
            })
            .catch( e => {
              console.log(e)
              this.playlists = [];
            })
      }
    }
  },
  data: () => ({
    playlists: [],
    songsStatusOptions: [],
    waitUpdate: false
  }),
  created() {
    this.$subscribe( (mutation) => {
      const refresh = mutation.events.key === 'call';
      if (refresh) {
        this.fetchPlaylists(this.isApiAlive);
      }
    });
    this.fetchPlaylists(true);
  },
  computed: {
    ...mapState(useUserCfgStore, [
        'isApiAlive','$subscribe'
    ])
  }
}
</script>
<style scoped>

</style>