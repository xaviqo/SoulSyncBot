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
            :value="interval.progress"
            class="w-full border-1 border-black-alpha-90"
        >{{ interval.sec }} <span class="ml-1" v-if="interval.sec > interval.refresh/3"> {{ ` / ${interval.refresh}` }}</span>
        </ProgressBar>
      </div>
      <div class="flex justify-content-center grid mt-3">
        <Dropdown
            v-model="interval.refresh"
            :options="options"
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
    min: 10,
    max: 120,
    step: 10,
    options: []
  }),
  methods: {
    setNewInterval(ev){
      this.interval.refresh = ev.value;
      clearInterval(this.interval.task);
      this.initInterval();
    },
    updateTimerBar(){
      this.interval.progress+=this.interval.increment;
      this.interval.sec+=1;
      if (this.interval.progress >= 100){
        this.resetInterval();
      }
    },
    initInterval(){
      this.resetInterval();
      this.interval.increment = (100 / this.interval.refresh);
      this.interval.task = setInterval(this.updateTimerBar,1000);
    },
    ...mapActions(useUserCfgStore,['resetInterval'])
  },
  created() {
    for (let i = this.min; i <= this.max ; i+=this.step) {
      this.options.push(i);
    }
    this.initInterval();
  },
  beforeUnmount() {
    clearInterval(this.interval.task);
  },
  computed: {
    ...mapState(useUserCfgStore, ['interval'])
  }
}
</script>
<style scoped>
#app > div.w-full.flex.align-items-center.justify-content-center > div > div > div.col-12.lg\:col-4 > div > div > div.p-card-content {
  padding: 0 !important;
}
</style>