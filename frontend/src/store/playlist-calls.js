import { defineStore } from "pinia";

export const usePlaylistStore = defineStore('playlist', {
    state: () => ({
        playlists: [],
        currentPlaylist: { id: 0 },
        playlistSongs: {
            size: 0,
            totalPages: 0,
            totalElements: 0,
            content: []
        },
        currentDownloadList: { id: 0 },
        playlistDownloadLists: [],
        downloadListSongs: {
            size: 0,
            totalPages: 0,
            totalElements: 0,
            content: []
        }
    }),
    actions: {
        addPlaylist(pl) {
            if (!Array.isArray(this.playlists))
                this.playlists = [];
            this.playlists.push(pl);
        },
        async fetchPlaylistSongs(playlistId,page,size) {
            if (playlistId) {
                const res = await this.$axios.get(`/playlist/${playlistId}/songs`, { params: { page: page , size }});
                this.playlistSongs = res.data;
            }
        },
        async fetchAllPlaylists() {
            if (this.playlists.length < 1) {
                const res = await this.$axios.get('/playlist');
                this.playlists = res.data;
            }
        },
        async fetchCurrentPlaylist(playlistId) {
            const isDistinct = playlistId && this.playlist?.id !== playlistId;
            if (isDistinct) {
                const res = await this.$axios.get(`/playlist/${playlistId}`);
                this.currentPlaylist = res.data;
            }
        },
        async fetchPlaylistDownloadLists(playlistId) {
            if (playlistId) {
                const res = await this.$axios.get(`/playlist/${playlistId}/download-lists`);
                this.playlistDownloadLists = res.data;
            }
        },
        setCurrentDownloadList(downloadListId) {
            if (downloadListId && this.playlistDownloadLists?.length > 0) {
                this.currentDownloadList = this.playlistDownloadLists
                    .find( dl => dl.id == downloadListId);
            }

        },
        async fetchDownloadListSongs(downloadListId,params) {
            if (downloadListId) {
                const res = await this.$axios.get(`/download-list/${downloadListId}/tracks`, { params: params });
                this.downloadListSongs = res.data;
            }
        },
        removePlaylists() {
            this.playlists = [];
        }
    },
    getters: {
        getPlaylists: state => {
            return state.playlists;
        },
        getPlaylistSongs: state => {
            return state.playlistSongs;
        },
        getCurrentPlaylist: state => {
          return state.currentPlaylist;
        },
        getPlaylistDownloadLists: state => {
          return state.playlistDownloadLists;
        },
        getCurrentDownloadList: state => {
          return state.currentDownloadList
              ? [state.currentDownloadList]
              : null;
        },
        getDownloadListSongs: state => {
          return state.downloadListSongs;
        },
        isPlaylistDiscography: state => {
            return state.playlist?.playlistType === 'DISCOGRAPHY';
        }
    }
})
