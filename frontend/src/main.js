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

const emitter = mitt()
const app = createApp(App)
const pinia = createPinia()
const axiosInstance = axios.create({
    headers: {"Content-Type": "application/json"},
    baseURL: "/v2/api"
});
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
            if (errorMsg === "TOKEN_ERROR"){
                localStorage.clear();
                router.push("/login");
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

pinia.use(({ store }) => {
    store.$axios = app.config.globalProperties.$axios;
    store.$emitter = app.config.globalProperties.emitter;
});

app.use(PrimeVue,{ripple:true})
app.use(VueAxios, axios)
app.use(pinia)
app.use(router)
app.mount('#app')
