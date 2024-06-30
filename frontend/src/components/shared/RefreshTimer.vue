<template>
  <Card>
    <template #content>
      <div class="w-full flex justify-content-center align-content-center mb-3 text-sm">
        {{ `Next refresh in ${getThreshold - getCurrent} sec` }}
      </div>
      <ProgressBar :value="getProgress">
        <div></div>
      </ProgressBar>
    </template>
  </Card>
</template>
<script>
import {mapActions, mapState} from "pinia";
import {useTimerStore} from "@/store/timer-manager";

export default {
  name: "RefreshTimer",
  data: () =>({
    timer: null
  }),
  watch: {
    getCurrent(newVal) {
      if (newVal >= this.getThreshold)
        this.onThresholdReached();
    }
  },
  methods: {
    onThresholdReached() {
      this.emitter.emit("refresh");
    },
    async initTimer() {
      await this.loadTimerValues();
      this.timer = setInterval(() => this.updateTimer() , 1000)
    },
    ...mapActions(useTimerStore, ['updateTimer','loadTimerValues'])
  },
  computed: {
    ...mapState(useTimerStore, {
      getCurrent: 'getCurrent',
      getThreshold: 'getThreshold',
      getMin: 'getMin',
      getMax: 'getMax',
      getProgress: 'getProgress',
    })
  },
  created() {
    this.initTimer();
  },
  beforeUnmount() {
    clearInterval(this.timer);
  }
}
</script>
<style scoped>

</style>