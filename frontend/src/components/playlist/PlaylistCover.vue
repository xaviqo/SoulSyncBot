<template>
  <Card class="w-12 p-0 m-0">
    <template #content>
      <img :src="playlistCover" width="100%" class="border-round" :alt="playlistCover"/>
    </template>
  </Card>
</template>
<script>
export default {
  name: "PlaylistCover",
  props: {
    playlistCover: null
  }
}
</script>