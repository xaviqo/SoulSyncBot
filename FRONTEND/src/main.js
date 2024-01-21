import { createApp,h } from 'vue'
import App from './App.vue'
import PrimeVue from 'primevue/config';
import router from './router'
import VueAxios from "vue-axios";
import axios from "axios";
import { createPinia } from 'pinia';
import 'primeflex/primeflex.css';
import 'primevue/resources/themes/nano/theme.css'
import 'primeicons/primeicons.css'
import { Vue3Mq } from "vue3-mq";
import mitt from "mitt";

import Button from "primevue/button";
import Toolbar from "primevue/toolbar";
import Panel from "primevue/panel";
import Chip from "primevue/chip";
import Card from "primevue/card";
import InputText from "primevue/inputtext";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import Tag from "primevue/tag";
import ProgressBar from "primevue/progressbar";
import Slider from "primevue/slider";
import Checkbox from "primevue/checkbox";
import DataView from "primevue/dataview";
import Dropdown from "primevue/dropdown";
import Tooltip from 'primevue/tooltip';
import Message from "primevue/message";
import InlineMessage from "primevue/inlinemessage";
import Chips from "primevue/chips";
import InputSwitch from "primevue/inputswitch";
import InputNumber from "primevue/inputnumber";
import ProgressSpinner from "primevue/progressspinner";
import Dialog from "primevue/dialog";
import ConfirmPopup from "primevue/confirmpopup";
import ConfirmationService from 'primevue/confirmationservice';
import Chart from 'primevue/chart';

const emitter = mitt();
const app  = createApp({render: ()=>h(App)});

axios.defaults.baseURL = process.env.VUE_APP_BACKEND_URL || 'http://localhost:6743/v1';

axios.interceptors.request.use(
    async config => {
        const token = localStorage.getItem("token")
        if (token){
            config.headers = {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json',
            }
        } else {
            config.headers = {
                'Accept': 'application/json',
            }
        }

        return config;
    },
    error => {
        Promise.reject(error)
    });

axios.interceptors.response.use(
    response => response,
    error => {

        if (error.response) {
            emitter.emit('show-alert',{
                info: error.response.data.message,
                icon: 'pi-exclamation-circle',
                severity: 'error'
            });
        }

        return Promise.reject(error);
    }
);



app.config.globalProperties.emitter = emitter
app
    .component('Button',Button)
    .component('Toolbar',Toolbar)
    .component('Panel',Panel)
    .component('Chip',Chip)
    .component('Card',Card)
    .component('InputText',InputText)
    .component('DataTable',DataTable)
    .component('Column',Column)
    .component('Tag',Tag)
    .component('ProgressBar',ProgressBar)
    .component('Slider',Slider)
    .component('Checkbox',Checkbox)
    .component('DataView',DataView)
    .component('Dropdown',Dropdown)
    .component('Message',Message)
    .component('InlineMessage',InlineMessage)
    .component('InputSwitch',InputSwitch)
    .component('InputNumber',InputNumber)
    .component('Chips',Chips)
    .component('ProgressSpinner',ProgressSpinner)
    .component('Dialog',Dialog)
    .component('ConfirmPopup',ConfirmPopup)
    .component('Chart',Chart)

app.directive('tooltip', Tooltip)

app
    .use(createPinia())
    .use(Vue3Mq,{preset: 'vuetify'})
    .use(PrimeVue)
    .use(ConfirmationService)
    .use(router)
    .use(VueAxios, axios)
    .mount('#app')