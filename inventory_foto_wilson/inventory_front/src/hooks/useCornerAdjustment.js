import { useEffect, useRef, useState } from 'react';
import { documentService } from '@/services/documentService';

const CORNER_KEYS = ['topLeft', 'topRight', 'bottomRight', 'bottomLeft'];

function defaultCenteredCorners(width, height) {
  const marginX = width * 0.1;
  const marginY = height * 0.1;
  return {
    topLeft: { x: marginX, y: marginY },
    topRight: { x: width - marginX, y: marginY },
    bottomRight: { x: width - marginX, y: height - marginY },
    bottomLeft: { x: marginX, y: height - marginY },
  };
}

function scaleCorners(corners, factor) {
  const scaled = {};
  for (const key of CORNER_KEYS) {
    scaled[key] = { x: corners[key].x * factor, y: corners[key].y * factor };
  }
  return scaled;
}

/**
 * Detecta el borde de una tarjeta al subir/rotar una foto, y permite
 * ajustar sus 4 esquinas a mano como red de seguridad cuando la detección
 * automática se equivoca. `activeCorners` (espacio de la imagen a tamaño
 * completo) es lo que se manda al backend; `previewSpaceCorners` es lo
 * mismo escalado al tamaño de la imagen de vista previa, para dibujar el
 * overlay arrastrable sobre ella.
 */
export function useCornerAdjustment(file, side, rotation) {
  const [detection, setDetection] = useState(null); // { fullWidth, fullHeight, previewWidth, previewHeight, previewImageUrl, autoCorners, detected }
  const [manualCorners, setManualCorners] = useState(null);
  const [loading, setLoading] = useState(false);
  const requestIdRef = useRef(0);

  useEffect(() => {
    setManualCorners(null);
    if (!file) {
      setDetection(null);
      setLoading(false);
      return;
    }

    const requestId = ++requestIdRef.current;
    setLoading(true);
    documentService
      .detectCorners(file, side, rotation)
      .then((result) => {
        if (requestIdRef.current !== requestId) return; // llegó una respuesta vieja, se ignora
        const autoCorners = result.detected
          ? result.corners
          : defaultCenteredCorners(result.fullWidth, result.fullHeight);
        setDetection({
          fullWidth: result.fullWidth,
          fullHeight: result.fullHeight,
          previewWidth: result.previewWidth,
          previewHeight: result.previewHeight,
          previewImageUrl: result.previewImageBase64,
          autoCorners,
          detected: result.detected,
        });
      })
      .catch(() => {
        if (requestIdRef.current === requestId) setDetection(null);
      })
      .finally(() => {
        if (requestIdRef.current === requestId) setLoading(false);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [file, side, rotation]);

  const activeCorners = manualCorners ?? detection?.autoCorners ?? null;
  const previewScale = detection ? detection.previewWidth / detection.fullWidth : 1;
  const previewSpaceCorners = activeCorners ? scaleCorners(activeCorners, previewScale) : null;

  const updateCornerFromPreview = (cornerKey, previewPoint) => {
    if (!detection || !activeCorners) return;
    const fullScale = detection.fullWidth / detection.previewWidth;
    const next = { ...activeCorners, [cornerKey]: { x: previewPoint.x * fullScale, y: previewPoint.y * fullScale } };
    setManualCorners(next);
  };

  const resetToAutoDetection = () => setManualCorners(null);

  return {
    detection,
    loading,
    activeCorners,
    previewSpaceCorners,
    isManual: manualCorners !== null,
    updateCornerFromPreview,
    resetToAutoDetection,
  };
}
