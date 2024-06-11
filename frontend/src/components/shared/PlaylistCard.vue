<template>
  <Card >
    <template #content>
      <div class="flex flex-wrap gap-3">
        <div class="relative w-12 flex justify-content-center wrap">
          <div class="w-12 p-3 absolute white-space-nowrap overflow-hidden text-overflow-ellipsis text-xl bg-black-alpha-80 border-round-top-lg">
            {{ playlist.name }}
          </div>
          <img
              :src="playlist.cover"
              class="w-12 border-round-lg"
              :alt="playlist.name"
          />
          <div class="w-12 p-3 absolute bottom-0 white-space-nowrap overflow-hidden text-overflow-ellipsis text-xl bg-black-alpha-80 border-round-bottom-lg">
            <div class="w-12 flex justify-content-between" style="bottom: 0">
              <div
                  class="p-2 border-round text-black-alpha-90"
                  :style="`background-color : ${PlaylistColor[playlist.playlistType]}; letter-spacing: 1.3px`"
              >
                {{ playlist.playlistType }}
              </div>
              <div>
                <Button icon="pi pi-times" class="mr-2" severity="danger" />
                <Button icon="pi pi-search" @click="goToPlaylist(playlist.id)" class="mr-2" severity="success" />
              </div>
            </div>
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
      this.$router.push({ name : 'playlist-view', params: { id }});
    },
  },
  props: {
    playlist: Object
  }
}
</script>
<style scoped>

</style>