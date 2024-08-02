<template>
  <div class="flex flex-wrap align-items-center gap-3">
    <Button
        v-for="key of Object.keys(apiStatus)"
        :key="key"
        v-tooltip="{ value: toolTips[key], showDelay:50, hideDelay:100 }"
        :label="`${apiStatus[key] ? '👍' : '👎'} ${key.toUpperCase()}`"
        :severity="apiStatus[key] ? 'success' : 'secondary'"
        class="w-12"
    />
  </div>
</template>
<script>
export default {
  name: "ApiConnections",
  props: {
    apiStatus: {
      type: Object,
      required: true
    },
    toolTips: {
      type: Object,
      required: true
    }
  }
}
</script>