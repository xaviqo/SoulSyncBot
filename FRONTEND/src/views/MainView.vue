<template>
  <div class="mt-2 grid w-full">
    <div class="col-12 xl:col-10">
      <AddPlaylistComp />
    </div>
    <div class="col-12 xl:col-2" v-if="mq.mdPlus">
      <RefreshConfigComp />
    </div>
    <div class="col-12 xl:col-4">
      <PlaylistListComp />
    </div>
    <div class="col-12 xl:col-8">
      <SongsTableComp />
    </div>
  </div>
</template>
<script>

import RefreshConfigComp from "@/components/RefreshTimer.vue";
import AddPlaylistComp from "@/components/AddPlaylistComp.vue";
import PlaylistListComp from "@/components/PlaylistListComp.vue";
import SongsTableComp from "@/components/SongsTableComp.vue"

export default {
  name: 'MainView',
  inject: ["mq"],
  components: {
    PlaylistListComp, AddPlaylistComp, SongsTableComp, RefreshConfigComp
  },
  data: () => ({
    songsCard: 'load-songs'
  }),
  methods: {
  },
  created() {
  }
}
</script>