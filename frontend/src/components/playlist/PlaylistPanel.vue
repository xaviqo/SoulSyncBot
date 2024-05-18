<template>
  <div class="flex justify-content-start gap-3">
    <Card class="w-6">
      <template #content>
      <span class="mr-2" style="font-size: 1.4rem">
           💿
      </span>
        <span class="font-bold text-xl">
        {{ playlistName }}
      </span>
      </template>
      <template #end>
        <Button
            icon="pi pi-angle-right"
            label="Log Out"
            severity="secondary"
        />
      </template>
    </Card>
    <Card class="w-6">
      <template #content>
        <div class="flex gap-3">
          <Button
              icon="pi pi-sign-out"
              label="Manage Downloads"
              severity="secondary"
          />
          <Button
              icon="pi pi-sign-out"
              label="Log Out"
              severity="secondary"
          />
        </div>
      </template>
    </Card>

  </div>
</template>
<script>
export default {
  name: "PlaylistPanel",
  props: {
    playlistName: null
  }
}
</script>
<style scoped>

</style>