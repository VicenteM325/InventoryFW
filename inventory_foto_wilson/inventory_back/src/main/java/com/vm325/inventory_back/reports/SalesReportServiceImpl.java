package com.vm325.inventory_back.reports;

import com.vm325.inventory_back.entities.Sale;
import com.vm325.inventory_back.repositories.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesReportServiceImpl implements SalesReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SaleRepository saleRepository;
    private final ReportWriterFactory reportWriterFactory;

    @Override
    public byte[] generate(LocalDateTime from, LocalDateTime to, ReportFormat format) {
        List<Sale> sales = saleRepository.findByDateBetweenOrderByDateAsc(from, to);

        List<String> headers = List.of("Fecha", "Empleado", "Cliente", "Productos", "Total (Q)");
        List<List<String>> rows = new ArrayList<>();
        BigDecimal totalPeriodo = BigDecimal.ZERO;

        for (Sale sale : sales) {
            String productos = sale.getDetails().stream()
                    .map(detail -> detail.getProduct().getName() + " x" + detail.getAmount())
                    .collect(Collectors.joining(", "));

            rows.add(List.of(
                    sale.getDate().format(DATE_FMT),
                    sale.getEmployee().getUserName(),
                    sale.getCustomerName() == null || sale.getCustomerName().isBlank() ? "-" : sale.getCustomerName(),
                    productos.isBlank() ? "-" : productos,
                    sale.getTotal().toPlainString()
            ));
            totalPeriodo = totalPeriodo.add(sale.getTotal());
        }

        int cantidadTransacciones = sales.size();
        BigDecimal ticketPromedio = cantidadTransacciones == 0
                ? BigDecimal.ZERO
                : totalPeriodo.divide(BigDecimal.valueOf(cantidadTransacciones), 2, RoundingMode.HALF_UP);

        List<String> footer = List.of(
                "Total del periodo: Q " + totalPeriodo.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "Cantidad de transacciones: " + cantidadTransacciones,
                "Ticket promedio: Q " + ticketPromedio.toPlainString()
        );

        String title = "Reporte de Ventas (" + from.format(DATE_FMT) + " a " + to.format(DATE_FMT) + ")";
        ReportTable table = new ReportTable(title, headers, rows, footer);

        return reportWriterFactory.get(format).write(List.of(table));
    }
}
