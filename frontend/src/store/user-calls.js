import { defineStore } from "pinia";
import router from "@/router";
export const useUserStore = defineStore('login', {
    state: () => ({
        isUserAuthenticated: false
    }),
    actions: {
        saveLoginResponse(response){
            localStorage.setItem('username',response.username);
            localStorage.setItem('token',response.token.token);
            localStorage.setItem('token-expiration',response.token.expirationDate);
            localStorage.setItem('role',response.role);
            this.checkAuthenticated();
        },
        checkAuthenticated(){
            const expiration = localStorage.getItem('token-expiration')
            if (expiration) {
                const expDate = new Date(parseInt(expiration));
                this.isUserAuthenticated = expDate > new Date();
            } else {
                localStorage.clear();
                this.isUserAuthenticated = false;
            }
        },
        logOut(){
            localStorage.clear();
            router.push("/login");
        }
    },
    getters: {
        isAuthenticated(state) {
            return state.isUserAuthenticated;
        }
    }
})