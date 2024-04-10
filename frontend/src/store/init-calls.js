import { defineStore } from "pinia";

export const useInitStore = defineStore('init', {
    state: () => ({
        isTokenValidVal: null,
        isInstalledVal: null
    }),
    actions: {
        async checkInstall(){
            if (this.isInstalledVal == null){
                const res = await this.$axios.get('/cfg/is-installed');
                this.isInstalledVal = res?.data?.isInstalled;
            }
        },
        async install(payload){
            return this.$axios
                .post('/cfg/initial-setup',payload)
                .then( res => res);
        }
    },
    getters: {
        isInstalled(state){
            return state.isInstalledVal;
        }
    }
})
