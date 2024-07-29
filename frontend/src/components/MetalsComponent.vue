<template>
  <div class="main">
    <div class="content">
    <div><h1>Metals</h1></div>
    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Price</th>
          <th>Entry Date</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><input v-model="newMaterial.name" type="text" placeholder="Name"></td>
          <td><input v-model.number="newMaterial.price" type="number" placeholder="Price"></td>
          <td><input v-model="newMaterial.entryDate" type="datetime-local"></td>
          <td>
            <button class="actionbutton" @click="addMaterial">Add New</button>
          </td>
        </tr>
        <tr v-for="metal in metals" :key="metal.id">
          <td><input v-model="metal.name" type="text" /></td>
          <td><input v-model.number="metal.price" type="number" /></td>
          <td>{{formatDate(metal.entryDate) }}</td>
          <td>
            <button class="actionbutton" @click="updateMetal(metal)">Save</button>
            <button class="actionbutton" @click="deleteMetal(metal.id)">Delete</button>
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
  name: 'MetalsComponent',
  data() {
    return {
      metals: [],
      newMaterial: { // Datenmodell für ein neues Material
        name: '',
        price: 0,
        entryDate:this.formatDateCustom(new Date())
        },
      errorMessage: ''
    };

  },

  mounted() {
    this.fetchMetals();
  },
  methods: {
   addMaterial() {
      this.newMaterial.entryDate = new Date(this.newMaterial.entryDate);
      axios.post('/materials', this.newMaterial)
        .then(response => {
         this.resetNewMaterial(); // Zurücksetzen des Formulars
          this.metals.push(response.data); // Füge das neue Material zur Liste hinzus
          this.clearErrorMessage();
        })
        .catch(error => {
          console.error("Error adding new material:", error);
          this.setErrorMessage(error,"Error adding metal. Please try again later.");
        });
    },
    resetNewMaterial() {
      this.newMaterial = {
        name: '',
        price: 0,
        entryDate: this.formatDateCustom(new Date())
      };
    },
    async fetchMetals() {
      try {
        const response = await axios.get(`/materials`);
        this.metals = response.data;
        this.clearErrorMessage();
      } catch (error) {
        console.error('Error fetching metals:', error);
         this.setErrorMessage(error,"Error fetching metals. Please try again later.");
      }
    },
   async updateMetal(metal) {
      metal.entryDate=new Date();
      axios.put(`/materials/`+metal.id, metal)
        .then(response => {
          // Handle success
          console.log("Update erfolgreich:", response);
          this.clearErrorMessage();
          this.fetchMetals();

        })
        .catch(error => {
          // Handle error
          console.error("Update fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not update Metal");
        });
    },
    async deleteMetal(metalid) {

      axios.delete(`/materials/`+metalid)
        .then(response => {
          // Handle success
          console.log("Delete erfolgreich:", response);
          this.fetchMetals();
        })
        .catch(error => {
          // Handle error
          console.error("Delete fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not delete Metal");
        });
    },

    setErrorMessage(error, defaultMessage){
          if(error.response.data != null && error.response.data.message != null){
            this.errorMessage =error.response.data.message;
          }
          else{
            this.errorMessage=defaultMessage;
          }
    },
    clearErrorMessage(){
      this.errorMessage='';
     },
    formatDate(date) {
      return new Date(date).toLocaleString(); // Formatiert Datum und Uhrzeit
    },
    formatDateCustom(date){

      var month=date.getMonth()+1;
      var day=date.getDate();
      var hour =date.getHours();
      var minutes =date.getMinutes();
      var seconds =date.getSeconds();
      if(month <10){
        month ="0"+month;
      }
      if(day <10){
        day ="0"+day
      }
      if(hour <10){
        hour="0"+hour;
      }
      if(minutes <10){
        minutes="0"+minutes;
      }
      if(seconds <10){
        seconds="0"+seconds;
      }

      return `${date.getFullYear()}-${month}-${day}T${hour}:${minutes}:${minutes}`;
    }

  }
};
</script>
