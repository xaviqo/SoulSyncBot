export const utilsMixin = {
    methods: {
        getArtistsByComa(artists){
            return artists?.map(a => a.name).join(", ");
        },
        capitalizeFirstLetter(text) {
            const words = text.split(" ");
            for (let i = 0; i < words.length; i++) {
                const firstLetter = words[i].charAt(0).toUpperCase();
                words[i] = firstLetter + words[i].slice(1);
            }
            return words.join(" ");
        },
        timestampToDate(ts) {
            if (ts > 0) {
                const date = new Date(ts);
                const d = date.getDate();
                const mon = date.getMonth() + 1;
                const y = date.getFullYear();
                const h = date.getHours();
                const min = date.getMinutes();
                return `${d}/${mon}/${y} ${h}:${min}`;
            }
        },
        bitsToSize(bits) {
            if (bits === 0) return '0 bits';
            const k = 1024;
            const sizes = ['bits', 'KB', 'MB', 'GB', 'TB'];
            const i = Math.floor(Math.log(bits) / Math.log(k));
            const size = (bits / Math.pow(k, i)).toFixed(2);
            return `${size} ${sizes[i]}`;
        }
    }
}