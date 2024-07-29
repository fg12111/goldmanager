import axios from 'axios';


const instance = axios.create({
  baseURL: "/api/",
});



instance.interceptors.request.use(config => {
  const token = sessionStorage.getItem('jwt-token');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});


export default instance;

