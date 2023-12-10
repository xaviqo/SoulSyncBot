<template>
  <div class="bg-white border-1 border-black-alpha-90 shadow-2" :style="mq.mdMinus ? 'width: 100%; margin: 0 10px' : 'width: 48.7%;'">
    <div class="flex flex-nowrap justify-content-between m-2">
      <div class="text-2xl flex flex-nowrap font-bold text-900 white-space-nowrap overflow-hidden text-overflow-ellipsis" >
        <div
            class="cursor-pointer p-1 bg-blue-500 hover:bg-blue-600 text-white border-1 border-black-alpha-90 flex align-items-center justify-content-center mr-2"
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
        <Button
            :icon="isPaused ? 'pi pi-play' : 'pi pi-pause'"
            :severity="isPaused ? 'success' : 'secondary'"
            class="border-1 border-black-alpha-90"
            @click="pausePlaylist(pl.id)"
        />
        <Button
            icon="pi pi-trash"
            severity="danger" class="border-1 border-black-alpha-90 ml-2"
            @click="removePlaylist($event,pl.id)"
        />
        <ProgressBar
            :value="getProgressBarValue(pl.total,pl.totalSucceeded)"
            style="min-width: 7rem; min-height: 1.7rem"
            class="border-1 border-black-alpha-90 ml-2"
        />
        <ConfirmPopup></ConfirmPopup>
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

<script>

import {soulmixin} from "@/mixins/soulmixin";
import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";

export default {
  name: "PlaylistCardComp",
  mixins: [soulmixin],
  inject: ["mq"],
  props: {
    pl: Object
  },
  data: () => ({
    isPaused: false
  }),
  methods: {
    getProgressBarValue(total, succeeded){
      return Math.round(( succeeded / total ) * 100);
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
    fetchPauseStatus(playlistId){
      if (this.isApiAlive) {
        this.axios.get(
            '/playlist/pause',
            { params: { playlistId } }
        )
            .then( res => {
              this.isPaused = res.data.isPaused;
            })
            .catch( e => {
              console.log(e)
              this.playlists = false;
            });
      }
    },
    pausePlaylist(playlistId){
      if (this.isApiAlive) {
        this.axios.post(
            '/playlist/pause',
            {
              playlistId: playlistId,
              pause: !this.isPaused
            }
        )
            .then( () => {
              this.isPaused = !this.isPaused;
            })
            .catch( e => {
              console.log(e)
              this.playlists = false;
            });
      }
    },
    removePlaylist(event,playlistId){
      this.$confirm.require({
        target: event.currentTarget,
        message: 'Do you want to remove this playlist from the database (only songs that are NOT shared with other playlists)?',
        icon: 'pi pi-exclamation-triangle',
        accept: () => {
          if (this.isApiAlive) {
            this.axios.delete(
                '/playlist',
                { params: { playlistId } }
            )
            .then( () => {
              this.emitter.emit('remove-playlist',playlistId);
            })
            .catch( e => {
              console.log(e)
            });
          }
        }
      });
    }
  },
  computed: {
    ...mapState(useUserCfgStore, [
      'isApiAlive'
    ])
  },
  created() {
    this.fetchPauseStatus(this.pl.id);
  }
}
</script>