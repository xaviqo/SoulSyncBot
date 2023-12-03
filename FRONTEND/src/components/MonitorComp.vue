<template>
  <div>
    <div class="w-full border-1 bg-white p-2 border-black-alpha-90 shadow-2 mt-2">
      <div class="flex w-full justify-content-between">
        <div class="flex">
          <div class="w-2rem h-2rem surface-50 border-1 border-black-alpha-90 flex align-items-center justify-content-center mr-3">
            <i :class="'pi pi-file'" style="font-size: 1.2rem"></i>
          </div>
          <div class="flex align-items-center text-xl font-bold">
            Logs
          </div>
        </div>
        <div class="flex flex-nowrap align-items-center">
          <span class="text-black-alpha-80 mr-2">Lines:</span>
          <Dropdown
              v-model="logLines.total"
              :options="logLines.options"
              @change="fetchLogs"
          />
        </div>
      </div>
    </div>
    <Card class="w-full border-1 border-black-alpha-90 shadow-2 mt-1">
      <template #content>
        <DataView :value="logLines.lines" paginator :rows="15">
          <template #list="line">
            <div class="text-xs">
              <span class="block my-1 text-700" v-if="line.index === 0">{{line.data}}</span>
              <span class="block my-1 text-600" v-else-if="line.index > 0 && line.index < 11">{{line.data}}</span>
              <span class="block my-1 text-500" v-else>{{line.data}}</span>
            </div>
          </template>
        </DataView>
      </template>
    </Card>
  </div>
</template>
<script>
import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";

export default {
  name: "MonitorComponent",
  inject: ["mq"],
  computed: {
    ...mapState(useUserCfgStore, ['isApiAlive'])
  },
  data: () => ({
    logLines: {
      lines: [],
      total: 25,
      options: [5,25,50,75,10,150,200,300,500]
    },
    refreshSec: 5
  }),
  methods: {
    fetchLogs(){
      this.axios.get(
          '/monitor/log',
          { params: { lines: this.logLines.total } }
      )
          .then( async res => {
            this.logLines.lines = res.data.reverse();
          })
          .catch( err => {
            console.error(err)
          })
    },
  },
  created() {
    this.fetchLogs();
    setTimeout( () => this.fetchLogs(), this.refreshSec * 1000);
  }
}
</script>
<style scoped>

</style>