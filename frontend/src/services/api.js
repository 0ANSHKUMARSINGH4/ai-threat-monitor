import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  withCredentials: true // Extremely important for HttpOnly cookies
});

// Add a request interceptor to inject the Bearer token
api.interceptors.request.use((config) => {
    const token = sessionStorage.getItem('auth_token');
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
});

// Add a response interceptor to handle 401 Unauthorized
api.interceptors.response.use((response) => response, (error) => {
  if (error.response && error.response.status === 401) {
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

export const reclassifyClient = async (ipAddress, label, note = '') => {
  const response = await api.post('/admin/reclassify', { 
    ipAddress, 
    label, 
    note 
  });
  return response.data;
};

export default api;
