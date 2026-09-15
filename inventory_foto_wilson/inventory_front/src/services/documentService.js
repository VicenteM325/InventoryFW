import api from './api';

const API_URL = '/api/documents';

export const documentService = {
  generateDpi: (front, back, format, clientName, settings, frontRotation, backRotation) => {
    const formData = new FormData();
    formData.append('front', front);
    formData.append('back', back);
    formData.append('format', format);
    if (clientName) {
      formData.append('clientName', clientName);
    }
    formData.append('brightness', settings.brightness);
    formData.append('contrast', settings.contrast);
    formData.append('sharpness', settings.sharpness);
    formData.append('frontRotation', frontRotation);
    formData.append('backRotation', backRotation);
    return api
      .post(`${API_URL}/dpi`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        responseType: 'blob',
      })
      .then((res) => res.data);
  },

  previewDpiImage: (file, side, settings, rotation) => {
    const formData = new FormData();
    formData.append('image', file);
    formData.append('side', side);
    formData.append('brightness', settings.brightness);
    formData.append('contrast', settings.contrast);
    formData.append('sharpness', settings.sharpness);
    formData.append('rotation', rotation);
    return api
      .post(`${API_URL}/dpi/preview`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        responseType: 'blob',
      })
      .then((res) => res.data);
  },
};
