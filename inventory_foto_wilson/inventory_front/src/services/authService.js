import axios from "axios";

const API_URL = "http://localhost:8080/auth";

// Configurar axios con interceptores
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Interceptor para agregar el token a todas las peticiones
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

// Interceptor para manejar errores de respuesta
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.log('Sesión expirada o no válida');
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

export const login = async (userName, password) => {
  try {
    const response = await api.post('/login', { userName, password });
    console.log('authService - login response:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error en login:', error);
    throw error;
  }
};

const handleLogin = async (userName, password) => {
  try {
    const response = await login(userName, password);
    // Guardar el token y datos del usuario
    localStorage.setItem('token', response.token);
    localStorage.setItem('user', JSON.stringify(response.user));
    // Redirigir al perfil
    navigate('/profile');
  } catch (error) {
    console.error('Error en login:', error);
    // Mostrar mensaje de error
  }
};
export const getUserDetails = async () => {
  try {
    const response = await api.get('/user/details');
    console.log('authService - getUserDetails response:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error al obtener detalles del usuario:', error);
    throw error;
  }
};

export const logout = async (token) => {
  try {
    const response = await api.post('/logout');
    return response.data;
  } catch (error) {
    console.error('Error en logout:', error);
    throw error;
  }
};

export default {
  login,
  getUserDetails,
  logout,
};