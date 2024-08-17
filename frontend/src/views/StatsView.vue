<template>

  <div class="w-12 flex gap-3">
    <Card class="w-2">
      <template #title>
        Version
      </template>
      <template #content>
        <Button
            :icon="appVersion.alertData.severity === 'INFO' ? 'pi pi-check' : 'pi pi-sync'"
            v-tooltip.bottom="{ value: appVersion.alertData.message, showDelay:50, hideDelay:100 }"
            :label="appVersion.current"
            :severity="appVersion.alertData.severity"
            class="w-12 opacity-80"
            @click="fetchSoulSyncVersion()"
        />
      </template>
    </Card>
    <Card v-for="ps in summaryData" :key="ps.title" class="flex-1" style="max-width: 200px;">
      <template #title>
        {{ ps.title }}
      </template>
      <template #content>
        <div class="flex justify-content-center align-items-end font-medium text-color-secondary">
          <span class="white-space-nowrap overflow-hidden text-overflow-ellipsis">
            {{ ps.value ? ps.value : 'N/A' }}
          </span>
        </div>
      </template>
    </Card>
  </div>
  <div v-if="showMeters" class="w-12 flex gap-3">
    <Card class="w-4">
      <template #title>
        Success Rate
      </template>
      <template #content>
        <MeterGroup
            :value="successRate.data"
            :max="successRate.max"
        />
      </template>
    </Card>
    <Card class="w-4">
      <template #title>
        Finding Logic Applied
      </template>
      <template #content>
        <MeterGroup
            :value="findLogicStats.data"
            :max="findLogicStats.max"
        />
      </template>
    </Card>
    <Card class="w-4">
      <template #title>
        Downloads Status
      </template>
      <template #content>
        <MeterGroup
            :value="countByStatus.data"
            :max="countByStatus.max"
        />
      </template>
    </Card>
  </div>
  <div class="w-12 flex gap-3">
    <Card class="w-2">
      <template #title>
        APIs Status
      </template>
      <template #content>
        <ApiConnections
            :api-status="apiStatus"
            :tool-tips="{ soulseek: 'Status of the temporary ban on the SoulSeek network', spotify: 'Spotify API connection status', slskd: 'Slskd API connection status'}"
        />
      </template>
    </Card>
    <Card class="w-4">
      <template #title>
        Iteration Data
      </template>
      <template #content>
        <IterationStats
            :iterations-data="iterationsData"
        />
      </template>
    </Card>
    <Card class="w-4">
      <template #title>
        Last Iterations <span class="font-light text-gray-500 text-sm">(1min. aprox. x iteration)</span>
      </template>
      <template #content>
        <DataTable
            v-if="Object.keys(iterationsData.lastIterations).length > 0"
            :value="formatIterationMaps(iterationsData.lastIterations)"
            scrollable
            scrollHeight="220px"
        >
          <Column header="Finished">
            <template #body="slotProps">
              {{ tsToDate(slotProps.data.timeStamp) }}
            </template>
          </Column>
          <Column field="requests" header="Requests"></Column>
        </DataTable>
        <div v-else class="w-full flex justify-content-center text-gray-600 font-bold text-xl p-3">
          No iterations found
        </div>
      </template>
    </Card>
    <Card class="w-4">
      <template #title>
        Last Bans
      </template>
      <template #content>
        <DataTable
            v-if="Object.keys(iterationsData.lastBans).length > 0"
            :value="formatIterationMaps(iterationsData.lastBans)"
            scrollable
            scrollHeight="220px"
        >
          <Column header="Finished">
            <template #body="slotProps">
              {{ tsToDate(slotProps.data.timeStamp) }}
            </template>
          </Column>
          <Column field="requests" header="Requests"></Column>
        </DataTable>
        <div v-else class="w-full flex justify-content-center text-gray-600 font-bold text-xl p-3">
          No ban has been applied
        </div>
      </template>
    </Card>
  </div>
  <div class="w-12 flex gap-3">
    <Card class="w-12">
      <template #title>
        Current Queue
      </template>
      <template #content>
        <QueueStatus
            :threads="queueStatus"
        />
      </template>
    </Card>
  </div>
</template>
<script>

import ApiConnections from "@/components/stats/ApiConnections.vue";
import QueueStatus from "@/components/stats/QueueStatus.vue";
import IterationStats from "@/components/stats/IterationStats.vue";
import {utilsMixin} from "@/mixin/utils";

