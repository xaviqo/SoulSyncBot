import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import VueAxios from "vue-axios";
import axios from "axios";
import {createPinia} from 'pinia';
import 'primeflex/primeflex.css';
import 'primevue/resources/themes/aura-dark-green/theme.css'
import 'primeicons/primeicons.css'
import PrimeVue from "primevue/config";
import mitt from "mitt";
import Button from "primevue/button";
import Card from "primevue/card";
import InputText from "primevue/inputtext";
import Toolbar from "primevue/toolbar";
import Message from "primevue/message";
import Dialog from "primevue/dialog";
import Steps from "primevue/steps";
import ProgressSpinner from "primevue/progressspinner";
import Dropdown from "primevue/dropdown";
import InputGroup from "primevue/inputgroup";
import FloatLabel from "primevue/floatlabel";
import Chips from "primevue/chips";
import InputNumber from "primevue/inputnumber";
import Tooltip from "primevue/tooltip";
import ToggleButton from "primevue/togglebutton";
import ConfirmDialog from 'primevue/confirmdialog';
import ConfirmationService from "primevue/confirmationservice";
import Badge from "primevue/badge";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import DataView from "primevue/dataview";
import Tag from "primevue/tag";
import ProgressBar from "primevue/progressbar";
import Divider from "primevue/divider";
import TabView from "primevue/tabview";
import TabPanel from "primevue/tabpanel";

const emitter = mitt()
const app = createApp(App)
const pinia = createPinia()
const axiosInstance = axios.create({
    headers: {"Content-Type": "application/json"},
    baseURL: "/v2/api"
});
const LOGOUT_WHEN_TKN_ERROR = true;

axiosInstance.interceptors.request.use(
    async config => {
        const token = localStorage
            .getItem("token")
        if (token) {
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

let lastAlert = null;
axiosInstance.interceptors.response.use(
    response => {
        const alertData = response.data?.alertData;
        if (alertData?.message !== lastAlert?.message) {
            lastAlert = alertData;
            emitAlert(alertData)
        }
        return response;
    },
    error => {
        const errorMsg = error.response.data?.error;
        const alertData = error.response.data?.alertData;
        if (alertData?.message !== lastAlert?.message) {
            lastAlert = alertData;
            emitAlert(alertData);
            if (LOGOUT_WHEN_TKN_ERROR && errorMsg === "TOKEN_ERROR"){
                console.error(error);
                localStorage.clear();
                setTimeout(() => window.location.reload(), 1200);
            }
        }
        return error;
    }
);

const emitAlert = (alertData) => {
    if (alertData) {
        emitter.emit('alert',{
            severity: alertData.severity.toLowerCase(),
            message: alertData.message
        })
    }
}

app.config.globalProperties.$axios = axiosInstance;
app.config.globalProperties.emitter = emitter

app
    .component('Button',Button)
    .component('Card',Card)
    .component('InputText',InputText)
    .component('Toolbar',Toolbar)
    .component('Message',Message)
    .component('Dialog',Dialog)
    .component('Steps',Steps)
    .component('ProgressSpinner',ProgressSpinner)
    .component('Dropdown',Dropdown)
    .component('InputGroup',InputGroup)
    .component('FloatLabel',FloatLabel)
    .component('Chips',Chips)
    .component('Badge',Badge)
    .component('InputNumber',InputNumber)
    .component('ToggleButton',ToggleButton)
    .component('ConfirmDialog',ConfirmDialog)
    .component('DataTable',DataTable)
    .component('Column',Column)
    .component('DataView',DataView)
    .component('Tag',Tag)
    .component('ProgressBar',ProgressBar)
    .component('Divider',Divider)
    .component('TabView',TabView)
    .component('TabPanel',TabPanel)

app.directive('tooltip',Tooltip)

pinia.use(({ store }) => {
    store.$axios = app.config.globalProperties.$axios;
    store.$emitter = app.config.globalProperties.emitter;
});

app.use(PrimeVue,{ripple:true})
app.use(ConfirmationService)
app.use(VueAxios, axios)
app.use(pinia)
app.use(router)
app.mount('#app')
