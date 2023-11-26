<template>
  <div class="border-1 border-black-alpha-90 shadow-2 w-full mt-1">
    <div class="w-full flex flex-wrap justify-content-evenly">
      <div class="col flex flex-wrap"
           v-for="(value, name) in playlist.stats" :key="name"
      >
        <div class="w-full">
          <strong>{{ capitalize(name) }}</strong>
        </div>
        <div class="w-full">
          <ProgressBar
              :value="getProgressBarValue(total,value)"
              style="min-width: 9rem"
              class="border-1 border-black-alpha-90"
          />
        </div>
      </div>
    </div>
  </div>
</template>
<script>

import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";

export default {
  name: "SongsStatsComp",
  methods: {
    getProgressBarValue(total, succeeded){
      return Math.round(( succeeded / total ) * 100);
    },
    fetchStats(playlistId){
      if (this.isApiAlive) {
        this.axios.get(
            '/playlist/stats',
            { params: { playlistId } }
        )
            .then( res => {
              this.playlist.stats = res.data;
            })
            .catch( e => console.log(e))
      }
    },
    capitalize(str) {
      return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    }
  },
  data: () => ({
    playlist: {
      id: null,
      name: '',
      lastUpdate: 0,
      total: 0,
      stats: null,
    }
  }),
  created() {
    this.emitter.on('load-stats', playlist => {
      if (playlist != null) {
        const { id, name, lastUpdate, total } = playlist;
        this.playlist.name = name;
        this.playlist.lastUpdate = lastUpdate;
        this.playlist.id = id;
        this.total = total;
        this.fetchStats(id);
      } else {
        this.playlist.stats = null;
      }
    })
    this.$subscribe( (mutation) => {
      const refresh = mutation.events.key === 'call';
      if (refresh && this.playlist.id) {
        this.fetchStats(this.playlist.id);
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