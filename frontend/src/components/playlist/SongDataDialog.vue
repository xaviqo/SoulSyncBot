<template>
  <Dialog
      v-model:visible="visible"
      :header="getDialogName()"
      class="p-1 col-12 lg:col-10 xl:col-8"
      :pt="{
        root: 'border-none',
        mask: { style: 'backdrop-filter: blur(2px)' }
    }"
  >
    <Card>
      <template #content>
        <div class="grid mb-5 text-white-alpha-90">
          <div class="col-3 flex flex-column gap-2">
            <label for="username">Song Name</label>
            <InputText disabled class="bg-black-alpha-60" :value="songData?.name"/>
          </div>
          <div class="col-3 flex flex-column gap-2">
            <label for="username">Artists</label>
              <InputText disabled class="bg-black-alpha-60" :value="getArtistsByComa()"/>
          </div>
          <div class="col-3 flex flex-column gap-2">
            <label for="username">Album</label>
            <InputText disabled class="bg-black-alpha-60" :value="songData?.album"/>
          </div>
          <div class="col-3 flex flex-column gap-2">
            <label for="username">Search Input</label>
            <InputText disabled class="bg-black-alpha-60" :value="songData?.searchInput"/>
          </div>
        </div>
        <div class="grid mb-5 text-white-alpha-90">
          <div class="col-2 flex flex-column gap-2">
            <label for="username">Status</label>
            <InputText disabled class="bg-black-alpha-60" :value="songData?.status"/>
          </div>
          <div class="col-2 flex flex-column gap-2">
            <label for="username">Attempts</label>
            <InputText disabled class="bg-black-alpha-60" :value="songData?.attempts"/>
          </div>
          <div class="col-2 flex flex-column gap-2">
            <label for="username">Added</label>
            <InputText disabled class="bg-black-alpha-60" :value="timestampToDate(songData?.added)"/>
          </div>
          <div class="col-2 flex flex-column gap-2">
            <label for="username">Last Check</label>
            <InputText disabled class="bg-black-alpha-60" :value="timestampToDate(songData?.lastCheck)"/>
          </div>
          <div class="col-2 flex flex-column gap-2">
            <label for="username">Size</label>
            <InputText disabled class="bg-black-alpha-60" :value="bitsToSize(songData?.size)"/>
          </div>
          <div class="col-2 flex flex-column gap-2">
            <label for="username">Bitrate</label>
            <InputText disabled class="bg-black-alpha-60" :value="songData?.bitRate"/>
          </div>
        </div>
        <div class="grid text-white-alpha-90">
          <div class="col-3 flex flex-column gap-2">
            <label for="username">Shared By</label>
            <InputText disabled class="bg-black-alpha-60" :value="songData?.sharedBy"/>
          </div>
          <div class="col-3 flex flex-column gap-2">
            <label for="username">Filename</label>
            <InputText disabled class="bg-black-alpha-60" :value="getFileName()"/>
          </div>
          <div class="col-6 flex flex-column gap-2">
            <label for="username">Copy Route</label>
            <InputText disabled class="bg-black-alpha-60" :value="songData?.copyRoute"/>
          </div>
        </div>
      </template>
    </Card>
  </Dialog>

</template>
<script>
import {utilsMixin} from "@/mixin/utils";

export default {
  name: "SongData",
  mixins: [utilsMixin],
  data: () => ({
    songData: null,
    visible: false,
  }),
  created() {
    this.emitter.on('song-data-dialog', songData => {
      this.visible = true;
      this.songData = songData;
    });
  },
  methods: {
    getFileName(){
      return this.songData?.filename?.split('\\')?.pop();
    },
    getDialogName(){
      return `
      ${this.getArtistsByComa()} -
      ${this.songData?.name}
      `
    },
    getArtistsByComa(){
      return this.songData?.artists?.map(a => a.name).join(", ");
    }
  }
}
</script>