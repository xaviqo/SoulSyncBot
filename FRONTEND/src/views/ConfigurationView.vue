<template>
  <div class="w-full flex justify-content-center mt-2">
    <div
        v-for="cfg in configurationCards"
        class="p-2 border-1 border-black-alpha-90 surface-100 hover:surface-200 cursor-pointer shadow-1 mr-4"
        :key="cfg.section"
        @click="changeCfgCard(cfg)"
    >
      <i :class="'mr-2 pi '+cfg.icon" style="font-size: .9rem" />
      {{ cfg.name }}
    </div>
  </div>
  <div class="mt-2 grid w-full">
    <ConfigurationComponent v-if="cfgCard != null" :section="cfgCard"/>
  </div>
</template>

<script>
import ConfigurationComponent from "@/components/ConfigurationComp.vue";

export default {
  name: 'ConfigurationView',
  components: {ConfigurationComponent},
  data: () => ({
    configurationCards: [
      { name: 'Bot', icon: 'pi-sync' , section: 'bot', enableReset: true, fields: null },
      { name: 'Search', icon: 'pi-search' , section: 'finder', enableReset: true, fields: null },
      { name: 'APIs', icon: 'pi-globe' , section: 'api', enableReset: true, fields: null },
      { name: 'Admin', icon: 'pi-user' , section: 'admin', enableReset: false,
        fields: [
          {
            "fieldName": "username",
            "jsonFieldName": "username",
            "type": "TEXT",
            "description": "Desired username",
            "value": "",
          },
          {
            "fieldName": "password",
            "jsonFieldName": "password",
            "type": "TEXT",
            "description": "Desired password",
            "value": "",
          },
          {
            "fieldName": "update credentials/new account",
            "jsonFieldName": "password",
            "type": "BOOLEAN",
            "description": "True if you want to create another account and keep the current one or false if you want to update the credentials of the one you are using now",
            "value": "",
          }
        ]
      },
    ],
    cfgCard: null
  }),
  methods: {
    changeCfgCard(card){
      if (card !== this.cfgCard) this.cfgCard = card;
    }
  },
  mounted() {
    this.cfgCard = this.configurationCards[0];
  }
}
</script>