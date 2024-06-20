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
        },
        refreshRate: {
            current: 0,
            threshold: 10
        }
    }),
    actions: {
        addPlaylist(pl) {
            if (!Array.isArray(this.playlists))
                this.playlists = [];
            this.playlists.push(pl);
        },
        setRefreshThreshold(threshold) {
            if (threshold > 4)
                this.refreshRate.threshold = threshold;
        },
        updateTimer() {
            let { current, thresold } = this.refreshRate;
            if (current > thresold)
                current = 0;
            else
                current++;
            this.refreshRate.current = current;
        },
        async fetchPlaylistSongs(playlistId,page,size) {
            if (playlistId) {
                this.$emitter.emit('loading',{show: true});
                const res = await this.$axios.get(`/playlist/${playlistId}/songs`, { params: { page: page-1, size }});
                this.playlistSongs = res.data;
                this.$emitter.emit('loading',{show: false});
            }
        },
        async fetchAllPlaylists() {
            if (this.playlists.length < 1) {
                this.$emitter.emit('loading',{show: true});
                const res = await this.$axios.get('/playlist');
                this.playlists = res.data;
                this.$emitter.emit('loading',{show: false});
            }
        },
        async fetchCurrentPlaylist(playlistId) {
            const isDistinct = playlistId && this.playlist?.id !== playlistId;
            if (isDistinct) {
                this.$emitter.emit('loading',{show: true});
                const res = await this.$axios.get(`/playlist/${playlistId}`);
                this.currentPlaylist = res.data;
                this.$emitter.emit('loading',{show: false});
            }
        },
        async fetchPlaylistDownloadLists(playlistId) {
            if (playlistId) {
                this.$emitter.emit('loading',{show: true});
                const res = await this.$axios.get(`/playlist/${playlistId}/download-lists`);
                this.playlistDownloadLists = res.data;
                this.$emitter.emit('loading',{show: false});
            }
        },
        setCurrentDownloadList(downloadListId) {
            if (downloadListId && this.playlistDownloadLists?.length > 0) {
                this.currentDownloadList = this.playlistDownloadLists
                    .find( dl => dl.id == downloadListId);
            }

        },
        async fetchDownloadListSongs(downloadListId,page,size) {
            if (downloadListId) {
                this.$emitter.emit('loading',{show: true});
                const res = await this.$axios.get(`/download-list/${downloadListId}/tracks`, { params: { page: page-1, size }});
                this.downloadListSongs = res.data;
                this.$emitter.emit('loading',{show: false});
            }
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
        getLastCurrentRefresh: state => {
            return state.refreshRate.current;
        },
        getRefreshRateThreshold: state => {
          return state.refreshRate.threshold;
        },
        isPlaylistDiscography: state => {
            return state.playlist?.playlistType === 'DISCOGRAPHY';
        }
    }
})
