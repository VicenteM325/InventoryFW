import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Adjunta el token guardado (si existe) a toda petición saliente.
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Ante una respuesta 401, la sesión ya no es válida: limpiar el estado local
// y redirigir a login (salvo que ya estemos ahí).
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('roles');

      if (!window.location.pathname.includes('/auth/sign-in')) {
        window.location.href = '/auth/sign-in';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
