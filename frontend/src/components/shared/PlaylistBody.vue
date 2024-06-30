<template>
  <div class="flex gap-3">
    <div class="w-2 flex flex-column flex-wrap justify-content-between gap-3">
      <div class="flex flex-column flex-wrap gap-3">
        <PlaylistCover
            :playlist-cover="playlist?.cover"
        />
        <PlaylistType
            :playlist-type="playlist?.playlistType"
        />
      </div>
      <RefreshTimer/>
    </div>
    <div class="w-10 flex flex-wrap gap-3">
      <div class="w-12 flex gap-3">
        <PlaylistName
            :class="isDownloadManager ? 'w-9' : 'w-4'"
            :emoji="isDownloadManager ? '💾' : '💿' "
            :playlist-name="playlist?.name"
            :id="playlist?.id"
        />
        <PlaylistManagerButton
            :class="isDownloadManager ? 'w-3' : 'w-8'"
            :playlist-type="playlist?.playlistType"
            :playlist-name="playlist?.name"
            :isDownloadManager="isDownloadManager"
            :playlist-id="playlist?.id"/>
      </div>
      <slot name="content" />
    </div>
  </div>
</template>
<script>
import PlaylistType from "@/components/playlist/PlaylistType.vue";
import PlaylistCover from "@/components/playlist/PlaylistCover.vue";
import PlaylistName from "@/components/playlist/PlaylistName.vue";
import PlaylistManagerButton from "@/components/playlist/PlaylistManagerButton.vue";
import RefreshTimer from "@/components/shared/RefreshTimer.vue";

export default {
  name: "PlaylistBody",
  components: {
    RefreshTimer,
    PlaylistManagerButton,
    PlaylistName,
    PlaylistCover,
    PlaylistType,
  },
  props: {
    playlist: {
      type: Object,
      required: true
    },
    isDownloadManager: {
      type: Boolean,
      required: true
    }
  }
}
</script>
<style scoped>

</style>