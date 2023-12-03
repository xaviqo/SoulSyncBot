<template>
  <div class="w-full border-1 bg-white p-2 border-black-alpha-90 shadow-2 mt-2">
    <ConfirmPopup></ConfirmPopup>
    <div class="flex justify-content-between">
      <div class="flex">
        <div class="w-2rem h-2rem surface-50 border-1 border-black-alpha-90 flex align-items-center justify-content-center mr-3">
          <i :class="'pi ' + section.icon" style="font-size: 1.2rem"></i>
        </div>
        <div class="flex align-items-center text-xl font-bold">
          {{ section.name }}
        </div>
      </div>
      <div v-if="isApiAlive" class="flex align-items-center">
        <Button
            v-if="section.enableReset"
            label="Reset to default"
            icon="pi pi-history"
            severity="secondary"
            @click="reset()"
            class="mr-2"
        />
        <Button
            label="Save changes"
            icon="pi pi-save"
            @click="save()"
        />
      </div>
    </div>
  </div>
  <Card class="w-full border-1 border-black-alpha-90 shadow-2 mt-1">
    <template #content>
      <div v-if="isApiAlive" class="w-full h-full grid">
        <template v-for="field in fields" :key="field.field">
          <div class="col-6">
            <div class="border-left-1 border-gray-200 flex justify-content-start align-items-center p-1 w-full h-full capitalize">
              <i class="pi pi-chevron-right mr-1" style="font-size: .6rem; color: rgb(171,171,171)"></i>
              <span class="font-bold text-900 white-space-nowrap overflow-hidden text-overflow-ellipsis">{{field.fieldName}}:</span>
            </div>
          </div>
          <div class="col-1 flex justify-content-end align-items-center">
            <Button
                icon="pi pi-info"
                severity="secondary"
                rounded
                outlined
                size="small"
                v-tooltip.left="field.description"
                style="width: 1.2rem; height: 1.2rem; cursor: default"
            />
          </div>
          <div class="col-5">
            <InputText
                v-if="field.type === FieldType.TEXT"
                :placeholder="capitalizeWords(field.fieldName)"
                class="w-full"
                v-model="field.value"
            />
            <InputNumber
                v-if="field.type === FieldType.NUMBER"
                v-model="field.value"
                :placeholder="capitalizeWords(field.fieldName)"
                class="w-full"
            />
            <Dropdown
                v-if="field.type === FieldType.SELECT"
                v-model="field.value"
                :options="field.values"
                :placeholder="capitalizeWords(field.fieldName)"
                class="w-full"
            />
            <InputSwitch
                v-if="field.type === FieldType.BOOLEAN"
                v-model="field.value"
                style="margin-top: 3px"
            />
            <div
                v-if="field.type === FieldType.RANGE"
                class="grid"
                style="margin-left: 1px"
            >
              <div
                  class="mt-1 col-4 flex justify-content-start align-items-center bg-gray-50 border-1 border-black-alpha-20"
              >
                <strong>
                  {{ field.value }}
                </strong>
              </div>
              <div class="col-8 flex justify-content-start align-items-center">
                <Slider
                    :min="field.min"
                    :max="field.max"
                    v-model="field.value"
                    class="w-full"

                />
              </div>
            </div>
            <span
                v-if="field.type === FieldType.ARRAY"
                class="p-float-label"
            >
            <Chips
                id="chips"
                v-model="field.value"
            />
            </span>
          </div>
        </template>
      </div>
      <div v-else class="w-full text-xl text-gray-500 mt-3" style="text-align: center">
        No API connection has been established. Unable to load configuration
      </div>
    </template>
  </Card>
</template>
<script>
import FieldType from "@/models/FieldType";
import {mapState} from "pinia";
import {useUserCfgStore} from "@/store/UserCfg";

export default {
  name: 'ConfigurationComponent',
  inject: ["mq"],
  computed: {
    FieldType() {
      return FieldType
    },
    ...mapState(useUserCfgStore, ['isApiAlive'])
  },
  data: () => ({
    fields: []
  }),
  props: {
    section: Object,
  },
  watch: {
    section(newSection) {
      if (newSection.fields != null) {
        this.fields = newSection.fields;
      } else {
        this.fetchOptions(newSection);
      }
    },
  },
  methods: {
    save(){
      this.axios.post(
          '/configuration/save',
          this.fields,
          { params: { section: this.section.section }}
      )
      .then( res => {
        if (this.section.fields == null) {
          this.fields = res.data;
        } else {
          this.fields.forEach( field => field.value = "");
        }
        this.emitter.emit('show-alert',{
          info: 'Changes successfully implemented',
          icon: 'pi-check-circle',
          severity: 'success'
        });
      })
      .catch( err => {
        this.emitter.emit('show-alert',{
          info: err.response.data.message,
          icon: 'pi-exclamation-circle',
          severity: 'error'
        });
        if (err.response.data.code === 704){
          this.fetchOptions(this.section);
        }
      })
    },
    reset(){
      if (this.section.enableReset) {
        this.axios.post(`/configuration/reset?section=${this.section.section}`)
            .then( res => {
              this.fields = res.data;
              this.emitter.emit('show-alert',{
                info: 'Section values reset successfully',
                icon: 'pi-check-circle',
                severity: 'success'
              });
            }).catch(err => {
              console.log(err)
              this.emitter.emit('show-alert',{
                info: err.response.data.message,
                icon: 'pi-exclamation-circle',
                severity: 'error'
              });
        })
      }
    },
    fetchOptions(section){
      this.axios.get(
          '/configuration/get',
          { params: { section: section.section } }
      )
      .then( res => {
        this.fields = res.data;
      })
      .catch( err => {
        console.error(err)
      })
    },
    capitalizeWords(str) {
      let words = str.split(" ");
      for (let i = 0; i < words.length; i++) {
        let word = words[i];
        if (word.length > 0) {
          words[i] = word[0].toUpperCase() + word.slice(1);
        }
      }
      return words.join(" ");
    },
  },
  mounted() {
    this.fetchOptions(this.section);
  },
}
</script>