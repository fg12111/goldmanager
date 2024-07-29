<template>
  <div class="main">
    <div class="content">
    <div><h1>Prices</h1></div>
    <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
    <div v-if="currentViewType==='PriceList'">
    <table>
      <thead>
        <tr>
          <th>Total Price</th>
          <th>Filter by Metal</th>
          <th>Switch View Type</th>
        </tr>
      </thead>
      <tbody>
      <tr>
        <td>{{ formatPrice(priceList.totalPrice) }}</td>
        <td>
        <select id="metals" v-model="currentFilter" @change="setCurrentFilter($event)" >
          <option value="">None</option>
          <option v-for="metal in metals" :key="metal.id" :value="metal.id">{{ metal.name }}</option>
        </select>
        </td>
        <td>
        <select id="viewtype" v-model="currentViewType" @change="setCurrentViewType($event)" >
          <option v-for="viewType in viewTypes" :key="viewType.id" :value="viewType.id">{{ viewType.label }}</option>
        </select>
        </td>
      </tr>
      </tbody>
      </table>
      <table v-if="priceList.prices.length >0">
        <thead>
          <tr>
            <th>Name</th>
            <th>Weight</th>
            <th>Unit</th>
            <th>Number of Items</th>
            <th>Unit Price</th>
            <th>Total Price</th>
            <th>Metal</th>
          </tr>
        </thead>
        <tbody>

          <tr v-for="price in priceList.prices" :key="price.item.id">
            <td>{{ price.item.name }}</td>
            <td>{{ price.item.amount }}</td>
            <td>{{ price.item.unit.name }}</td>
            <td>{{ price.item.itemCount }}</td>
            <td>{{ formatPrice(price.price) }}</td>
            <td>{{ formatPrice(price.priceTotal) }}</td>
            <td>{{ price.item.itemType.material.name }}</td>
          </tr>
        </tbody>
      </table>
    </div>
   <div v-else >
    <table>
      <thead>
        <tr>

          <th>Switch View Type</th>
        </tr>
      </thead>
      <tbody>
      <tr>
        <td>
        <select id="viewtype" v-model="currentViewType" @change="setCurrentViewType($event)" >
          <option v-for="viewType in viewTypes" :key="viewType.id" :value="viewType.id">{{ viewType.label }}</option>
        </select>
        </td>
      </tr>
      </tbody>
      </table>
      <div v-for="(group, groupName) in priceGroups" :key="groupName">
      <H2>{{ groupName }}</H2>
      <table>
      <thead>
        <tr>
          <th>Total Price</th>
          <th>Total Weight</th>

        </tr>
      </thead>
      <tbody>
      <tr>
        <td>{{ formatPrice(group.totalPrice) }}</td>
        <td>{{ formatPrice(group.amount) }} Oz</td>

      </tr>
      </tbody>
      </table>


        <table v-if="group.prices.length >0">
        <thead>
          <tr>
            <th>Name</th>
            <th>Weight</th>
            <th>Unit</th>
            <th>Number of Items</th>
            <th>Unit Price</th>
            <th>Total Price </th>
            <th>Metal</th>
          </tr>
        </thead>
        <tbody>

          <tr v-for="price in group.prices" :key="price.item.id">
            <td>{{ price.item.name }}</td>
            <td>{{ price.item.amount }}</td>
            <td>{{ price.item.unit.name }}</td>
            <td>{{ price.item.itemCount }}</td>
            <td>{{ formatPrice(price.price) }}</td>
            <td>{{ formatPrice(price.priceTotal) }}</td>
            <td>{{ price.item.itemType.material.name }}</td>
          </tr>
        </tbody>
      </table>
      </div>
   </div>
    </div>
  </div>
</template>

<script>
import axios from '../axios';

export default {
  name: 'PricesComponent',
  data() {
    return {
      viewTypes:[
      {
       id: 'PriceList',
       label: 'Price list'

      },
      {
       id: 'GroupByMetal',
       label: 'Group by Metal'
      }

      ],
      priceList: {totalPrice:0, prices:[]},
      priceGroups:{},
      metals: [],
      currentViewType: 'PriceList',
      errorMessage: '',
      currentFilter: ''
    };

  },

  mounted() {
    this.currentFilter=this.getCurrentFilter();
    this.currentViewType=this.getCurrentViewType();
    this.fetchData();

  },
  methods: {
    getCurrentFilter(){
      return sessionStorage.getItem("PriceMaterialFilter")?sessionStorage.getItem("PriceMaterialFilter"):"";
    },

    setCurrentFilter(event){

      sessionStorage.setItem("PriceMaterialFilter",event.target.value);
      this.currentFilter=event.target.value;
      this.fetchData();
    },
    getCurrentViewType(){
      return sessionStorage.getItem("PriceViewType")?sessionStorage.getItem("PriceViewType"):"PriceList";
    },
    setCurrentViewType(event){

      sessionStorage.setItem("PriceViewType",event.target.value);
      this.currentViewType=event.target.value;
      this.fetchData();
    },
    async fetchData() {
      this.errorMessage='';
      this.priceList={totalPrice:0, prices:[]};
      this.priceGroups={};

      var errorMessage="Error fetching data. Please try again later.";

      if(this.currentViewType === "PriceList"){
         console.log("PriceList");
         this.fetchPriceList();
      }
      else{
        this.fetchPriceGroupsByMetal();
      }

      try{
        errorMessage="Error fetching metals. Please try again later.";
        const response = await axios.get(`/materials`);
        this.metals = response.data.map(metal => ({
          id: metal.id,
          name: metal.name
        }));

      } catch (error) {
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,errorMessage);
      }
    },
   async fetchPriceList(){
      var errorMessage="Error fetching data. Please try again later.";
      try{
          if(this.currentFilter != "" && this.currentFilter != null){

            errorMessage="No price for selected metal available.";
            console.log('currentFilter:',this.currentFilter);
            const response = await axios.get(`/prices/material/`+this.currentFilter);

            this.priceList = response.data;
            console.log('priceList:',this.priceList);
          }
          else{

            const response =  await axios.get(`/prices`);

            this.priceList = response.data;
            console.log('priceList:',this.priceList);
          }
      }
      catch(error){
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,errorMessage);
      }
    },

   async fetchPriceGroupsByMetal(){
      var errorMessage="No price groups available.";
      try{
         console.log('currentFilter:',this.currentFilter);
         const response = await axios.get(`/prices/groupBy/material`);
         this.priceGroups = response.data.priceGroups;
         console.log('priceGroups:',this.priceGroups);

      }
      catch(error){
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,errorMessage);
      }
    },
    formatPrice(value) {
      return Number(value).toFixed(2)
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
