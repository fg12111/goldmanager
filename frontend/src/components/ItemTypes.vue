<template>
  <div class="main">
    <div class="content">
    <div><h1>ItemTypes</h1></div>
    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
    <table >
      <thead>
        <tr>
          <th>Name</th>
          <th>Modfier</th>
          <th>Metal</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><input v-model="newItemType.name" type="text" placeholder="Name"></td>
          <td><input v-model.number="newItemType.modifier" type="number" placeholder="Factor"></td>
          <td>
            <select id="options" v-model="newItemType.material.id">
              <option v-for="metal in metals" :key="metal.value" :value="metal.value">{{ metal.text }}</option>
            </select>
          </td>
          <td>
            <button class="actionbutton" @click="addItemType()">Add New</button>
          </td>
        </tr>
        <tr v-for="itemType in itemTypes" :key="itemType.id">
          <td><input v-model="itemType.name" type="text"/></td>
          <td><input v-model.number="itemType.modifier" type="number" /></td>
          <td>
            <select id="options" v-model="itemType.material.id">
              <option v-for="metal in metals" :key="metal.value" :value="metal.value">{{ metal.text }}</option>
            </select>
          </td>
          <td>
            <button class="actionbutton" @click="updateItemType(itemType)">Save</button>
            <button class="actionbutton" @click="deleteItemType(itemType.id)">Delete</button>
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
  name: 'ItemTypes',
  data() {
    return {
      itemTypes: [],
      metals: [],
      newItemType: {
        name: '',
        modifier: 1,
        material: {
          id: ''
          },
        },
      preSelectedMetal:'',
      errorMessage: ''
    };

  },

  mounted() {
    this.fetchData();
  },
  methods: {
   addItemType(){
      axios.post('/itemTypes', this.newItemType)
        .then(response => {
          this.itemTypes.push(response.data);
          this.resetNewItemType();
        })
        .catch(error => {
          console.error("Error adding new unit:", error);
          this.setErrorMessage(error,"Error adding unit. Please try again later.");
        });
    },
    resetNewItemType() {
      this.newItemType= {
        name: '',
        modifier: 1,
        material: {
            id: this.preSelectedMetal
        }
        };
    },
    async fetchData() {
      try {
        const response = await axios.get(`/materials`);
         this.metals = response.data.map(option => ({
          value: option.id,
          text: option.name
        }));
        if (this.metals.length > 0) {
          this.preSelectedMetal = this.metals[0].value;
        }
        this.newItemType.material.id=this.preSelectedMetal;

       const itemtyperesponse = await axios.get(`/itemTypes`);
        this.itemTypes = itemtyperesponse.data;
      } catch (error) {
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,"Error fetching data. Please try again later.");
      }
    },
   async updateItemType(itemType) {

      axios.put(`/itemTypes/`+itemType.id, itemType)
        .then(response => {
          // Handle success
          console.log("Update erfolgreich:", response);
          this.fetchData();
        })
        .catch(error => {
          // Handle error
          console.error("Update fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not update ItemType");
        });
    },
    async deleteItemType(itemTypeId) {

      axios.delete(`/itemTypes/${itemTypeId}`)
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
