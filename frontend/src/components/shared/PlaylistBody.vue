<template>
  <div class="flex gap-3">
    <div class="w-2 flex flex-column flex-wrap justify-content-between gap-3">
      <div class="flex flex-column flex-wrap gap-3">
        <PlaylistCover
            :playlist-cover="playlist?.cover"
        />
        <Card class="w-12 p-0 m-0">
          <template #content>
            <div
                class="p-2 border-round font-semibold inline text-gray-800 opacity-80 w-full flex justify-content-center"
                :style="`background-color : ${PlaylistColor[playlist.playlistType]}; letter-spacing: 1.3px`"
            >
              {{ playlist.playlistType }}
            </div>
          </template>
        </Card>
        <Card class="w-12 p-0 m-0">
          <template #content>
            <div
                class="inline text-gray-800 opacity-80 w-full flex flex-wrap justify-content-center gap-2"
                style="letter-spacing: 1.3px"
            >
              <div class="p-1 font-semibold text-white">
                Total Songs
              </div>
              <div class="p-1 border-round bg-gray-800 text-white">
                {{ playlist.totalTracks }}
              </div>
            </div>
          </template>
        </Card>
        <Card v-if="playlist.lastUpdate > 0" class="w-12 p-0 m-0">
          <template #content>
            <div
                class="inline text-gray-800 opacity-80 w-full flex flex-wrap justify-content-center gap-2"
                style="letter-spacing: 1.3px"
            >
              <div class="p-1 font-semibold text-white">
                Last Update
              </div>
              <div class="p-1 border-round bg-gray-800 text-white">
                {{ timestampToDate(playlist.lastUpdate) }}
              </div>
            </div>
          </template>
        </Card>
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
import PlaylistCover from "@/components/playlist/PlaylistCover.vue";
import PlaylistName from "@/components/playlist/PlaylistName.vue";
import PlaylistManagerButton from "@/components/playlist/PlaylistManagerButton.vue";
import RefreshTimer from "@/components/shared/RefreshTimer.vue";
import PlaylistColor from "@/model/PlaylistColor";
import {utilsMixin} from "@/mixin/utils";

export default {
  name: "PlaylistBody",
  mixins: [utilsMixin],
  components: {
    RefreshTimer,
    PlaylistManagerButton,
    PlaylistName,
    PlaylistCover,
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
  },
  computed: {
    PlaylistColor() {
      return PlaylistColor
    },
  }
}
</script>