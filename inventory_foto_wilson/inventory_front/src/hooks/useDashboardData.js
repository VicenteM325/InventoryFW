import { useState, useEffect, useCallback } from 'react';
import { saleService } from '@/services/saleService';
import { stockAlertService } from '@/services/stockAlertService';
import { productService } from '@/services/productService';
import { notificationService } from '@/services/notificationService';

// Combina las llamadas ya existentes (ventas recientes, alertas pendientes,
// productos) más el nuevo endpoint de notificaciones para alimentar el
// panel principal con datos reales, en vez de los datos de ejemplo
// estáticos que traía la plantilla original. Un endpoint agregador propio
// solo se justificaría si el volumen de datos creciera mucho; para el
// tamaño de este negocio, combinar en paralelo lo ya existente basta.
export function useDashboardData() {
  const [recentSales, setRecentSales] = useState([]);
  const [pendingAlerts, setPendingAlerts] = useState([]);
  const [products, setProducts] = useState([]);
  const [unreadNotifications, setUnreadNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAll = useCallback(async () => {
    try {
      setLoading(true);
      const [salesData, alertsData, productsData, notificationsData] = await Promise.all([
        saleService.getRecent(),
        stockAlertService.getPending(),
        productService.getAll(),
        notificationService.getAll(false),
      ]);
      setRecentSales(salesData);
      setPendingAlerts(alertsData);
      setProducts(productsData);
      setUnreadNotifications(notificationsData);
      setError(null);
    } catch (err) {
      console.error('Error cargando datos del dashboard:', err);
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  return { recentSales, pendingAlerts, products, unreadNotifications, loading, error, refresh: fetchAll };
}

export default useDashboardData;
