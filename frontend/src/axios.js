import axios from 'axios';
import router from '@/router';
import { jwtDecode } from 'jwt-decode';

function isTokenExpiringSoon() {
	const exp = sessionStorage.getItem('jwtExp');
	if (!exp) return false;

	const expDate = new Date(exp);
	const now = new Date();
	const minutesBeforeExpiry = (expDate - now) / (1000 * 60);

	return minutesBeforeExpiry < 60;
}

async function refreshToken(oldToken) {
	try {
		const response = await axios.get((process.env.VUE_APP_API_BASE_URL ?? "") + "/api/auth/refresh", {
			headers: {
				'Authorization': `Bearer ${oldToken}`
			},
			withCredentials: true
		});
		const newToken = response.data;
		const decodedToken = jwtDecode(response.data);
		const newExpDate = new Date(decodedToken.exp * 1000);

		if (newToken) {
			sessionStorage.setItem('jwt-token', newToken);
			sessionStorage.setItem('jwtExp', newExpDate.toISOString());
			return newToken;
		}
	} catch (error) {
		console.error('Token refresh failed:', error);
		return null;
	}
}

const instance = axios.create({
	baseURL: (process.env.VUE_APP_API_BASE_URL ?? "") + "/api/",
	withCredentials: true
});


instance.interceptors.request.use(async config => {
	let token = sessionStorage.getItem('jwt-token');

	if (token && isTokenExpiringSoon()) {
		token = await refreshToken(token);
	}

	if (token) {
		config.headers['Authorization'] = `Bearer ${token}`;
	}

	return config;
}, error => {
	return Promise.reject(error);
});


instance.interceptors.response.use(response => {

	return response;
}, error => {

	if (error.response && error.response.status === 403) {

		if (router.currentRoute.path !== '/login') {
			sessionStorage.removeItem('jwt-token');
			sessionStorage.removeItem('username');
			sessionStorage.removeItem('jwtExp');
			router.push({ path: '/login' });
		}
	}
	return Promise.reject(error);
});

export default instance;