export default {
  name: "StatsView",
  mixins: [utilsMixin],
  components: {IterationStats, QueueStatus, ApiConnections},
  created() {
    this.emitter.emit('loading', {show: true, text: `Loading SoulSync Stats...`});
    this.fetchApiStatus();
    this.fetchIterationStats();
    this.fetchQueueStatus();
    this.fetchSummary();
    this.fetchFindLogicStats();
    this.fetchCountByStatus();
    this.fetchSoulSyncVersion();
    this.apiStatusInterval = setInterval(this.fetchApiStatus,30000);
    this.countByStatusInterval = setInterval(this.fetchCountByStatus,5000);
    this.findingLogicInterval = setInterval(this.fetchFindLogicStats,5000);
    this.iterationStatsInterval = setInterval(this.fetchIterationStats,5000);
    this.fetchSummaryInterval = setInterval(this.fetchSummary,5000);
    this.fetchQueueStatusInterval = setInterval(this.fetchQueueStatus,3000);
  },
  data: () => ({
    apiStatusInterval: null,
    iterationStatsInterval: null,
    fetchSummaryInterval: null,
    fetchQueueStatusInterval: null,
    findingLogicInterval: null,
    countByStatusInterval: null,
    queueStatus: [],
    summaryData: [],
    findLogicStats: {
      data: [],
      max: 0
    },
    countByStatus: {
      data: [],
      max: 0
    },
    successRate: {
      data:[],
      max: 0
    },
    iterationsData: {
      banExpirationTime: 0,
      nextIteration: 0,
      lastBans: {},
      lastIterations: {},
      currentRequests: 0,
      isBanned: false,
      minsUntilBanExpiry: 0,
      throttleMultiplier: 0,
      adjustedMillisBetweenRequests: 0,
      millisBetweenRequests: 0
    },
    apiStatus: {},
    appVersion: {
      current: '',
      latest: '',
      alertData: {
        message: '',
        severity: ''
      }
    },
    initStatsCounter: 0
  }),
  methods: {
    getValue(data,field) {
      return data[field] ? data[field] : 0;
    },
    fetchSoulSyncVersion() {
      this.$axios
          .get('/cfg/app-version')
          .then( res => {
            console.log(res.data)
            this.appVersion = res.data;
            this.increaseInitStats();
          });
    },
    fetchApiStatus() {
      this.$axios
          .get('/stats/apis-status')
          .then( res => {
            this.apiStatus = res.data;
            this.increaseInitStats();
          });
    },
    fetchQueueStatus() {
      this.$axios
          .get('/stats/queue')
          .then( res => {
            this.queueStatus = res.data
            this.increaseInitStats();
          });
    },
    fetchIterationStats() {
      this.$axios
          .get('/stats/iterations')
          .then( res => {
            this.iterationsData = res.data;
            this.increaseInitStats();
          });
    },
    fetchSummary() {
      this.$axios
          .get('/stats/summary')
          .then( res => {
            this.summaryData = [
              { title: 'Running Time', value: this.getValue(res.data, 'runningTime') },
              { title: 'Total', value: this.getValue(res.data, 'totalProcessed') },
              { title: 'Total Success', value: this.getValue(res.data, 'totalSuccess') },
              { title: 'Total Failed', value: this.getValue(res.data, 'totalFailed') },
              { title: 'Last Success', value: this.getValue(res.data, 'lastSuccess') },
              { title: 'Last Failed', value: this.getValue(res.data, 'lastFailed') },
            ];
            this.successRate.data = [
              { label: 'Success', value: this.getValue(res.data, 'totalSuccess'), color: '#34d399' },
              { label: 'Failure', value: this.getValue(res.data, 'totalFailed'), color: '#c084fc' },
            ]
            this.successRate.max = this.getValue(res.data, 'totalProcessed');
            this.increaseInitStats();
          });
    },
    fetchCountByStatus() {
      this.$axios
          .get('/stats/downloads-status')
          .then( res => {
            const searching = this.getValue(res.data, 'SEARCHING');
            const findingFile = this.getValue(res.data, 'FINDING_FILE');
            this.countByStatus = {
              data: [
                { label: 'Searching', value: (searching+findingFile), color: '#00fff7' },
                { label: 'Downloading', value: this.getValue(res.data, 'DOWNLOADING'), color: '#ffa10a' },
                { label: 'Completed', value: this.getValue(res.data, 'COMPLETED'), color: '#04e762' },
                { label: 'Copied', value: this.getValue(res.data, 'COPIED'), color: '#db00b6' },
                { label: 'Waiting', value: this.getValue(res.data, 'WAITING'), color: '#6c757d' },
              ],
              max: Object.values(res.data).reduce((a, b) => a + b, 0)
            };
            this.increaseInitStats();
          });
    },
    fetchFindLogicStats() {
      this.$axios
          .get('/stats/finding-logic')
          .then( res => {
            this.findLogicStats = {
              data: [
                { label: 'Flexible', value: res.data.totalFlexible, color: '#60a5fa' },
                { label: 'Strict', value: res.data.totalStrict, color: '#fbbf24' },
              ],
              max: res.data.totalFlexible + res.data.totalStrict
            };
            this.increaseInitStats();
          });
    },
    showMeters() {
      const showCountByStatus = this.countByStatus.max > 0;
      const showFindLogicStats = this.findLogicStats.max > 0;
      const showSuccessRate = this.successRate.max > 0;
      return showCountByStatus
          && showFindLogicStats
          && showSuccessRate;
    },
    formatIterationMaps(data) {
      return Object.keys(data).map(key => ({
        timeStamp: key,
        requests: data[key]
      })).sort( (a,b) => b.timeStamp - a.timeStamp);
    },
    tsToDate(timeStamp) {
      return this.timestampToDate(timeStamp);
    },
    increaseInitStats() {
      if (this.initStatsCounter < 6)
        this.initStatsCounter++;
      else
        this.emitter.emit('loading', {show: false});
    }
  },
  beforeUnmount() {
    clearInterval(this.iterationStatsInterval);
    clearInterval(this.fetchSummaryInterval)
    clearInterval(this.fetchQueueStatusInterval);
    clearInterval(this.apiStatusInterval);
    clearInterval(this.countByStatusInterval);
    clearInterval(this.findingLogicInterval);
  }
}
</script>