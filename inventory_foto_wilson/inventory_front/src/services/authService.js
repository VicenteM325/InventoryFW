import api from './api';

export const login = async (userName, password) => {
  const response = await api.post('/auth/login', { userName, password });
  return response.data;
};

export const getUserDetails = async () => {
  const response = await api.get('/auth/user/details');
  return response.data;
};

export const logout = async () => {
  const response = await api.post('/auth/logout');
  return response.data;
};

export default {
  login,
  getUserDetails,
  logout,
};
