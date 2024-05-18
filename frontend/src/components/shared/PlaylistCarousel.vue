<template>
  <Carousel
      :value="playlists"
      :numVisible="3"
      :numScroll="1"
      :autoplayInterval="3000"
  >
    <template #>
      s
    </template>
    <template #item="pl">
      <PlaylistCard
          class="m-3"
          :playlist="pl.data"
      ></PlaylistCard>
    </template>
  </Carousel>
</template>
<script>
import PlaylistCard from "@/components/shared/PlaylistCard.vue";

export default {
  name: "PlaylistCarousel",
  components: {PlaylistCard},
  props: {
    playlists: Array
  }
}
</script>
<style scoped>
</style>