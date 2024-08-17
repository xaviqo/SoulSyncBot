<template>
  <div class="w-full flex justify-content-around gap-4 p-3" v-if="isAdmin()">
    <div class="w-4 flex flex-wrap gap-4 justify-content-center" >
      <div class="font-bold text-xl text-color-secondary">
        Current Users
      </div>
      <DataTable :value="users" class="w-full">
        <Column field="role">
          <template #body="slotProps">
            <div class="bg-gray-800 p-1 flex justify-content-center text-gray-300 border-round">
              {{ slotProps.data.role }}
            </div>
          </template>
        </Column>
        <Column field="username">
          <template #body="slotProps">
            <div
                class="w-full flex justify-content-center text-xl"
                :class="isCurrentUser(slotProps.data.username) ? 'text-primary' : ''"
            >
              {{ slotProps.data.username }}
            </div>
          </template>
        </Column>
        <Column class="flex justify-content-evenly gap-2">
          <template #body="slotProps">
            <Button
                size="small"
                severity="secondary"
                type="button"
                icon="pi pi-cog"
                class="shadow-1"
                @click="editUser(slotProps.data)"
            />
            <Button
                size="small"
                severity="secondary"
                type="button"
                icon="pi pi-times"
                class="shadow-1"
                @click="deleteUser(slotProps.data)"
            />
          </template>
        </Column>
      </DataTable>
      <Button
          @click="createNewUser()"
          v-if="!isCreate"
          severity="secondary"
          label="Create new user"
          icon="pi pi-plus"
          size="small"
      />
    </div>
    <div class="w-4 flex flex-wrap gap-4 justify-content-center">
      <div class="font-bold text-xl text-color-secondary">
        {{
          isCreate ? 'Create new user' : 'Modify Existing User'
        }}
      </div>
      <InputGroup class="flex flex-wrap gap-2">
        <div class="w-full">
          <label for="username" class="font-semibold">Username</label>
        </div>
        <div class="w-full">
          <InputText id="username" v-model="user.username" class="w-full"></InputText>
        </div>
      </InputGroup>
      <InputGroup class="flex flex-wrap gap-2">
        <div class="w-full">
          <label for="password" class="font-semibold">Password</label>
        </div>
        <div class="w-full flex flex-nowrap">
          <InputText id="password"
                     v-model="user.password"
                     class="w-full"
                     :class="!isCreate ? 'input-right-squared' : ''"
                     :disabled="!isEditPwd"
          ></InputText>
          <Button
              v-if="!isCreate"
              class="input-left-squared"
              v-tooltip="{ value: 'Toggle edit user password', showDelay:50, hideDelay:100 }"
              icon="pi pi-cog"
              severity="secondary"
              @click="editPassword(!isEditPwd)"
          />
        </div>
      </InputGroup>
      <InputGroup class="flex flex-wrap gap-2">
        <div class="w-full">
          <label for="role" class="font-semibold" >Role</label>
        </div>
        <div class="w-full">
          <Dropdown
              class="w-full"
              id="role"
              :options="roles"
              v-model="user.role"
          />
        </div>
      </InputGroup>
      <div>
        <Button
            label="Save User"
            icon="pi pi-plus"
            severity="secondary"
            @click="sendUser()"
        />
      </div>
    </div>
  </div>
  <div v-else class="w-full flex justify-content-center p-4">
    <InlineMessage severity="error">Access denied, only admins have access to this section</InlineMessage>
  </div>
</template>
<script>
import {useUserStore} from "@/store/user-calls";
import {mapState} from "pinia";

export default {
  name: "UsersConfiguration",
  data: () => ({
    users: [],
    user: {
      username: null,
      password: null,
      role: 'ADMIN',
    },
    isCreate: true,
    isEditPwd: true,
    roles: [
      'ADMIN',
      'DEMO'
    ]
  }),
  methods: {
    sendUser() {
      const isReady = Object.values(this.user).every( field => field != null && field !== '' );
      if (!isReady) {
        this.emitter.emit('alert', {
          severity: 'warn',
          message: `The fields cannot be empty`
        });
      } else {
        if (!this.isCreate && !this.isEditPwd) {
          this.user.password = null;
        }
        this.$axios
            .post('/account/create', this.user)
            .then( () => {
              this.user.password = null;
              this.users.push({...this.user});
              this.emitter.emit('alert', {
                severity: 'success',
                message: `User successfully saved`
              });
              this.createNewUser();
            });
      }
    },
    editUser(user){
      this.isCreate = false;
      this.isEditPwd = false;
      this.user = user;
      this.user.password = '********'
    },
    deleteUser(user){
      if (user.role === 'ADMIN') {
        const totalAdmins = this.users
            .filter(user => user.role === 'ADMIN')
            .length;
        if (totalAdmins < 2) {
          this.emitter.emit('alert', {
            severity: 'warn',
            message: `User ${user.username} cannot be deleted.
            There must be at least one user with admin permissions`
          });
          return;
        }
      }
      this.$axios
          .delete(`/account/delete/${user.username}`)
          .then( () => {
            this.emitter.emit('alert', {
              severity: 'success',
              message: `User ${user.username} successfully deleted`
            });
            this.users = [];
            this.fetchUsers();
          });
    },
    editPassword(isEditPwd){
      this.isEditPwd = isEditPwd;
      this.user.password = isEditPwd ? null : '********';
    },
    createNewUser(){
      this.isCreate = true;
      this.isEditPwd = true;
      this.user = {
        username: null,
        password: null,
        role: 'ADMIN',
      };
    },
    fetchUsers() {
      this.$axios
          .get("/account/users")
          .then( res => this.users = res.data );
    },
    isAdmin(){
      return this.getRole === 'ADMIN';
    },
    isCurrentUser(current) {
      return current === localStorage.getItem('username');
    }
  },
  computed: {
    ...mapState(useUserStore, {
      getRole: 'getRole'
    })
  },
  created() {
    if (this.isAdmin()) this.fetchUsers();
  }
}
</script>