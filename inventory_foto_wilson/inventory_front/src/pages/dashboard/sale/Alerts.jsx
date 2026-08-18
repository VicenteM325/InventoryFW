import React, { useState, useEffect, useContext } from 'react';
import {
  Card,
  Typography,
  Chip,
  Button,
  Spinner,
} from "@material-tailwind/react";
import { stockAlertService } from "@/services/stockAlertService";
import { AuthContext } from "@/context/AuthContext";

export function Alerts() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(null);
  const { roles } = useContext(AuthContext);
  
  const isAdmin = roles?.includes('ADMIN') || roles?.includes('ROLE_ADMIN');

  useEffect(() => {
    fetchAlerts();
  }, []);

  const fetchAlerts = async () => {
    try {
      setLoading(true);
      const data = await stockAlertService.getPending();
      setAlerts(data);
    } catch (error) {
      console.error('Error fetching alerts:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAttend = async (id) => {
    setProcessing(id);
    try {
      await stockAlertService.attend(id);
      await fetchAlerts();
    } catch (error) {
      console.error('Error attending alert:', error);
      alert(error.response?.data?.message || 'Error al atender la alerta');
    } finally {
      setProcessing(null);
    }
  };

  const getAlertColor = (stock) => {
    if (stock <= 5) return 'red';
    if (stock <= 10) return 'orange';
    return 'amber';
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spinner className="h-8 w-8" />
      </div>
    );
  }

  return (
    <div className="mt-12">
      <div className="flex justify-between items-center mb-6">
        <Typography variant="h5" color="blue-gray">
          Alertas de Stock Pendientes
        </Typography>
        <Chip
          value={`${alerts.length} pendientes`}
          color={alerts.length > 0 ? 'orange' : 'green'}
          size="md"
        />
      </div>

      {alerts.length === 0 ? (
        <Card className="p-8 text-center">
          <Typography color="gray" className="text-lg">
            🎉 No hay alertas de stock pendientes
          </Typography>
          <Typography color="gray" className="text-sm">
            Todos los productos tienen stock suficiente
          </Typography>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          {alerts.map((alert) => (
            <Card key={alert.stockAlertId} className="p-6">
              <div className="flex justify-between items-start">
                <div>
                  <Typography variant="h6" color="blue-gray">
                    {alert.productName}
                  </Typography>
                  <Typography variant="small" color="gray">
                    ID Producto: {alert.productId}
                  </Typography>
                  <div className="mt-2">
                    <Chip
                      value={`Stock: ${alert.stockAtAlert} unidades`}
                      color={getAlertColor(alert.stockAtAlert)}
                      size="sm"
                    />
                  </div>
                  <Typography variant="small" color="gray" className="mt-2">
                    Creada: {new Date(alert.createdAt).toLocaleString()}
                  </Typography>
                </div>
                {isAdmin && (
                  <Button
                    color="green"
                    size="sm"
                    onClick={() => handleAttend(alert.stockAlertId)}
                    disabled={processing === alert.stockAlertId}
                  >
                    {processing === alert.stockAlertId ? 'Procesando...' : 'Atender'}
                  </Button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}