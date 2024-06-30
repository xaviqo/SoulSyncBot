<template>
  <Card>
    <template #content>
      <TabView
          class="w-full border-round-xl overflow-hidden"
          :scrollable="true"
          @tab-change="event => activeIndex = event.index"
      >
        <TabPanel
            v-for="tab in sections"
            :key="tab.title"
            :header="tab.name"
        >
          <div v-if="tab.section === 'policies'">
            search policies
          </div>
          <div v-else-if="tab.section === 'users'">
            usuarios
          </div>
          <div v-else class="flex flex-wrap gap-3">
            <div class="mt-2">
              <ConfigurationFields
                  :disabled="false"
                  :fields="getSectionFields(tab.section.toUpperCase())"
              />
            </div>
            <div class="w-full flex justify-content-end">
              <Button
                  label="Save"
                  security="success"
                  class="border-1 border-white-alpha-30 hover:bg-green-alpha-10"
                  @click="saveConfiguration()"
              ></Button>
            </div>
          </div>
        </TabPanel>
      </TabView>
    </template>
  </Card>
</template>
<script>
import ConfigurationFields from "@/components/shared/ConfigurationFields.vue";

export default {
  name: "ConfigurationView",
  components: {ConfigurationFields},
  data: () =>  ({
    sections: [
      { name: `API's Configuration`, section: 'api' },
      { name: 'Search/Download', section: 'search' },
      { name: 'Maintenance', section: 'maintenance' },
      { name: `Search Policies`, section: 'policies' },
      { name: 'Users', section: 'users' }
    ],
    fields: [],
    activeIndex: 0
  }),
  created() {
    this.fetchSections();
  },
  methods: {
    fetchSections() {
      this.emitter.emit('loading', {show: true, text: `Loading SoulSync configuration...`});
      const sections = this.sections
          .map( s => s.section )
          .join(',');
      this.$axios
          .get('/cfg/fields', { params: { sections, value: true }})
          .then( res => {
            this.fields = res.data;
            this.emitter.emit('loading', {show: false});
          });
    },
    getSectionFields(section) {
      return this.fields.filter( f => f.section === section);
    },
    saveConfiguration() {
      const section = this.sections[this.activeIndex]?.section;
      this.emitter.emit('loading', {show: true, text: `Saving ${section} configuration...`});
      const payload = this.getSectionFields(section.toUpperCase());
      this.$axios
          .post('/cfg/fields', payload)
          .then((e) => {
            this.emitter.emit('loading', {show: false});
            if (e?.status === 200) {
              this.emitter.emit('alert', {
                severity: 'success',
                message: `SoulSync configuration successfully updated`
              });
            }
          })
    }
  }
 }
</script>
<style scoped>

</style>