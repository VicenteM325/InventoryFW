import React, { useState } from 'react';
import {
  Card,
  Typography,
  Button,
  Input,
  Select,
  Option,
} from "@material-tailwind/react";
import { reportService } from "@/services/reportService";
import { downloadBlob, extractErrorMessage } from "@/utils/fileDownload";

function today() {
  // toISOString() convierte a UTC, lo que en una zona horaria negativa
  // (como America/Guatemala) puede adelantar la fecha un día durante la
  // tarde/noche local. Construir el string a partir de los componentes
  // locales evita ese corrimiento.
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function Reports() {
  const [reportType, setReportType] = useState('sales');
  const [from, setFrom] = useState(today());
  const [to, setTo] = useState(today());
  const [format, setFormat] = useState('PDF');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleGenerate = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const extension = format === 'XLSX' ? 'xlsx' : 'pdf';
      let blob;
      let filename;

      if (reportType === 'sales') {
        blob = await reportService.getSalesReport(from, to, format);
        filename = `reporte_ventas_${from}_${to}.${extension}`;
      } else {
        blob = await reportService.getInventoryReport(format);
        filename = `reporte_inventario_${today()}.${extension}`;
      }

      downloadBlob(blob, filename);
    } catch (err) {
      const message = await extractErrorMessage(err, 'Error al generar el reporte');
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mt-12">
      <Typography variant="h5" color="blue-gray" className="mb-1">
        Reportes
      </Typography>
      <Typography variant="small" color="gray" className="mb-6">
        Genera y descarga reportes de ventas o de inventario en Excel o PDF
      </Typography>

      <Card className="p-6">
        <form onSubmit={handleGenerate} className="space-y-6">
          <Select label="Tipo de reporte" value={reportType} onChange={(value) => setReportType(value)}>
            <Option value="sales">Ventas</Option>
            <Option value="inventory">Inventario</Option>
          </Select>

          {reportType === 'sales' && (
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
              <Input
                type="date"
                label="Desde"
                value={from}
                onChange={(e) => setFrom(e.target.value)}
              />
              <Input
                type="date"
                label="Hasta"
                value={to}
                onChange={(e) => setTo(e.target.value)}
              />
            </div>
          )}

          <Select label="Formato" value={format} onChange={(value) => setFormat(value)}>
            <Option value="PDF">PDF</Option>
            <Option value="XLSX">Excel (.xlsx)</Option>
          </Select>

          {error && (
            <Typography variant="small" color="red">
              {error}
            </Typography>
          )}

          <div className="flex justify-end">
            <Button type="submit" color="blue" disabled={loading}>
              {loading ? 'Generando...' : 'Generar y descargar'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}

export default Reports;
