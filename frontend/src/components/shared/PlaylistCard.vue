<template>
  <Card>
    <template #title>
      <div class="white-space-nowrap overflow-hidden text-overflow-ellipsis">
        {{ playlist.name }}
      </div>
    </template>
    <template #content>
      <div class="flex flex-wrap gap-3">
        <div class="w-12">
          <img
              :src="playlist.cover"
              class="border-round shadow-2"
              :alt="playlist.name"
              width="100%"
          />
        </div>
        <div class="w-12 flex justify-content-between">
          <div
              class="p-2 border-round inline font-s text-md text-gray-800 opacity-80"
              :style="`background-color : ${PlaylistColor[playlist.playlistType]}; letter-spacing: 1.3px`"
          >
            {{ playlist.playlistType }}
          </div>
          <div>
            <Button icon="pi pi-times" class="mr-2" severity="danger" />
            <Button icon="pi pi-search" @click="goToPlaylist(playlist.id)" severity="success" />
          </div>
        </div>
      </div>
    </template>
  </Card>
</template>
<script>
import PlaylistColor from "@/model/PlaylistColor";

export default {
  name: "PlaylistCard",
  computed: {
    PlaylistColor() {
      return PlaylistColor
    },
  },
  methods: {
    goToPlaylist(id) {
      this.$router.push("/playlist/" + id);
    },
  },
  props: {
    playlist: Object
  }
}
</script>
<style scoped>

</style>