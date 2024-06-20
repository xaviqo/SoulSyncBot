<template>
  <DataView
      v-if="downloadLists && downloadLists.length > 0"
      :value="downloadLists"
      :paginator="showGlass"
      layout="list"
      :rows="5"
      class="border-round-xl overflow-hidden"
  >
    <template #list="slotProps">
      <div class="grid grid-nogutter">
        <DownloadListCard
            v-for="dl in slotProps.items"
            :key="dl.id"
            :download-list="dl"
            :playlist-id="playlistId"
            class="w-12 p-2"
            :show-glass="showGlass"
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
    playlistId: {
      type: String,
      required: true
    },
    showGlass: {
      type: Boolean,
      required: true
    }
  }
}
</script>
<style scoped>

</style>