<template>
  <div class="flex flex-wrap align-items-center gap-3">
    <Button
        v-for="key of Object.keys(apiStatus)"
        :key="key"
        :icon="apiStatus[key] ? 'pi pi-check' : 'pi pi-times'"
        v-tooltip="{ value: toolTips[key], showDelay:50, hideDelay:100 }"
        :label="key.toUpperCase()"
        :severity="apiStatus[key] ? 'success' : 'secondary'"
        iconPos="bottom"
        class="w-12 opacity-80"
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