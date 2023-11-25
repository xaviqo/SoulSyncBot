<template>
  <Card class="border-1 border-black-alpha-90 shadow-2 w-full">
    <template #title>
      <div class="grid">
        <div class="col-4 flex flex-nowrap justify-content-start">
          <div class="w-2rem h-2rem surface-50 border-1 border-black-alpha-90 flex align-items-center justify-content-center mr-3">
            <i class="pi pi-chevron-right" style="font-size: 1.2rem"></i>
          </div>
          <div class="flex align-items-center">
            Add new playlist
          </div>
        </div>
        <div class="col font-normal text-base pt-3">
          <div class="card flex flex-wrap justify-content-center gap-3">
            <div class="flex align-items-center pr-2">
              <Button
                  icon="pi pi-info"
                  severity="secondary"
                  rounded
                  text
                  size="small"
                  v-tooltip.left="info.avoidDuplicates"
                  style="width: .6rem; height: 1rem; cursor: default"
                  class="mr-1"
              />
              <Checkbox v-model="payload.avoidDuplicates" inputId="ingredient1" binary />
              <label for="ingredient1" class="ml-2"> Avoid duplicates </label>
            </div>
            <div class="flex align-items-center">
              <Button
                  icon="pi pi-info"
                  severity="secondary"
                  rounded
                  text
                  size="small"
                  v-tooltip.left="info.update"
                  style="width: .6rem; height: 1rem; cursor: default"
                  class="mr-1"
              />
              <Checkbox v-model="payload.update" inputId="ingredient2" binary />
              <label for="ingredient2" class="ml-2"> Autoupdate </label>
            </div>
          </div>
        </div>
      </div>
    </template>
    <template #content>
      <div class="mt-2">
        <InputText
            v-tooltip.top="'You can add either the playlist URL or Id'"
            v-model="payload.playlist"
            id="pl-input"
            class="surface-50 border-1 border-black-alpha-90 mt-2 w-10 shadow-1"
            placeholder="Playlist URL or Spotify Id"
        />
        <Button
            type="button"
            :label="mq.mdPlus ? 'Add' : ''"
            icon="pi pi-plus"
            badgeClass="p-badge-danger"
            class="border-1 border-black-alpha-90 w-2 shadow-1"
            @click="sendPlaylist"
            :disabled="!isApiAlive"
        />
      </div>
    </template>
  </Card>
</template>
<script>
import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";

export default {
  name: 'AddPlaylistComp',
  inject: ["mq"],
  data: () => ({
    sending: false,
    payload: {
      playlist: null,
      avoidDuplicates: true,
      avoidLive: true,
      avoidRemix: true,
      update: true
    },
    info: {
      avoidDuplicates: 'Duplicate songs in the playlist will be added only once',
      update: 'Automatically updates the playlist waiting for new additions'
    }
  }),
  methods: {
    sendPlaylist(){
      if (!this.payload.playlist) return;
      this.sending = true;
      this.axios.post('/playlist',this.payload)
          .then( () => {
            this.emitter.emit('show-alert',{
              info: 'Playlist added to watchlist',
              icon: 'pi-check-circle',
              severity: 'success'
            });
          })
          .catch( (err) => {
            this.emitter.emit('show-alert',{
              info: err.response.data.message,
              icon: 'pi-exclamation-circle',
              severity: 'error'
            });
          });
      this.sending = false;
      this.payload.playlist = null;
      this.payload = {
        playlist: null,
        avoidDuplicates: true,
        update: true
      };
    },
    loadIsMobile(){
    }
  },
  mounted() {
    this.loadIsMobile();
    window.addEventListener('resize',this.loadIsMobile);
  },
  computed: {
    ...mapState(useUserCfgStore, ['isApiAlive'])
  }
}
</script>