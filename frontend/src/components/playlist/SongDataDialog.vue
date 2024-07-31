<template>
  <Dialog
      v-model:visible="visible"
      closable
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
            <InputText disabled class="bg-black-alpha-60" :value="getArtists()"/>
          </div>
          <div class="col-3 flex flex-column gap-2">
            <label for="username">Album</label>
            <InputText disabled class="bg-black-alpha-60" :value="songData?.album"/>
          </div>
          <div class="col-3 gap-2">
            <InputGroup class="flex flex-column gap-2">
              <label for="username">Search Input</label>
              <div class="w-full flex flex-nowrap">
                <InputText
                    :disabled="!modifySearchInput"
                    class="w-full bg-black-alpha-60 input-right-squared"
                    v-model="songData.searchInput"
                />
                <Button
                    v-if="!modifySearchInput"
                    class="input-left-squared"
                    v-tooltip="{ value: 'Modify search input', showDelay:50, hideDelay:100 }"
                    icon="pi pi-cog"
                    severity="secondary"
                    @click="switchModifySearchInput()"
                />
                <Button
                    v-else
                    class="input-left-squared"
                    v-tooltip="{ value: 'Save new search input', showDelay:50, hideDelay:100 }"
                    icon="pi pi-check"
                    severity="success"
                    @click="saveNewSearchInput()"
                />
              </div>
            </InputGroup>
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
          <div class="col-4 flex flex-column gap-2">
            <label for="username">Copy Route</label>
            <InputText disabled class="bg-black-alpha-60" :value="songData?.copyRoute"/>
          </div>
          <div class="col-2 flex flex-column justify-content-end gap-2">
            <Button
                label="Reset Download"
                v-tooltip="{ value: 'Download the file again', showDelay:50, hideDelay:100 }"
                severity="secondary"
                @click="resetDownload(false)"
            />
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
    modifySearchInput: false
  }),
  created() {
    this.emitter.on('song-data-dialog', this.handleSongDataDialog);
    this.emitter.on('reset-download', this.handleResetDownload);
  },
  methods: {
    handleResetDownload(isReset) {
      if (isReset) this.resetDownload(true);
    },
    resetDownload(isReset) {
      const songName = `${this.songData?.name} - ${this.getArtistsByComa(this.songData?.artists)}`;
      if (isReset) {
        this.$axios
            .post(`/download-list/${this.songData?.id}/reset`)
            .then( () => {
              this.emitter.emit('alert', {
                severity: 'success',
                message: `Download for track ${songName} successfully restarted`
              });
              this.emitter.emit('refresh');
            });
      } else {
        this.emitter.emit('confirm',{
          header: `Do you want to restart the download of the track ${songName}?`,
          message: `The downloaded file will be deleted and the track will be put back in the pending download queue.`,
          reject: 'Cancel',
          accept: 'Restart',
          listenerLabel: 'reset-download'
        });
      }
    },
    saveNewSearchInput() {
      const songName = `${this.songData?.name} - ${this.getArtistsByComa(this.songData?.artists)}`;
      this.$axios
          .post(`/download-list/${this.songData?.id}/search-input`, { searchInput: this.songData?.searchInput })
          .then( () => {
            this.emitter.emit('alert', {
              severity: 'success',
              message: `New search input for track ${songName} successfully saved`
            });
          });
    },
    switchModifySearchInput() {
      this.modifySearchInput = !this.modifySearchInput;
    },
    handleSongDataDialog(songData) {
      this.visible = true;
      this.modifySearchInput = false;
      this.songData = songData;
    },
    getFileName(){
      return this.songData?.filename?.split('\\')?.pop();
    },
    getDialogName(){
      return `
      ${this.getArtists()} -
      ${this.songData?.name}
      `
    },
    getArtists(){
      return this.getArtistsByComa(this.songData?.artists);
    }
  },
  beforeUnmount() {
    this.emitter.off('song-data-dialog', this.handleSongDataDialog);
    this.emitter.off('reset-download', this.handleResetDownload);
  }
}
</script>