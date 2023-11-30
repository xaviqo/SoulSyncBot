import {defineStore} from "pinia";
export const useUserCfgStore = defineStore('userCfg', {
    state: () => ({
        interval: {
            refresh: 30,
            sec: 0,
            increment: 0,
            progress: 0,
            task: null,
        },
        isApiAlive: false,
        isConnected: false,
        refreshListener: null
    }),
    actions: {
        resetInterval(){
            this.interval.progress=0;
            this.interval.sec=0;
        },
        setApiStatus(status) {
            this.isApiAlive = status;
        },
        setToken(token){
            localStorage.setItem('token',token);
            this.checkTokenStatus();
        },
        deleteToken(){
            localStorage.removeItem('token');
            this.checkTokenStatus();
        },
        checkTokenStatus(){
            this.isConnected = localStorage.getItem('token') != null;
        },
        triggerRefresh(){
            this.refreshListener = Math.random();
        }
    }
})