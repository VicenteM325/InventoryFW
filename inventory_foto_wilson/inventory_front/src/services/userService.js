import api from './api';

const API_URL = '/api/users';

export const userService = {
  getAll: () => api.get(API_URL).then((res) => res.data),
  create: (data) => api.post(API_URL, data).then((res) => res.data),
  update: (id, data) => api.put(`${API_URL}/${id}`, data).then((res) => res.data),
  deactivate: (id) => api.patch(`${API_URL}/${id}/deactivate`).then((res) => res.data),
  activate: (id) => api.patch(`${API_URL}/${id}/activate`).then((res) => res.data),
};
