import api from './api';

const API_URL = '/api/sales';

export const saleService = {
  create: (data) => {
    console.log('Enviando a la API:', data); // Debug
    return api.post(API_URL, data).then(res => res.data);
  },
  getRecent: () => api.get(`${API_URL}/recent`).then(res => res.data),
};