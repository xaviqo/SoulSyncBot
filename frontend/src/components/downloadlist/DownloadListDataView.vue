<template>
  <DataView
      v-if="downloadLists && downloadLists.length > 0"
      :value="downloadLists"
      :paginator="showGlass"
      layout="list"
      :rows="1"
      class="border-round-xl overflow-hidden"
  >
    <template #list="slotProps">
      <div class="grid grid-nogutter">
        <DownloadListCard
            v-for="dl in slotProps.items"
            :key="dl.id"
            :download-list="dl"
            :playlist-id="dl.playlistId"
            class="w-12 p-2"
            :show-glass="showGlass"
            :show-cog="showCog"
            :show-pause="showPause"
            :show-trash="showTrash"
        >
        </DownloadListCard>
      </div>
    </template>
  </DataView>
</template>
<script>
import DownloadListCard from "@/components/downloadlist/DownloadListCard.vue";

export default {
  name: "DownloadListDataView",
  components: {DownloadListCard},
  props: {
    downloadLists: {
     type: Array,
      required: true
    },
    showGlass: {
      type: Boolean,
      required: true
    },
    showCog: {
      type: Boolean,
      required: true
    },
    showPause: {
      type: Boolean,
      required: true
    },
    showTrash: {
      type: Boolean,
      required: true
    }
  }
}
</script>