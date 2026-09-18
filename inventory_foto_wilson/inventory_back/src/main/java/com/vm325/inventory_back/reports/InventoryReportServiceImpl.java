package com.vm325.inventory_back.reports;

import com.vm325.inventory_back.entities.Product;
import com.vm325.inventory_back.entities.StockAlert;
import com.vm325.inventory_back.enums.StockAlertStatus;
import com.vm325.inventory_back.repositories.ProductRepository;
import com.vm325.inventory_back.repositories.StockAlertRepository;
import com.vm325.inventory_back.services.StockStatusCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryReportServiceImpl implements InventoryReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProductRepository productRepository;
    private final StockAlertRepository stockAlertRepository;
    private final StockStatusCalculator stockStatusCalculator;
    private final ReportWriterFactory reportWriterFactory;

    @Override
    public byte[] generate(ReportFormat format) {
        ReportTable productTable = buildProductTable();
        ReportTable alertTable = buildAlertTable();

        return reportWriterFactory.get(format).write(List.of(productTable, alertTable));
    }

    private ReportTable buildProductTable() {
        List<Product> products = productRepository.findAll();

        List<String> headers = List.of("Producto", "Codigo de barras", "Categoria", "Proveedor", "Stock", "Estado");
        List<List<String>> rows = products.stream()
                .map(p -> List.of(
                        p.getName(),
                        p.getBarcode(),
                        p.getCategory() != null ? p.getCategory().toString() : "-",
                        p.getSupplier() != null ? p.getSupplier().getName() : "-",
                        String.valueOf(p.getStock()),
                        stockStatusCalculator.resolve(p.getStock(), p.getMinStock())
                ))
                .collect(Collectors.toList());

        long disponibles = products.stream().filter(p -> "DISPONIBLE".equals(stockStatusCalculator.resolve(p.getStock(), p.getMinStock()))).count();
        long stockBajo = products.stream().filter(p -> "STOCK_BAJO".equals(stockStatusCalculator.resolve(p.getStock(), p.getMinStock()))).count();
        long agotados = products.stream().filter(p -> "AGOTADO".equals(stockStatusCalculator.resolve(p.getStock(), p.getMinStock()))).count();

        List<String> footer = List.of(
                "Total de productos: " + products.size(),
                "Disponibles: " + disponibles + "  |  Stock bajo: " + stockBajo + "  |  Agotados: " + agotados
        );

        return new ReportTable("Reporte de Inventario", headers, rows, footer);
    }

    private ReportTable buildAlertTable() {
        List<StockAlert> alerts = stockAlertRepository.findAll();
        alerts.sort(Comparator.comparing(StockAlert::getCreatedAt).reversed());

        List<String> headers = List.of("Producto", "Stock al generarse", "Estado", "Generada", "Atendida");
        List<List<String>> rows = alerts.stream()
                .map(a -> List.of(
                        a.getProduct().getName(),
                        String.valueOf(a.getStockAtAlert()),
                        a.getStatus().toString(),
                        a.getCreatedAt().format(DATE_FMT),
                        a.getAttendedAt() == null ? "-" : a.getAttendedAt().format(DATE_FMT)
                ))
                .collect(Collectors.toList());

        long pendientes = alerts.stream().filter(a -> a.getStatus() == StockAlertStatus.PENDIENTE).count();
        List<String> footer = List.of(
                "Alertas pendientes: " + pendientes + "  |  Atendidas: " + (alerts.size() - pendientes)
        );

        return new ReportTable("Alertas de Reposicion de Stock", headers, rows, footer);
    }
}
