<template>
  <DataView :value="data" layout="grid" class="border-round-xl overflow-hidden" data-key="id">
    <template #grid="slotProps">
      <div class="grid grid-cols-12 grid-nogutter">
        <PlaylistCard
            v-for="pl in slotProps.items"
            :key="pl.id"
            :class="getCardClass()"
            :playlist="pl"
        ></PlaylistCard>
        <!--  -->
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
  name: "PlaylistDataView",
  components: {PlaylistCard},
  props: {
    data: Array
  },
  methods: {
    getCardClass(){
      const data = this.data;
      if (data && data.length <= 1)
        return '';
      else if (data && data.length === 2)
        return 'lg:col-6 md:col-12 col-12';
      else
        return 'lg:col-4 md:col-6 col-12';
    }
  }
}
</script>