<template>
  <div class="main">
    <div class="content">
    <div><h1>Units</h1></div>
    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
    <table >
      <thead>
        <tr>
          <th>Name</th>
          <th>Factor</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><input v-model="newUnit.name" type="text" placeholder="Name"></td>
          <td><input v-model.number="newUnit.factor" type="number" placeholder="Factor"></td>

          <td>
            <button class="actionbutton" @click="addUnit()">Add New</button>
          </td>
        </tr>
        <tr v-for="unit in units" :key="unit.name">
          <td><input v-model="unit.name" type="text" disabled="true"/></td>
          <td><input v-model.number="unit.factor" type="number" /></td>

          <td>
            <button class="actionbutton" @click="updateUnit(unit)">Save</button>
            <button class="actionbutton" @click="deleteUnit(unit.name)">Delete</button>
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
  name: 'UnitsComponent',
  data() {
    return {
      units: [],
      newUnit: {
        name: '',
        factor: 1
        },
      errorMessage: ''
    };

  },

  mounted() {
    this.fetchUnits();
  },
  methods: {
   addUnit(){
      axios.post('/units', this.newUnit)
        .then(response => {
          this.units.push(response.data);
          this.resetNewUnit();
        })
        .catch(error => {
          console.error("Error adding new unit:", error);
          this.setErrorMessage(error,"Error adding unit. Please try again later.");
        });
    },
    resetNewUnit() {
      this.newUnit = {
        name: '',
        factor: 1

      };
    },
    async fetchUnits() {
      try {
        const response = await axios.get(`/units`);
        this.units = response.data;
      } catch (error) {
        console.error('Error fetching units:', error);
         this.setErrorMessage(error,"Error fetching units. Please try again later.");
      }
    },
   async updateUnit(unit) {

      axios.put(`/units/`+unit.name, unit)
        .then(response => {
          // Handle success
          console.log("Update erfolgreich:", response);
          this.fetchUnits();
        })
        .catch(error => {
          // Handle error
          console.error("Update fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not update Unit");
        });
    },
    async deleteUnit(unitname) {

      axios.delete(`/units/${unitname}`)
        .then(response => {
          // Handle success
          console.log("Delete erfolgreich:", response);
          this.fetchUnits();
        })
        .catch(error => {
          // Handle error
          console.error("Delete fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not delete Unit");
        });
    },

    setErrorMessage(error, defaultMessage){
          if(error.response.data != null && error.response.data.message != null){
            this.errorMessage =error.response.data.message;
          }
          else{
            this.errorMessage=defaultMessage;
          }
    }

  }
};
</script>
