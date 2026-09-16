import api from './api';

const API_URL = '/api/products';

export const productService = {
  getAll: () => api.get(API_URL).then(res => res.data),
  getById: (id) => api.get(`${API_URL}/${id}`).then(res => res.data),
  create: (data) => api.post(API_URL, data).then(res => res.data),
  update: (id, data) => api.put(`${API_URL}/${id}`, data).then(res => res.data),
  delete: (id) => api.delete(`${API_URL}/${id}`),
  activate: (id) => api.patch(`${API_URL}/${id}/activate`).then(res => res.data),
};