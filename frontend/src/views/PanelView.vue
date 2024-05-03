<template>
  <main class="w-full flex justify-content-center">
    <AddNewPlaylistCard/>
  </main>
</template>
<script>
import AddNewPlaylistCard from "@/components/panel/AddNewPlaylistCard.vue";

export default {
  name: "PanelView",
  components: {AddNewPlaylistCard}
}
</script>
<style scoped>

</style>