import React, { useEffect, useRef, useState } from 'react';
import {
  Card,
  Typography,
  Button,
  Input,
  Select,
  Option,
} from "@material-tailwind/react";
import { ArrowPathIcon } from "@heroicons/react/24/outline";
import { documentService } from "@/services/documentService";
import { downloadBlob, printBlob, extractErrorMessage } from "@/utils/fileDownload";

const PREVIEW_DEBOUNCE_MS = 400;

function useProcessedPreview(file, side, rotation, settings) {
  const [previewUrl, setPreviewUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const requestIdRef = useRef(0);
  const previewUrlRef = useRef(null);

  useEffect(() => {
    if (!file) {
      setPreviewUrl(null);
      setLoading(false);
      return;
    }

    const requestId = ++requestIdRef.current;
    setLoading(true);
    const timer = setTimeout(async () => {
      try {
        const blob = await documentService.previewDpiImage(file, side, settings, rotation);
        if (requestIdRef.current !== requestId) return; // llegó una respuesta vieja, se ignora
        const url = URL.createObjectURL(blob);
        if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current);
        previewUrlRef.current = url;
        setPreviewUrl(url);
      } catch {
        if (requestIdRef.current === requestId) setPreviewUrl(null);
      } finally {
        if (requestIdRef.current === requestId) setLoading(false);
      }
    }, PREVIEW_DEBOUNCE_MS);

    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [file, side, rotation, settings.brightness, settings.contrast, settings.sharpness]);

  useEffect(() => () => {
    if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current);
  }, []);

  return { previewUrl, loading };
}

