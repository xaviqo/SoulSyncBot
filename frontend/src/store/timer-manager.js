import {defineStore} from "pinia";

export const useTimerStore = defineStore('timer', {
    state: () => ({
        refreshRate: {
            current: 0,
            threshold: 15,
            min: 5,
            max: 480
        }
    }),
    actions: {
        async loadTimerValues() {
            const res = await this.$axios.get(
                '/cfg/field',
                { params: { name: 'APP_DATA_REFRESH_RATE' }}
            );
            this.refreshRate = {
                current: 0,
                threshold: res.data.value,
                min: res.data.min,
                max: res.data.max
            };
        },
        updateTimer() {
            const { current, threshold } = this.refreshRate;
            if (current >= threshold)
                this.refreshRate.current = 0;
            else
                this.refreshRate.current++;
        },
        setRefreshThreshold(threshold) {
            const { min, max } = this.refreshRate;
            if (threshold >= min && threshold <= max) {
                this.refreshRate.threshold = threshold;
            }
        },
    },
    getters: {
        getCurrent: state => state.refreshRate.current,
        getThreshold: state => state.refreshRate.threshold,
        getMin: state => state.refreshRate.min,
        getMax: state => state.refreshRate.max,
        getProgress: state => {
            const { current, threshold } = state.refreshRate;
            return (current / threshold) * 100;
        },
    }
})