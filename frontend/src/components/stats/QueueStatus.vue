<template>
  <Carousel v-if="threads.length > 0" :value="threads" :numVisible="4" circular :autoplayInterval="3000" class="w-full">
    <template #item="slotProps">
      <div class="m-1 p-3" style="max-width: 100%">
        <div class="mb-1">
          <div class="relative mx-auto">
            <img :src="slotProps.data.playlistCover"
                 :alt="slotProps.data.name"
                 style="width: 235px;"
                 class="border-round-2xl"
            />
            <div class="absolute p-1 bg-black-alpha-80 border-round" style="left:8px; top: 8px">
              <Tag :value="slotProps.data.status" :severity="getSeverity(slotProps.data.status)"/>
            </div>
            <div class="absolute p-1 bg-black-alpha-80 border-round white-space-nowrap overflow-hidden text-overflow-ellipsis" style="left:8px; bottom: 10px; width: 92%">
              <span
                  class="mb-1 w-fit font-medium">
                {{ `${slotProps.data.name} - ${getArtists(slotProps.data.artists)}` }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </Carousel>
  <div v-else class="w-full flex justify-content-center text-gray-600 font-bold text-xl p-3">
    No requests are currently queued
  </div>
</template>
<script>
import {utilsMixin} from "@/mixin/utils";

export default {
  name: "QueueStatus",
  mixins: [utilsMixin],
  props: {
    threads: {
      type: Array,
      required: true
    }
  },
  methods: {
    getSeverity(status) {
      return this.getStatusSeverity(status);
    },
    getArtists(artists) {
      return this.getByComa(artists)
    }
  }
}
</script>