function ImageDropzone({ label, file, previewUrl, onChange, onRotate, processedPreviewUrl, processedLoading }) {
  const inputId = `dpi-image-${label}`;

  return (
    <div>
      <Typography variant="small" color="blue-gray" className="mb-2 font-medium">
        {label}
      </Typography>
      <label
        htmlFor={inputId}
        className="flex h-40 cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed border-blue-gray-200 bg-blue-gray-50/50 p-4 text-center hover:border-blue-400"
      >
        {previewUrl ? (
          <img src={previewUrl} alt={`Foto original ${label}`} className="h-full max-h-32 object-contain" />
        ) : (
          <>
            <Typography color="blue-gray" className="font-medium">
              Arrastra la imagen aquí
            </Typography>
            <Typography variant="small" color="gray">
              o haz clic para seleccionarla
            </Typography>
          </>
        )}
      </label>
      <input
        id={inputId}
        type="file"
        accept="image/jpeg,image/png"
        className="hidden"
        onChange={(e) => onChange(e.target.files?.[0] ?? null)}
      />
      {file && (
        <Typography variant="small" color="gray" className="mt-1 truncate">
          {file.name}
        </Typography>
      )}

      {file && (
        <div className="mt-3">
          <div className="mb-1 flex items-center justify-between">
            <Typography variant="small" color="blue-gray" className="font-medium">
              Vista previa procesada
            </Typography>
            <Button
              size="sm"
              variant="text"
              className="flex items-center gap-1 p-1"
              onClick={onRotate}
              type="button"
            >
              <ArrowPathIcon className="h-4 w-4" />
              Rotar 90°
            </Button>
          </div>
          <div className="flex h-40 items-center justify-center rounded-lg border border-blue-gray-100 bg-white p-2">
            {processedPreviewUrl ? (
              <img src={processedPreviewUrl} alt={`Vista previa procesada ${label}`} className="h-full max-h-32 object-contain" />
            ) : (
              <Typography variant="small" color="gray">
                {processedLoading ? 'Procesando...' : 'Sin vista previa disponible'}
              </Typography>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export function DpiDocuments() {
  const [front, setFront] = useState(null);
  const [frontRawPreview, setFrontRawPreview] = useState(null);
  const [back, setBack] = useState(null);
  const [backRawPreview, setBackRawPreview] = useState(null);
  const [frontRotation, setFrontRotation] = useState(0);
  const [backRotation, setBackRotation] = useState(0);
  const [brightness, setBrightness] = useState(0);
  const [contrast, setContrast] = useState(0);
  const [sharpness, setSharpness] = useState(15);
  const [clientName, setClientName] = useState('');
  const [format, setFormat] = useState('PDF');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null); // { blob, filename }

  const settings = { brightness, contrast, sharpness };
  const frontPreview = useProcessedPreview(front, 'front', frontRotation, settings);
  const backPreview = useProcessedPreview(back, 'back', backRotation, settings);

  const handleFrontChange = (file) => {
    setFront(file);
    setFrontRawPreview(file ? URL.createObjectURL(file) : null);
    setFrontRotation(0);
    setResult(null);
  };

  const handleBackChange = (file) => {
    setBack(file);
    setBackRawPreview(file ? URL.createObjectURL(file) : null);
    setBackRotation(0);
    setResult(null);
  };

  const handleReset = () => {
    setFront(null);
    setFrontRawPreview(null);
    setBack(null);
    setBackRawPreview(null);
    setFrontRotation(0);
    setBackRotation(0);
    setBrightness(0);
    setContrast(0);
    setSharpness(15);
    setClientName('');
    setResult(null);
    setError('');
  };

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!front || !back) {
      setError('Debes adjuntar las fotos de anverso y reverso');
      return;
    }

    setLoading(true);
    setError('');
    setResult(null);
    try {
      const blob = await documentService.generateDpi(
        front, back, format, clientName, settings, frontRotation, backRotation
      );
      const extension = format === 'DOCX' ? 'docx' : 'pdf';
      const filename = `dpi_${Date.now()}.${extension}`;
      setResult({ blob, filename });
    } catch (err) {
      const message = await extractErrorMessage(err, 'Error al generar el documento');
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mt-12">
      <Typography variant="h5" color="blue-gray" className="mb-1">
        Documentos DPI
      </Typography>
      <Typography variant="small" color="gray" className="mb-6">
        Sube el anverso y reverso; el sistema detecta el borde de la tarjeta, la endereza y ajusta el color automáticamente
      </Typography>

      <Card className="p-6">
        <form onSubmit={handleGenerate} className="space-y-6">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <ImageDropzone
              label="Anverso"
              file={front}
              previewUrl={frontRawPreview}
              onChange={handleFrontChange}
              onRotate={() => setFrontRotation((r) => (r + 90) % 360)}
              processedPreviewUrl={frontPreview.previewUrl}
              processedLoading={frontPreview.loading}
            />
            <ImageDropzone
              label="Reverso"
              file={back}
              previewUrl={backRawPreview}
              onChange={handleBackChange}
              onRotate={() => setBackRotation((r) => (r + 90) % 360)}
              processedPreviewUrl={backPreview.previewUrl}
              processedLoading={backPreview.loading}
            />
          </div>

          <div>
            <Typography variant="small" color="blue-gray" className="mb-3 font-medium">
              Ajustes de imagen (aplican a ambas fotos)
            </Typography>
            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
              <div>
                <Typography variant="small" color="gray">Brillo ({brightness})</Typography>
                <input
                  type="range" min="-100" max="100" value={brightness}
                  onChange={(e) => setBrightness(Number(e.target.value))}
                  className="w-full"
                />
              </div>
              <div>
                <Typography variant="small" color="gray">Contraste ({contrast})</Typography>
                <input
                  type="range" min="-100" max="100" value={contrast}
                  onChange={(e) => setContrast(Number(e.target.value))}
                  className="w-full"
                />
              </div>
              <div>
                <Typography variant="small" color="gray">Nitidez ({sharpness})</Typography>
                <input
                  type="range" min="0" max="100" value={sharpness}
                  onChange={(e) => setSharpness(Number(e.target.value))}
                  className="w-full"
                />
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <Input
              label="Nombre del cliente (opcional)"
              value={clientName}
              onChange={(e) => setClientName(e.target.value)}
            />
            <Select label="Formato de salida" value={format} onChange={(value) => setFormat(value)}>
              <Option value="PDF">PDF</Option>
              <Option value="DOCX">Word (.docx)</Option>
            </Select>
          </div>

          {error && (
            <Typography variant="small" color="red">
              {error}
            </Typography>
          )}

          <div className="flex justify-end gap-4">
            <Button type="button" variant="text" color="blue-gray" onClick={handleReset}>
              Limpiar
            </Button>
            <Button type="submit" color="blue" disabled={loading}>
              {loading ? 'Generando...' : 'Generar documento'}
            </Button>
          </div>
        </form>
      </Card>

      {result && (
        <Card className="mt-6 p-6">
          <Typography variant="h6" color="blue-gray" className="mb-4">
            Documento generado
          </Typography>
          <div className="flex flex-wrap gap-4">
            <Button color="green" onClick={() => downloadBlob(result.blob, result.filename)}>
              Descargar {format === 'DOCX' ? 'Word' : 'PDF'}
            </Button>
            {format === 'PDF' && (
              <Button variant="outlined" color="blue-gray" onClick={() => printBlob(result.blob)}>
                Imprimir
              </Button>
            )}
          </div>
        </Card>
      )}
    </div>
  );
}

export default DpiDocuments;
