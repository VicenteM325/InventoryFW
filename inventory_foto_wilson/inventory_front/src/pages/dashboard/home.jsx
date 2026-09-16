import React from "react";
import { Typography, Card, CardHeader, CardBody, Chip, Spinner } from "@material-tailwind/react";
import {
  BanknotesIcon,
  ExclamationTriangleIcon,
  DocumentTextIcon,
  CubeIcon,
} from "@heroicons/react/24/solid";
import { StatisticsCard } from "@/widgets/cards";
import { useDashboardData } from "@/hooks/useDashboardData";

function isToday(dateString) {
  // El backend serializa LocalDateTime sin zona horaria (p.ej.
  // "2026-09-15T00:39:20.6"). Comparar objetos Date reintroduce una
  // conversión de zona horaria que puede correr la fecha un día, así que
  // en vez de eso se comparan directamente los primeros 10 caracteres
  // (YYYY-MM-DD) contra la fecha local de hoy en el mismo formato.
  const datePart = String(dateString).slice(0, 10);
  const now = new Date();
  const todayPart = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
  return datePart === todayPart;
}

export function Home() {
  const { recentSales, pendingAlerts, products, unreadNotifications, loading } = useDashboardData();

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spinner className="h-8 w-8" />
      </div>
    );
  }

  const salesToday = recentSales.filter((sale) => isToday(sale.date));
  const totalToday = salesToday.reduce((sum, sale) => sum + (sale.total || 0), 0);
  const documentsGenerated = unreadNotifications.filter((n) => n.type === "DOCUMENTO_GENERADO").length;

  const statisticsCardsData = [
    {
      color: "blue",
      icon: BanknotesIcon,
      title: "Ventas de hoy",
      value: `Q ${totalToday.toFixed(2)}`,
      footerLabel: `${salesToday.length} transacciones hoy`,
    },
    {
      color: "red",
      icon: ExclamationTriangleIcon,
      title: "Alertas de stock bajo",
      value: pendingAlerts.length,
      footerLabel: pendingAlerts.length > 0 ? "Requieren reposición" : "Todo en orden",
    },
    {
      color: "green",
      icon: DocumentTextIcon,
      title: "Documentos DPI generados",
      value: documentsGenerated,
      footerLabel: "Sin leer",
    },
    {
      color: "purple",
      icon: CubeIcon,
      title: "Productos en inventario",
      value: products.length,
      footerLabel: `${products.filter((p) => p.stockStatus === "AGOTADO").length} agotados`,
    },
  ];

  return (
    <div className="mt-12">
      <div className="mb-12 grid gap-y-10 gap-x-6 md:grid-cols-2 xl:grid-cols-4">
        {statisticsCardsData.map(({ icon, title, value, footerLabel, color }) => (
          <StatisticsCard
            key={title}
            color={color}
            title={title}
            value={value}
            icon={React.createElement(icon, { className: "w-6 h-6 text-white" })}
            footer={
              <Typography className="font-normal text-blue-gray-600">{footerLabel}</Typography>
            }
          />
        ))}
      </div>

      <div className="mb-6 grid grid-cols-1 gap-6 xl:grid-cols-2">
        <Card className="border border-blue-gray-100 shadow-sm">
          <CardHeader floated={false} shadow={false} color="transparent" className="m-0 p-6">
            <Typography variant="h6" color="blue-gray">
              Alertas de reposición de inventario
            </Typography>
            <Typography variant="small" color="gray">
              Generadas automáticamente cuando el stock cae por debajo del mínimo
            </Typography>
          </CardHeader>
          <CardBody className="pt-0">
            {pendingAlerts.length === 0 ? (
              <Typography color="gray" className="text-sm">
                No hay alertas pendientes
              </Typography>
            ) : (
              <div className="space-y-2">
                {pendingAlerts.slice(0, 6).map((alert) => (
                  <div
                    key={alert.stockAlertId}
                    className="flex items-center justify-between border-b border-blue-gray-50 py-2"
                  >
                    <div>
                      <Typography variant="small" color="blue-gray" className="font-semibold">
                        {alert.productName}
                      </Typography>
                      <Typography variant="small" color="gray">
                        Stock actual: {alert.stockAtAlert}
                        {alert.supplierName ? ` · Proveedor: ${alert.supplierName}` : ''}
                      </Typography>
                    </div>
                    <Chip value="Pendiente" color="red" size="sm" />
                  </div>
                ))}
              </div>
            )}
          </CardBody>
        </Card>

        <Card className="border border-blue-gray-100 shadow-sm">
          <CardHeader floated={false} shadow={false} color="transparent" className="m-0 p-6">
            <Typography variant="h6" color="blue-gray">
              Ventas recientes
            </Typography>
            <Typography variant="small" color="gray">
              Últimas transacciones registradas en el sistema
            </Typography>
          </CardHeader>
          <CardBody className="pt-0">
            {recentSales.length === 0 ? (
              <Typography color="gray" className="text-sm">
                No hay ventas registradas
              </Typography>
            ) : (
              <div className="space-y-2">
                {recentSales.slice(0, 6).map((sale, index) => (
                  <div
                    key={sale.salesId ?? index}
                    className="flex items-center justify-between border-b border-blue-gray-50 py-2"
                  >
                    <div>
                      <Typography variant="small" color="blue-gray" className="font-semibold">
                        {sale.customerName || "Cliente anónimo"}
                      </Typography>
                      <Typography variant="small" color="gray">
                        {new Date(sale.date).toLocaleString()}
                      </Typography>
                    </div>
                    <Typography variant="small" color="blue-gray" className="font-bold">
                      Q {(sale.total || 0).toFixed(2)}
                    </Typography>
                  </div>
                ))}
              </div>
            )}
          </CardBody>
        </Card>
      </div>
    </div>
  );
}

export default Home;
