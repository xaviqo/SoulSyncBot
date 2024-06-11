<template>
  <DataView :value="data" layout="grid" class="border-round-xl overflow-hidden">
    <template #grid="slotProps">
      <div class="grid grid-nogutter">
        <PlaylistCard
            v-for="pl in slotProps.items"
            :key="pl.id"
            :class="(slotProps.items < 2) ? '' : 'lg:col-4 md:col-6 col-12'"
            :playlist="pl"
        ></PlaylistCard>
      </div>
    </template>
    <template #empty>
      <div class="w-full flex justify-content-center p-4 text-xl text-gray-600">
        <span>
          No playlists listed
        </span>
      </div>
    </template>
  </DataView>
</template>
<script>
import PlaylistCard from "@/components/shared/PlaylistCard.vue";

export default {
  name: "CardPLDataView",
  components: {PlaylistCard},
  props: {
    data: Array
  }
}
</script>
<style scoped>
</style>