<template>
  <div class="main">
    <div class="content">
    <div><h1>Users</h1></div>
    <div v-if="statusMessage">{{ statusMessage }}</div>
    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
    <table >
      <thead>
        <tr>
          <th>Name</th>
          <th>Password</th>
          <th>Password (Confirm)</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><input v-model="newUser.username" type="text" placeholder="Name"></td>
          <td><input v-model="newUserPasword" type="password" placeholder="Password"></td>
          <td><input v-model="newUserPaswordConfirm" type="password" placeholder="Password (Confirm)"></td>

          <td>
            <button class="actionbutton" @click="addUser()">Add New</button>
          </td>

        </tr>
        <tr v-for="userInfo in userInfos" :key="userInfo.username">
          <td><input v-model="userInfo.username" type="text" disabled=true/></td>
          <td><input v-model="userInfo.password" type="password" placeholder="Password"/></td>
          <td><input v-model="userInfo.passwordConfirm" type="password" placeholder="Password (Confirm)"/></td>
          <td>
            <button class="actionbutton" @click="updatePassword(userInfo)">Update Password</button>
            <button :class="getButtonClass(isUserCurrentUser(userInfo.username))" @click="deleteUser(userInfo.username)" :disabled="isUserCurrentUser(userInfo.username)">Delete</button>
            <button v-if="userInfo.active" :class="getButtonClass(isUserCurrentUser(userInfo.username))" @click="setUserStatus(false, userInfo)" :disabled="isUserCurrentUser(userInfo.username)">Disable</button>
            <button v-else :class="getButtonClass(isUserCurrentUser(userInfo.username))" @click="setUserStatus(true, userInfo)" :disabled="isUserCurrentUser(userInfo.username)">Enable</button>
          </td>
        </tr>
      </tbody>
    </table>

    </div>
  </div>
</template>

<script>
import axios from '../axios';

export default {
  name: 'UsersComponent',
  data() {
    return {
      userInfos: [],


      newUser: {
        username: '',
        password: ''
        },
      newUserPasword:'',
      newUserPaswordConfirm:'',
      errorMessage: '',
      statusMessage: ''
    };

  },

  mounted() {
    this.fetchData();
  },
  methods: {

  isUserCurrentUser(username){
   return sessionStorage.getItem("username") === username;
  },
  getButtonClass(isCurrentUser){
    if(isCurrentUser){
      return "actionbutton_disabled";
    }
    else{
    return "actionbutton";
    }
  },

   addUser(){
    this.statusMessage='';
    this.errorMessage='';

    if(this.newUserPasword === this.newUserPaswordConfirm){
      this.newUser.password=this.newUserPasword;

        console.log("add new user:", this.newUser);
        axios.post('/userService', this.newUser) .then(response => {
          // Handle success
          console.log("Update erfolgreich:", response);
        this.fetchData();
        this.resetNewUser();
            })
        .catch(error => {
          // Handle error
          console.error("Add User fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not add new User");
        });

    }
    else{
        this.errorMessage="Passwords do not match!";
    }
  },
    resetNewUser() {
      this.newUser= {
        username: '',
        password: ''
        };
       this.newUserPasword='';
       this.newUserPaswordConfirm='';


    },
    async fetchData() {
      try {
      this.statusMessage='';
      this.errorMessage='';
        const response = await axios.get('/userService');
        console.log("Response", response.data)
         this.userInfos = response.data.userInfos.map(user => ({
          username: user.userName,
          active: user.active,
          password: '',
          passwordConfirm: ''
        }));
      } catch (error) {
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,"Error fetching data. Please try again later.");
      }
    },

    async updatePassword(userInfo) {
      this.statusMessage='';
      if(userInfo.password === userInfo.passwordConfirm){

        axios.put('/userService/updatePassword/'+userInfo.username, {newPassword: userInfo.password})
        .then(response => {
          // Handle success
          console.log("Update erfolgreich:", response);
          userInfo.password='';
          userInfo.passwordConfirm='';
          this.statusMessage='Password successfully updated!';
        })
        .catch(error => {
          // Handle error
          console.error("Update fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not update UserStatus");
        });
      }
      else{
        this.errorMessage="Passwords do not match!";
      }

    },
   async setUserStatus(newStatus, userInfo) {
      this.statusMessage='';
      axios.put('/userService/setStatus/'+userInfo.username, {active: newStatus})
        .then(response => {
          // Handle success
          console.log("Update erfolgreich:", response);
          //userInfo.active=newStatus;
         this.fetchData();

        })
        .catch(error => {
          // Handle error
          console.error("Update fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not update UserStatus");
        });
    },
    async deleteUser(userID) {
      this.statusMessage='';
      axios.delete("/userService/deleteuser/"+userID)
        .then(response => {
          // Handle success
          console.log("Delete erfolgreich:", response);
          this.fetchData();
        })
        .catch(error => {
          // Handle error
          console.error("Delete fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not delete ItemType");
        });
    },

    setErrorMessage(error, defaultMessage){
          this.statusMessage='';
          if( error.response != null &&  error.response.data != null  && error.response.data.message != null){
            this.errorMessage =error.response.data.message;
          }
          else{
            this.errorMessage=defaultMessage;
          }
    }

  }
};
</script>
