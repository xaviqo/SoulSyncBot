<template>
  <div>
    <div class="flex flex-nowrap gap-2 mb-2">
      <div class="w-6 border-1 border-50 p-1 border-round">
        <div class="font-bold text-sm text-gray-300 mb-1">
          Is Banned
        </div>
        <div class="text-sm text-gray-400">
          {{ iterationsData.isBanned ? 'Yes' : 'No' }}
        </div>
      </div>
      <div class="w-6 border-1 border-50 p-1 border-round">
        <div class="font-bold text-sm text-gray-300 mb-1">
          Ban Expiration
        </div>
        <div class="text-sm text-gray-400">
          {{ iterationsData.isBanned ? tsToDate(iterationsData.banExpirationTime): 'Not Banned' }}
        </div>
      </div>
    </div>
    <div class="flex flex-nowrap gap-2 mb-2">
      <div class="w-6 border-1 border-50 p-1 border-round">
        <div class="font-bold text-sm text-gray-300 mb-1">
          Mins Until Ban Exp.
        </div>
        <div class="text-sm text-gray-400">
          {{ iterationsData.isBanned ? `${iterationsData.minsUntilBanExpiry}mins` : 'Not Banned' }}
        </div>
      </div>
      <div class="w-6 border-1 border-50 p-1 border-round">
        <div class="font-bold text-sm text-gray-300 mb-1">
          Current Iter. Req.
        </div>
        <div class="text-sm text-gray-400">
          {{ iterationsData.currentRequests }}
        </div>
      </div>
    </div>
    <div class="flex flex-nowrap gap-2 mb-2">
      <div class="w-6 border-1 border-50 p-1 border-round">
        <div class="font-bold text-sm text-gray-300 mb-1">
          Next Iteration
        </div>
        <div class="text-sm text-gray-400">
          {{ tsToDate(iterationsData.nextIteration + 60) }}
        </div>
      </div>
      <div class="w-6 border-1 border-50 p-1 border-round">
        <div class="font-bold text-sm text-gray-300 mb-1">
          Pause Time Multip.
        </div>
        <div class="text-sm text-gray-400">
          {{ `x${iterationsData.throttleMultiplier}` }}
        </div>
      </div>
    </div>
    <div class="flex flex-nowrap gap-2 mb-2">
      <div class="w-6 border-1 border-50 p-1 border-round">
        <div class="font-bold text-sm text-gray-300 mb-1">
          Adjust. Pause Time
        </div>
        <div class="text-sm text-gray-400">
          {{ `${iterationsData.adjustedMillisBetweenRequests}ms` }}
        </div>
      </div>
      <div class="w-6 border-1 border-50 p-1 border-round">
        <div class="font-bold text-sm text-gray-300 mb-1">
          Default Pause Time
        </div>
        <div class="text-sm text-gray-400">
          {{ `${iterationsData.millisBetweenRequests}ms` }}
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import {utilsMixin} from "@/mixin/utils";

export default {
  name: "IterationStats",
  mixins: [utilsMixin],
  props: {
    iterationsData: {
      type: Object,
      required: true
    }
  },
  methods: {
    tsToDate(timeStamp) {
      return this.timestampToDate(timeStamp);
    }
  }
}
</script>