<template>
  <div class="w-full border-1 bg-white p-2 border-black-alpha-90 shadow-2 m-2">
    <div class="flex justify-content-between">
      <div class="flex">
        <div class="ml-2 w-2rem h-2rem surface-50 border-1 border-black-alpha-90 flex align-items-center justify-content-center mr-3">
          <i class="pi pi-list" style="font-size: 1.2rem"></i>
        </div>
        <div class="flex align-items-center text-xl font-bold">
          Added Playlists
        </div>
      </div>
      <div class="flex align-items-center">
        <Button
            @click="fetchPlaylists(true)"
            label="Force Refresh"
            icon="pi pi-sync"
        />
      </div>
    </div>
  </div>
  <div class="border-1 border-black-alpha-90 shadow-2 w-full mt-1" v-if="playlists.length < 1">
    <div
        class="text-xl bg-white text-gray-700 py-2 w-full text-center"
    >
      <i
          class="pi pi-spin pi-spinner mr-2"
          style="font-size: 1rem; color: lightslategray"
      ></i>
      <span >No playlists are currently available.</span>
    </div>
  </div>
  <div v-else class="w-full flex flex-wrap gap-3 justify-content-center mt-2">
    <template v-for="pl in playlists" :key="pl.id">
      <div class="bg-white border-1 border-black-alpha-90 shadow-2" :style="mq.mdMinus ? 'width: 100%; margin: 0 10px' : 'width: 48.7%;'">
        <div class="flex flex-nowrap justify-content-between m-2">
          <div class="text-2xl flex flex-nowrap font-bold text-900 white-space-nowrap overflow-hidden text-overflow-ellipsis" >
            <div
                class="cursor-pointer p-1 bg-blue-500 hover:bg-blue-600 text-white border-1 border-black-alpha-90 flex align-items-center justify-content-center mr-3"
                @click="loadPlaylistInfo({
                    name: pl.name,
                    id: pl.id,
                    lastUpdate: pl.lastUpdate,
                    total: pl.total
                  })"
            >
              <i class="pi pi-search" style="font-size: 1.2rem"></i>
            </div>
            <span>
              {{ pl.name }}
            </span>
          </div>
          <div class="flex flex-nowrap">
            <ProgressBar
                :value="getProgressBarValue(pl.total,pl.totalSucceeded)"
                style="min-width: 7rem; min-height: 1.7rem"
                class="border-1 border-black-alpha-90 ml-2"
            />
          </div>
        </div>
        <div class="flex flex-nowrap">
          <div class="w-full grid p-2">
            <div class="col-6 flex-nowrap">
              <span class="text-900">
                <i class="pi pi-calendar-plus" style="scale: .9"></i>
                Added:
              </span>
              <span class="text-600">
                {{ formatTimestamp(pl.added) }}
              </span>
            </div>
            <div class="col-6 flex-nowrap">
              <span class="text-900">
                <i class="pi pi-clock" style="scale: .9"></i>
                Updated:
              </span>
              <span class="text-600">
                {{ getElapsedTime(pl.lastUpdate) }} ago
              </span>
            </div>
            <div class="col-6 flex-nowrap">
              <span class="text-900">
                <i class="pi pi-plus-circle" style="scale: .9"></i>
                Tracks in Playlist:
              </span>
              <span class="text-600">
                {{ pl.total }}
              </span>
            </div>
            <div class="col-6 flex-nowrap">
              <span class="text-900">
                <i class="pi pi-check-circle" style="scale: .9"></i>
                Tracks Succeded:
              </span>
              <span class="text-600">
                {{ pl.totalSucceeded }}
              </span>
            </div>
            <div class="col-12" >
              <Tag :value="pl.type" class="border-1 border-black-alpha-90 bg-gray-500" />
            </div>
          </div>
          <div class="mr-2 mb-1">
            <img
                :src="pl.cover"
                :alt="pl.id"
                class="w-7rem shadow-2 border-1 border-black-alpha-90"
            />
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
<script>
import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";
import {soulmixin} from "@/mixins/soulmixin";

export default {
  name: "PlaylistContainerComp",
  inject: ["mq"],
  mixins: [soulmixin],
  methods: {
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
    },
    formatTimestamp(ts){
      const date = new Date(ts);
      const d = date.getDate();
      const mon = date.getMonth() + 1;
      const y = date.getFullYear();
      const h = date.getHours();
      const min = date.getMinutes();
      const s = date.getSeconds();
      return `${d}/${mon}/${y} ${h}:${min}:${s}`;
    },
    loadPlaylistInfo(playlist) {
      this.emitter.emit('load-songs', playlist);
    },
  },
  data: () => ({
    playlists: [],
    songsStatusOptions: [],
    waitUpdate: false,
  }),
  created() {
    this.$subscribe( (mutation, state) => {
      if (state.interval.sec === 0) {
        this.fetchPlaylists(this.isApiAlive);
      }
    });
    this.playlists = [];
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