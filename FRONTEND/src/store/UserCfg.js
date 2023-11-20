import {defineStore} from "pinia";
export const useUserCfgStore = defineStore('userCfg', {
    state: () => ({
        refresh: 30,
        isApiAlive: false
    }),
    actions: {
        setRefreshInterval(refresh){
            this.refresh = refresh;
        },
        setApiStatus(status) {
            this.isApiAlive = status;
        },
    }
})