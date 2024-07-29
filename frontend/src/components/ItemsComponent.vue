<template>
  <div class="main">
    <div class="content">
    <div><h1>Items</h1></div>
    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
    <table >
      <thead>
        <tr>
          <th>Name</th>
          <th>Type</th>
          <th>Weight</th>
          <th>Unit</th>
          <th>Number of Items</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><input v-model="newItem.name" type="text" placeholder="Name"></td>

          <td>
            <select id="options" v-model="newItem.itemType.id">
              <option v-for="itemType in itemTypes" :key="itemType.value" :value="itemType.value">{{ itemType.text }}</option>
            </select>
          </td>
           <td><input v-model.number="newItem.amount" type="number" placeholder="Weight"></td>
          <td>
            <select id="options" v-model="newItem.unit.name">
              <option v-for="unit in units" :key="unit.value" :value="unit.value">{{ unit.text }}</option>
            </select>
          </td>
           <td><input v-model.number="newItem.itemCount" type="integer" placeholder="Number of items"></td>
          <td>
            <button class="actionbutton" @click="addItem()">Add New</button>
          </td>

        </tr>
        <tr v-for="item in items" :key="item.id">
          <td><input v-model="item.name" type="text"/></td>
          <td>
            <select id="options" v-model="item.itemType.id">
              <option v-for="itemType in itemTypes" :key="itemType.value" :value="itemType.value">{{ itemType.text }}</option>
            </select>
          </td>
           <td><input v-model.number="item.amount" type="number" placeholder="Weight"></td>
          <td>
            <select id="options" v-model="item.unit.name">
              <option v-for="unit in units" :key="unit.value" :value="unit.value">{{ unit.text }}</option>
            </select>
          </td>
          <td><input v-model.number="item.itemCount" type="integer" placeholder="Number of Items"></td>
          <td>
            <button class="actionbutton" @click="updateItem(item)">Save</button>
            <button class="actionbutton" @click="deleteItem(item.id)">Delete</button>
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
  name: 'ItemsComponent',
  data() {
    return {
      items: [],
      units: [],
      itemTypes: [],
      newItem: {
        name: '',
        amount: 0,
        itemType: {
          id: ''
          },
        unit: {
          name: ''
          },
        },
      preSelectedUnit:'',
      preSelectedItemtype:'',
      errorMessage: ''
    };

  },

  mounted() {
    this.fetchData();
  },
  methods: {
   addItem(){
      axios.post('/items', this.newItem)
        .then(response => {
          this.items.push(response.data);
          this.resetNewItem();
        })
        .catch(error => {
          console.error("Error adding new unit:", error);
          this.setErrorMessage(error,"Error adding unit. Please try again later.");
        });
    },
    resetNewItem() {
      this.newItem= {

        name: '',
        amount: 0,
        itemType: {
          id: this.preSelectedItemtype
          },
        unit: {
          name: this.preSelectedUnit
          },

        };
    },
    async fetchData() {
      try {
        const response = await axios.get(`/units`);
         this.units = response.data.map(option => ({
          value: option.name,
          text: option.name
        }));
        if (this.units.length > 0) {
          this.preSelectedUnit = this.units[0].value;
        }
        this.newItem.unit.name=this.preSelectedUnit;

       const itemtyperesponse = await axios.get(`/itemTypes`);
        this.itemTypes = itemtyperesponse.data.map(option => ({
          value: option.id,
          text: option.name
        }));
         if (this.itemTypes.length > 0) {
          this.preSelectedItemtype = this.itemTypes[0].value;
        }
        this.newItem.itemType.id=this.preSelectedItemtype;

        const itemresponse = await axios.get(`/items`);

        this.items = itemresponse.data;
      } catch (error) {
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,"Error fetching data. Please try again later.");
      }
    },
   async updateItem(item) {

      axios.put(`/items/`+item.id, item)
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
    async deleteItem(itemId) {

      axios.delete(`/items/${itemId}`)
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
