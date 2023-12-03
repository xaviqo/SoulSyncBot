<template>
  <div class="grid w-full">
    <ConfirmPopup></ConfirmPopup>
    <div class="col-6">
      <div class="w-full border-1 bg-white p-2 border-black-alpha-90 shadow-2 mt-2">
        <div class="flex w-full justify-content-between">
          <div class="flex">
            <div class="w-2rem h-2rem surface-50 border-1 border-black-alpha-90 flex align-items-center justify-content-center mr-3">
              <i :class="'pi pi-compass'" style="font-size: 1.2rem"></i>
            </div>
            <div class="flex align-items-center text-xl font-bold">
              Current queue
            </div>
          </div>
        </div>
      </div>
      <Card class="w-full border-1 border-black-alpha-90 shadow-2 mt-1">
        <template #content>
          <div v-if="queue.length < 1" class="w-full p-4 text-xl text-black-alpha-60">
            <i
                class="pi pi-spin pi-spinner mr-2"
                :style="'font-size: 1rem; color: lightslategray'"
            ></i>
            No songs in queue...
          </div>
          <DataView v-else :value="queue">
            <template #list="track">
              <div class="w-full mb-1">
                <div class="text-xl font-bold text-900">{{ track.data.name + " - " + track.data.artists.join(", ") }}</div>
                <div class="w-full flex flex-nowrap">
                  <div class="mr-3">
                    <span class="text-900">Attempts: </span>
                    <span>{{ track.data.attempts }}</span>
                  </div>
                  <div class="mr-3">
                    <span class="text-900">Added: </span>
                    <span class="text-700">{{ getElapsedTime(track.data.added) }}</span>
                  </div>
                </div>
              </div>
            </template>
          </DataView>
        </template>
      </Card>
    </div>
    <div class="col-6">
      <div class="w-full border-1 bg-white p-2 border-black-alpha-90 shadow-2 mt-2">
        <div class="flex w-full justify-content-between">
          <div class="flex">
            <div class="w-2rem h-2rem surface-50 border-1 border-black-alpha-90 flex align-items-center justify-content-center mr-3">
              <i :class="'pi pi-cog'" style="font-size: 1.2rem"></i>
            </div>
            <div class="flex align-items-center text-xl font-bold">
              Reboot queue
            </div>
          </div>
        </div>
      </div>
      <Card class="w-full border-1 border-black-alpha-90 shadow-2 mt-1">
        <template #content>
          <div class="w-full flex justify-content-center">
            <div
                @click="reboot($event)"
                class="m-2 p-3 border-1 border-black-alpha-90 bg-red-100 hover:bg-orange-100 cursor-pointer shadow-1 mr-4"
            >
              <i :class="'mr-2 pi pi-sync'" style="font-size: .9rem" />
              Reboot Queue
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>
<script>
import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";
import {soulmixin} from "@/mixins/soulmixin";

export default {
  name: "ProcessComp",
  mixins: [soulmixin],
  inject: ["mq"],
  computed: {
    ...mapState(useUserCfgStore, ['isApiAlive'])
  },
  data: () => ({
    queue: []
  }),
  methods: {
    reboot(event){
      this.$confirm.require({
        target: event.currentTarget,
        message: 'Restarting the process will stop the running searches. Are you sure you want to proceed?',
        icon: 'pi pi-exclamation-triangle',
        accept: () => this.sendReboot()
      });
    },
    fetchQueue(){
      this.axios.get('/monitor/queue')
          .then( res => {
            this.queue = res.data;
          })
          .catch( err => {
            console.error(err)
          })
    },
    sendReboot(){
      this.axios.post('/configuration/reboot')
          .then( () => {
            this.emitter.emit('show-alert',{
              info: 'Process rebooted',
              icon: 'pi-info',
              severity: 'success'
            });
            this.fetchQueue();
          })
          .catch( err => {
            console.error(err)
          });
    },
  },
  created() {
    this.fetchQueue();
    setInterval( () => this.fetchQueue(), 1000);
  }
}
</script>
<style scoped>

</style>