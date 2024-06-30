<template>
  <Card class="w-4">
    <template #content>
      <div
          class="flex justify-content-start align-items-center h-full mt-1 cursor-pointer"
          @click="this.$router.push({ name : 'playlist-view', params: { id }})">
          <span class="mr-2" style="font-size: 1.4rem">
               {{ emoji }}
          </span>
          <span class="font-bold text-xl white-space-nowrap overflow-hidden text-overflow-ellipsis">
            {{ playlistName }}
          </span>
      </div>
    </template>
    <template #end>
      <Button
          icon="pi pi-angle-right"
          label="Log Out"
          severity="secondary"
      />
    </template>
  </Card>
</template>
<script>
export default {
  name: "PlaylistName",
  props: {
    emoji: String,
    playlistName: String,
    id: String
  }
}
</script>
<style scoped>

</style>