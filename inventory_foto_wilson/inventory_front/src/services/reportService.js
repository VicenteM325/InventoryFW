import api from './api';

const API_URL = '/api/reports';

export const reportService = {
  getSalesReport: (from, to, format) =>
    api
      .get(`${API_URL}/sales`, { params: { from, to, format }, responseType: 'blob' })
      .then((res) => res.data),
  getInventoryReport: (format) =>
    api
      .get(`${API_URL}/inventory`, { params: { format }, responseType: 'blob' })
      .then((res) => res.data),
};
