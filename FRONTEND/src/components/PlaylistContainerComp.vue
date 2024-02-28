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
  <div v-else
       class="w-full flex flex-wrap gap-3"
       :class="playlists.length > 1 ? 'justify-content-center' : ''"
  >
    <template v-for="pl in playlists" :key="pl.id">
      <PlaylistCardComp :pl="pl" :class="playlists.length < 2 ? 'ml-2':''" />
    </template>
  </div>
</template>
<script>
import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";
import PlaylistCardComp from "@/components/PlaylistCardComp.vue";

export default {
  name: "PlaylistContainerComp",
  components: {PlaylistCardComp},
  methods: {
    fetchPlaylists(fetch) {
      if (fetch && this.isApiAlive) {
        this.axios.get('/playlist')
            .then(res => {
              this.waitUpdate = false;
              res.data.forEach(pl => {
                if (pl.cover === "") {
                  pl.cover = "https://placehold.co/400x400/green/white";
                }
              });
              this.playlists = res.data;
            })
            .catch(e => {
              console.log(e);
              this.playlists = [];
            });
      }
    }
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
    this.emitter.on('remove-playlist', (id) => {
      this.playlists = this.playlists.filter( pl => pl.id !== id);
    })
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