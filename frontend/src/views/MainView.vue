<template>
  <div class="flex flex-wrap gap-3">
    <AddNewPlaylistCard/>
    <CardPLDataView :data="getPlaylists"/>
  </div>
</template>
<script>
import AddNewPlaylistCard from "@/components/panel/AddNewPlaylistCard.vue";
import {mapActions, mapState} from "pinia";
import {usePlaylistStore} from "@/store/playlist-calls";
import CardPLDataView from "@/components/shared/CardPLDataView.vue";

export default {
  name: "MainView",
  components: {CardPLDataView, AddNewPlaylistCard},
  data: () => ({
    layout: 'grid'
  }),
  created() {
    this.fetchAllPlaylists();
  },
  methods: {
    ...mapActions(usePlaylistStore,[
      'fetchAllPlaylists'
    ])
  },
  computed: {
    ...mapState(usePlaylistStore,[
      'getPlaylists'
    ])
  },
}
</script>
<style scoped>
.dview {
  border-radius: 50px !important
}
</style>