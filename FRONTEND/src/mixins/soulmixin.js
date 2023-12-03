export const soulmixin = {
    methods: {
        getElapsedTime(timestamp) {
            if (timestamp < 1) return 'Never';
            const now = new Date();
            const elapsedTimeInMilliseconds = now - timestamp;
            const elapsedMinutes = Math.floor(elapsedTimeInMilliseconds / (1000 * 60));
            if (elapsedMinutes < 1) {
                return '< 1 min';
            } else if (elapsedMinutes === 1) {
                return '1 min';
            } else {
                return `${elapsedMinutes} mins`;
            }
        }
    }
}