import React, { useState } from 'react';
import {
  Card,
  Typography,
  Button,
  Input,
  Select,
  Option,
} from "@material-tailwind/react";
import { documentService } from "@/services/documentService";
import { downloadBlob, printBlob, extractErrorMessage } from "@/utils/fileDownload";

function ImageDropzone({ label, file, previewUrl, onChange }) {
  const inputId = `dpi-image-${label}`;

  return (
    <div>
      <Typography variant="small" color="blue-gray" className="mb-2 font-medium">
        {label}
      </Typography>
      <label
        htmlFor={inputId}
        className="flex h-48 cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed border-blue-gray-200 bg-blue-gray-50/50 p-4 text-center hover:border-blue-400"
      >
        {previewUrl ? (
          <img src={previewUrl} alt={`Vista previa ${label}`} className="h-full max-h-40 object-contain" />
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
    </div>
  );
}

export function DpiDocuments() {
  const [front, setFront] = useState(null);
  const [frontPreview, setFrontPreview] = useState(null);
  const [back, setBack] = useState(null);
  const [backPreview, setBackPreview] = useState(null);
  const [clientName, setClientName] = useState('');
  const [format, setFormat] = useState('PDF');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null); // { blob, filename }

  const handleFrontChange = (file) => {
    setFront(file);
    setFrontPreview(file ? URL.createObjectURL(file) : null);
    setResult(null);
  };

  const handleBackChange = (file) => {
    setBack(file);
    setBackPreview(file ? URL.createObjectURL(file) : null);
    setResult(null);
  };

  const handleReset = () => {
    setFront(null);
    setFrontPreview(null);
    setBack(null);
    setBackPreview(null);
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
      const blob = await documentService.generateDpi(front, back, format, clientName);
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
        Sube el anverso y reverso; el sistema recorta, ajusta y arma el documento automáticamente
      </Typography>

      <Card className="p-6">
        <form onSubmit={handleGenerate} className="space-y-6">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <ImageDropzone label="Anverso" file={front} previewUrl={frontPreview} onChange={handleFrontChange} />
            <ImageDropzone label="Reverso" file={back} previewUrl={backPreview} onChange={handleBackChange} />
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
