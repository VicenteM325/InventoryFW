// Dispara la descarga de un blob en el navegador (para los binarios que
// devuelven /api/documents/dpi y /api/reports/*).
export function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

// Abre el blob (pensado para PDF) en una pestaña nueva y dispara el diálogo
// de impresión del navegador.
export function printBlob(blob) {
  const url = window.URL.createObjectURL(blob);
  const win = window.open(url, '_blank');
  if (win) {
    win.onload = () => {
      win.focus();
      win.print();
    };
  }
}

// axios con responseType: 'blob' también entrega el cuerpo de una
// respuesta de error como Blob (no como JSON ya parseado), así que hay que
// leerlo como texto y parsearlo manualmente para mostrar el mensaje real
// del backend en vez de un error genérico.
export async function extractErrorMessage(error, fallback) {
  const data = error?.response?.data;
  if (data instanceof Blob) {
    try {
      const text = await data.text();
      const parsed = JSON.parse(text);
      return parsed.message || fallback;
    } catch {
      return fallback;
    }
  }
  return data?.message || fallback;
}
