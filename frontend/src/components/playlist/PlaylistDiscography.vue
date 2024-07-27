<template>
  <CardPLDataView
      :data="discography"
  />
</template>
<script>
import CardPLDataView from "@/components/shared/PlaylistDataView.vue";

export default {
  name: "PlaylistDiscography",
  components: {CardPLDataView},
  props:{
    playlistId: null
  },
  data: () => ({
    discography: null,
  }),
  created() {
    this.fetchDiscography();
    this.emitter.on('refresh', this.handleRefresh);
  },
  methods: {
    handleRefresh() {
      this.fetchDiscography();
    },
    fetchDiscography(){
      this.$axios
          .get(`/playlist/${this.playlistId}/parent-playlists`)
          .then( (res) => {
            this.discography = []
            this.discography = res.data;
          })
          .catch( () => {
            setTimeout(() => this.$router.push("/"), 1200);
          })
    }
  },
  beforeUnmount() {
    this.emitter.off('refresh', this.handleRefresh);
  },
}
</script>