<template>
    <PlaylistContainerCard
        :playlists="discography"
    />
</template>
<script>
import PlaylistContainerCard from "@/components/shared/PlaylistCarousel.vue";

export default {
  name: "PlaylistDiscography",
  components: {PlaylistContainerCard},
  props:{
    playlistId: null
  },
  data: () => ({
    discography: null,
  }),
  created() {
    this.fetchDiscography();
  },
  methods: {
    fetchDiscography(){
      this.$axios
          .get(`/playlist/${this.playlistId}/parent-playlists`)
          .then( (res) => {
            this.discography = res.data;
          })
          .catch( () => {
            setTimeout(() => this.$router.push("/"), 1200);
          })
    }
  }
}
</script>