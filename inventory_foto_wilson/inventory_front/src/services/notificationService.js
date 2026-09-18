import api from './api';

const API_URL = '/api/notifications';

export const notificationService = {
  getAll: (read) =>
    api.get(API_URL, { params: read === undefined ? {} : { read } }).then((res) => res.data),
  markAsRead: (id) => api.patch(`${API_URL}/${id}/read`).then((res) => res.data),
};
