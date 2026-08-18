import api from './api';

const API_URL = '/api/stock-alerts';

export const stockAlertService = {
  getPending: () => api.get(`${API_URL}/pending`).then(res => res.data),
  attend: (id) => api.patch(`${API_URL}/${id}/attend`).then(res => res.data),
};