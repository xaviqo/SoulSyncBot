<template>
  <div class="grid">
    <InputGroup
        v-for="field in fields"
        class="col-6 flex flex-wrap"
        :key="field.name"
    >
      <div class="w-full mb-1 text-gray-200">
        <label :for="field.name">
          {{ field.fieldName }}
        </label>
      </div>
      <div class="w-full flex flex-nowrap">
        <InputText
            v-if="field.dataType === FieldType.TEXT"
            :id="field.name"
            v-model="field.value"
            class="input-right-squared"
        />
        <InputNumber
            v-else-if="field.dataType === FieldType.NUMBER"
            :id="field.name"
            v-model="field.value"
            class="input-right-squared"
        />
        <Dropdown
            v-else-if="field.dataType === FieldType.SELECT"
            :id="field.name"
            :options="field.defaultValues"
            v-model="field.value"
            class="input-right-squared"
        />
        <ToggleButton
            class="w-full border-1 border-100 input-left-rounded"
            v-else-if="field.dataType === FieldType.BOOLEAN"
            :id="field.name"
            v-model="field.value"
        />
        <Button
            class="input-left-squared"
            v-tooltip="{ value: field.description, showDelay:50, hideDelay:100 }"
            icon="pi pi-question"
            severity="secondary"
        />
      </div>
    </InputGroup>
  </div>
</template>
<script>
import FieldType from "@/model/FieldType";

export default {
  name: "ConfigurationFields",
  computed: {
    FieldType() {
      return FieldType
    }
  },
  props: {
    fields: {
      type: Array
    }
  }
}
</script>
<style scoped>

</style>