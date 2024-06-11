<template>
  <CardPLDataView
      :data="discography"
      />
</template>
<script>


import CardPLDataView from "@/components/shared/CardPLDataView.vue";

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