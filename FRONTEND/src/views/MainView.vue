<template>
  <div class="mt-2 grid w-full">
    <div class="col-12 xl:col-10">
      <AddPlaylistComp />
    </div>
    <div class="col-12 xl:col-2" v-if="mq.mdPlus">
      <RefreshConfigComp />
    </div>
    <PlaylistListComp v-if="playlist == null"/>
    <SongsTableComp v-else :pl-prop="playlist"/>
  </div>
</template>
<script>

import RefreshConfigComp from "@/components/RefreshTimer.vue";
import AddPlaylistComp from "@/components/AddPlaylistComp.vue";
import PlaylistListComp from "@/components/PlaylistContainerComp.vue";
import SongsTableComp from "@/components/SongsTableComp.vue";

export default {
  name: 'MainView',
  inject: ["mq"],
  components: {
    SongsTableComp, PlaylistListComp, AddPlaylistComp, RefreshConfigComp
  },
  data: () => ({
    playlist: null
  }),
  methods: {
  },
  created() {
    this.emitter.on('load-songs', pl => {
      this.playlist = pl;
    });
  }
}
</script>