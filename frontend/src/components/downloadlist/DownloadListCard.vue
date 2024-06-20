<template>
  <Card class="-my-1">
    <template #content>
      <div class="flex gap-3 align-items-center -my-2" v-if="downloadList">
        <div class="w-2 flex justify-content-start gap-3">
          <Button v-if="showGlass" icon="pi pi-search" class="mr-2" severity="success" @click="doAction('show')"/>
          <Button v-else icon="pi pi-cog" class="mr-2" severity="info" @click="doAction('policy')"/>
          <Button icon="pi pi-trash" class="mr-2" severity="danger" @click="doAction('delete')"/>
        </div>
        <div class="w-10 flex gap-3">
          <div class="w-2 flex flex-column gap-1">
            <div class="text-center">
              Priority
            </div>
            <div
                class="border-round text-center font-bold"
                :style="{ backgroundColor: PriorityColor[downloadList?.priority] }"
            >
              {{ downloadList?.priority ?? 'N/A' }}
            </div>
          </div>
          <div class="w-2 flex flex-column gap-1">
            <div class="text-center">
              Activated
            </div>
            <div
                class="border-round text-center w-full h-full font-bold"
                :style="{
                backgroundColor: downloadList?.isActive ? '#86EFAC' : '#FCA5A5',
                color: downloadList?.isActive ? 'black' : 'white'
            }"
            >
              {{ downloadList?.isActive ? 'ACTIVATED' : 'DEACTIVATED' }}
            </div>
          </div>
          <div class="w-2 flex flex-column gap-1">
            <div class="text-center">
              Attempts
            </div>
            <div class="bg-gray-800 border-round text-center">
              {{ downloadList?.attempts ?? 'N/A' }}
            </div>
          </div>
          <div class="w-2 flex flex-column gap-1">
            <div class="text-center">
              Last Retry
            </div>
            <div class="bg-gray-800 border-round text-center text-sm w-full h-full flex align-items-center justify-content-center">
              {{ getLastRetryDate(downloadList?.lastCheck) ?? 'N/A' }}
            </div>
          </div>
          <div class="w-4 flex flex-column gap-1">
            <div class="text-center">
              Total Completed
            </div>
            <ProgressBar :value="getProgressBarValue(downloadList?.totalTracks ?? 0, downloadList?.totalCompleted ?? 0)">
              {{ downloadList?.totalCompleted ?? 0 }}/{{ downloadList?.totalTracks ?? 0 }}
            </ProgressBar>
          </div>
        </div>
      </div>
      <DownloadListConfigurationDialog />
    </template>
  </Card>
</template>

<script>
import PriorityColor from "@/model/PriorityColor";
import {utilsMixin} from "@/mixin/utils";
import DownloadListConfigurationDialog from "@/components/downloadlist/DownloadListConfigurationDialog.vue";

export default {
  name: "DownloadListCard",
  components: {DownloadListConfigurationDialog},
  mixins: [utilsMixin],
  computed: {
    PriorityColor() {
      return PriorityColor
    }
  },
  props: {
    downloadList: {
      type: Object,
      required: true
    },
    playlistId: {
      type: String,
      required: true
    },
    showGlass: {
      type: Boolean,
      required: true
    }
  },
  methods: {
    getProgressBarValue(total, succeeded){
      return Math.round(( succeeded / total ) * 100);
    },
    getLastRetryDate(ts) {
      return this.timestampToDate(ts);
    },
    doAction(action) {
      switch (action) {
        case 'show':
          this.$router.push({
            name: 'download-list-view',
            params: {
              pl: this.playlistId,
              dl: this.downloadList?.id
            }
          });
          break;
        case 'policy':
          this.emitter.emit('download-list-data-dialog', this.downloadList);
          break;
      }
    }
  }
}
</script>