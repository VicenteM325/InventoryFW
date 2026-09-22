import api from './api';

const API_URL = '/api/documents';

export const documentService = {
  generateDpi: (front, back, format, clientName, settings, frontRotation, backRotation, frontCorners, backCorners) => {
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
    if (frontCorners) {
      formData.append('frontCorners', JSON.stringify(frontCorners));
    }
    if (backCorners) {
      formData.append('backCorners', JSON.stringify(backCorners));
    }
    return api
      .post(`${API_URL}/dpi`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        responseType: 'blob',
      })
      .then((res) => res.data);
  },

  previewDpiImage: (file, side, settings, rotation, corners) => {
    const formData = new FormData();
    formData.append('image', file);
    formData.append('side', side);
    formData.append('brightness', settings.brightness);
    formData.append('contrast', settings.contrast);
    formData.append('sharpness', settings.sharpness);
    formData.append('rotation', rotation);
    if (corners) {
      formData.append('corners', JSON.stringify(corners));
    }
    return api
      .post(`${API_URL}/dpi/preview`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        responseType: 'blob',
      })
      .then((res) => res.data);
  },

  detectCorners: (file, side, rotation) => {
    const formData = new FormData();
    formData.append('image', file);
    formData.append('side', side);
    formData.append('rotation', rotation);
    return api
      .post(`${API_URL}/dpi/detect-corners`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((res) => res.data);
  },
};
