import api from './api';

const API_URL = '/api/documents';

export const documentService = {
  generateDpi: (front, back, format, clientName) => {
    const formData = new FormData();
    formData.append('front', front);
    formData.append('back', back);
    formData.append('format', format);
    if (clientName) {
      formData.append('clientName', clientName);
    }
    return api
      .post(`${API_URL}/dpi`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        responseType: 'blob',
      })
      .then((res) => res.data);
  },
};
