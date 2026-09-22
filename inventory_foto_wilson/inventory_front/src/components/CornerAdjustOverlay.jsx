import { useRef } from 'react';

const CORNER_ORDER = ['topLeft', 'topRight', 'bottomRight', 'bottomLeft'];

/**
 * Imagen con un cuadrilátero arrastrable superpuesto (SVG + Pointer Events)
 * para ajustar a mano el recorte cuando la detección automática del borde
 * de la tarjeta se equivoca. `imageWidth`/`imageHeight` y `corners` están en
 * el mismo espacio de píxeles que la imagen mostrada (la vista previa que
 * devuelve el backend), no en el de la imagen a tamaño completo.
 */
export function CornerAdjustOverlay({ imageUrl, imageWidth, imageHeight, corners, onChange, disabled }) {
  const svgRef = useRef(null);
  const draggingRef = useRef(null);

  const toImageSpace = (clientX, clientY) => {
    const rect = svgRef.current.getBoundingClientRect();
    const x = ((clientX - rect.left) / rect.width) * imageWidth;
    const y = ((clientY - rect.top) / rect.height) * imageHeight;
    return {
      x: Math.min(Math.max(x, 0), imageWidth),
      y: Math.min(Math.max(y, 0), imageHeight),
    };
  };

  const handlePointerDown = (cornerKey) => (e) => {
    if (disabled) return;
    e.preventDefault();
    e.target.setPointerCapture(e.pointerId);
    draggingRef.current = cornerKey;
  };

  const handlePointerMove = (e) => {
    if (!draggingRef.current || disabled) return;
    onChange(draggingRef.current, toImageSpace(e.clientX, e.clientY));
  };

  const handlePointerUp = (e) => {
    if (draggingRef.current) {
      e.target.releasePointerCapture?.(e.pointerId);
    }
    draggingRef.current = null;
  };

  const points = CORNER_ORDER.map((key) => `${corners[key].x},${corners[key].y}`).join(' ');

  return (
    <div className="relative w-full" style={{ aspectRatio: `${imageWidth} / ${imageHeight}` }}>
      <img src={imageUrl} alt="Foto con esquinas detectadas" className="absolute inset-0 h-full w-full object-contain" />
      <svg
        ref={svgRef}
        viewBox={`0 0 ${imageWidth} ${imageHeight}`}
        preserveAspectRatio="none"
        className="absolute inset-0 h-full w-full touch-none"
      >
        <polygon points={points} fill="rgba(37,99,235,0.15)" stroke="#2563eb" strokeWidth={imageWidth * 0.004} />
        {CORNER_ORDER.map((key) => (
          <circle
            key={key}
            cx={corners[key].x}
            cy={corners[key].y}
            r={imageWidth * 0.015}
            fill="#2563eb"
            stroke="white"
            strokeWidth={imageWidth * 0.003}
            className={disabled ? '' : 'cursor-move'}
            onPointerDown={handlePointerDown(key)}
            onPointerMove={handlePointerMove}
            onPointerUp={handlePointerUp}
          />
        ))}
      </svg>
    </div>
  );
}

export default CornerAdjustOverlay;
