import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});

// Add a request interceptor
api.interceptors.request.use((config) => {
  const credentials = localStorage.getItem('adminCredentials');
  if (credentials) {
    config.headers.Authorization = `Basic ${credentials}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

// Add a response interceptor to handle 401 Unauthorized
api.interceptors.response.use((response) => response, (error) => {
  if (error.response && error.response.status === 401) {
    localStorage.removeItem('adminCredentials');
    window.dispatchEvent(new Event('auth-failed'));
  }
  return Promise.reject(error);
});

export const fetchClients = async () => {
  const response = await api.get('/admin/clients');
  return response.data;
};

export const fetchTrafficStats = async () => {
  const response = await api.get('/admin/traffic');
  return response.data;
};

export const unblockClient = async (ip) => {
  const response = await api.post(`/admin/unblock/${ip}`);
  return response.data;
};

export const resetDemo = async () => {
  const response = await api.post('/admin/reset');
  return response.data;
};
