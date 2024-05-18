import { defineStore } from "pinia";

export const usePlaylistStore = defineStore('playlist', {
    state: () => ({
        playlists: [],
    }),
    actions: {
        async loadPlaylists() {
            const res = await this.$axios.get('/playlist');
            this.playlists = res.data;
        },
        addPlaylist(pl) {
            if (!Array.isArray(this.playlists))
                this.playlists = [];
            this.playlists.push(pl);
        }
    },
    getters: {
        getPlaylists: (state) => {
            return state.playlists;
        }
    }
})
