<template>
  <div class="border-1 border-black-alpha-90 shadow-2 w-full m-2">
    <div class="w-full flex flex-wrap justify-content-evenly">
      <div class="col flex flex-wrap"
           v-for="(value, name) in stats" :key="name"
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
              this.stats = res.data;
            })
            .catch( e => console.log(e))
      }
    },
    capitalize(str) {
      return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    }
  },
  data: () => ({
    stats: null
  }),
  created() {
    this.$subscribe( (mutation, state) => {
      if (state.interval.sec === 0) {
        this.fetchStats(this.playlistId);
      }
    });
  },
  computed: {
    ...mapState(useUserCfgStore, ['isApiAlive','$subscribe'])
  },
  props: {
    playlistId: String,
    total: Number
  },
  watch: {
    playlistId: {
      handler(id){
        this.fetchStats(id);
      },
      immediate: true
    }
  }
}
</script>
<style scoped>

</style>