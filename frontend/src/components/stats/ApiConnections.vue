<template>
  <div class="flex flex-wrap align-items-center gap-3">
    <Button
        label="SPOTIFY"
        class="w-12"
    />
    <Button
        label="SLSKD"
        class="w-12"
    />
    <Button
        label="SOULSEEK"
        class="w-12"
    />
  </div>
</template>
<script>
export default {
  name: "ApiConnections",
  props: {
    apiStatus: {
      type: Array,
      required: true
    }
  }
}
</script>