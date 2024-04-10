export const utilsMixin = {
    methods: {
        capitalizeFirstLetter(text) {
            const words = text.split(" ");
            for (let i = 0; i < words.length; i++) {
                const firstLetter = words[i].charAt(0).toUpperCase();
                words[i] = firstLetter + words[i].slice(1);
            }
            return words.join(" ");
        },
    }
}