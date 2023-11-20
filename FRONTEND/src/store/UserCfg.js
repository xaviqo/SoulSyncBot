import {defineStore} from "pinia";
export const useUserCfgStore = defineStore('userCfg', {
    state: () => ({
        refresh: 30,
        isApiAlive: false,
        isConnected: false
    }),
    actions: {
        setRefreshInterval(refresh){
            this.refresh = refresh;
        },
        setApiStatus(status) {
            this.isApiAlive = status;
        },
        setToken(token){
            localStorage.setItem('token',token);
            this.checkStatus();
        },
        deleteToken(){
            localStorage.removeItem('token');
            this.checkStatus();
        },
        checkStatus(){
            this.isConnected = localStorage.getItem('token') != null;
        }
    }
})