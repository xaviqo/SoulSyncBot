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
      <div>
        <ProgressBar :value="timer">
          <div>{{ `Next refresh: ${timer - timerThreshold}sec` }}</div>
        </ProgressBar>
      </div>
    </div>
    <div class="w-10 flex flex-wrap gap-3">
      <div class="w-12 flex gap-3">
        <PlaylistName
            class="w-4"
            :playlist-name="playlist?.name"
            :id="playlist?.id"
        />
        <PlaylistManagerButton
            class="w-8"
            :playlist-type="playlist?.playlistType"
        />
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

export default {
  name: "PlaylistBody",
  components: {
    PlaylistManagerButton,
    PlaylistName,
    PlaylistCover,
    PlaylistType,
  },
  created() {
/*    this.emitter.on(
        'download-list-action',
        payload => this.handleDownloadListAction(payload)
    );*/
  },
  props: {
    playlist: {
      type: Object,
      required: true
    },
    timer: {
      type: Number,
      required: true
    },
    timerThreshold: {
      type: Number,
      required: true
    }
  }
}
</script>
<style scoped>

</style>