<template>
  <div class="mt-2 grid w-full">
    <div
        class="col-12 lg:col-6"
        v-for="card in configurationCards"
        :key="card.section"
    >
      <ConfigurationComponent :section="card"/>
    </div>
  </div>
</template>

<script>
import ConfigurationComponent from "@/components/ConfigurationComp.vue";

export default {
  name: 'ConfigurationView',
  components: {ConfigurationComponent},
  data: () => ({
    configurationCards: [
      { name: 'Bot', icon: 'pi-sync' , section: 'bot' },
      { name: 'Search', icon: 'pi-search' , section: 'finder' },
      { name: 'APIs', icon: 'pi-globe' , section: 'api' },
    ]
  }),
  methods: {
  },
  mounted() {
  }
}
</script>