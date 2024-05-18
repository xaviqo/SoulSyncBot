<template>
  <Card class="w-12">
    <template #content>
      <div
          class="p-2 border-round font-semibold inline text-gray-800 opacity-80 w-full flex justify-content-center"
          :style="`background-color : ${PlaylistColor[playlistType]}; letter-spacing: 1.3px`"
      >
        {{ playlistType }}
      </div>
    </template>
  </Card>
</template>
<script>
import PlaylistColor from "@/model/PlaylistColor";

export default {
  name: "PlaylistType",
  computed: {
    PlaylistColor() {
      return PlaylistColor
    },
  },
  props: {
    playlistType: String
  },
}
</script>
<style scoped>

</style>