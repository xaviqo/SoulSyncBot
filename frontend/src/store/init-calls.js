import { defineStore } from "pinia";

export const useInitStore = defineStore('init', {
    state: () => ({
        isTokenValidVal: null,
        isInstalledVal: null
    }),
    actions: {
        async checkSetup(){
            if (this.isInstalledVal == null){
                const res = await this.$axios.get('/cfg/is-installed');
                const isInst = res?.data?.isInstalled;
                this.isInstalledVal = isInst;
                return isInst;
            }
            return false;
        }
    },
    getters: {
        isInstalled(state){
            return state.isInstalledVal;
        }
    }
})
