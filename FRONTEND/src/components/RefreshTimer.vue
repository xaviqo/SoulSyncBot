<template>
  <Card class="border-1 border-black-alpha-90 shadow-2 h-10rem">
    <template #title>
      <div class="grid text-base">
        Next Refresh (sec):
      </div>
    </template>
    <template #content>
      <div class="w-full flex justify-content-center -mt-2">
        <ProgressBar
            :value="this.interval.progress"
            class="w-full border-1 border-black-alpha-90"
        > {{ this.interval.sec }}/{{ this.refresh }}
        </ProgressBar>
      </div>
      <div class="flex justify-content-center grid mt-3">
        <Dropdown
            v-model="refresh"
            :options="this.interval.options"
            @change="setNewInterval"
        />
      </div>
    </template>
  </Card>
</template>
<script>
import {mapActions, mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";

export default {
  name: "RefreshConfigComp",
  inject: ["mq"],
  data: () => ({
    interval: {
      min: 10,
      max: 120,
      step: 10,
      options: [],
      progress: 0,
      task: null,
      incrementValue: 0,
      sec: 0
    }
  }),
  methods: {
    setNewInterval(ev){
      this.setRefreshInterval(ev.value);
      clearInterval(this.interval.task);
      this.interval.incrementValue = 100 / this.refresh;
      this.interval.progress=0;
      this.interval.sec=0;
      this.interval.task = setInterval(this.updateTimerBar,1000);
    },
    updateTimerBar(){
      this.interval.progress+=this.interval.incrementValue;
      this.interval.sec+=1;

      if (this.interval.progress >= 100) {
        this.interval.progress = 0;
        this.interval.sec = 0;
        this.emitter.emit('trigger-refresh');
      }
    },
    ...mapActions(useUserCfgStore, [
      'setRefreshInterval',
    ])
  },
  computed: {
    ...mapState(useUserCfgStore, ['refresh'])
  },
  created() {
    for (let i = this.interval.min; i <= this.interval.max ; i+=this.interval.step) {
      this.interval.options.push(i);
    }
    this.interval.incrementValue = 100 / this.refresh;
    this.interval.task = setInterval(this.updateTimerBar,1000);
    this.emitter.on('force-refresh', () => this.setNewInterval(this.refresh));
    this.emitter.emit('trigger-refresh');
  },
  beforeUnmount() {
    clearInterval(this.interval.task);
  }
}
</script>
<style scoped>
#app > div.w-full.flex.align-items-center.justify-content-center > div > div > div.col-12.lg\:col-4 > div > div > div.p-card-content {
  padding: 0 !important;
}
</style>