package com.logicore.benchmark.application.usecase;

import com.logicore.benchmark.domain.model.BenchmarkEntry;
import com.logicore.benchmark.domain.repository.BenchmarkRepository;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BenchmarkExportService {

    private final BenchmarkRepository repository;

    public BenchmarkExportService(BenchmarkRepository repository) {
        this.repository = repository;
    }

    public record ExportResult(byte[] data, String contentType, String filename) {}

    public ExportResult exportCsv(UUID organizationId, int limit) throws IOException {
        List<BenchmarkEntry> entries = repository.findByOrganization(organizationId, limit);
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{
                    "ID", "Strategy", "Type", "OrderCount", "ExecutionTimeMs",
                    "TotalDistanceKm", "RouteScore", "Grade", "TwoOptApplied",
                    "TwoOptImprovementKm", "Feasible", "CreatedAt"
            });
            for (BenchmarkEntry e : entries) {
                writer.writeNext(new String[]{
                        e.id().toString(), e.strategyIdentifier(), e.strategyType(),
                        String.valueOf(e.orderCount()), String.valueOf(e.executionTimeMs()),
                        String.valueOf(e.totalDistanceKm()),
                        e.routeScore() != null ? String.valueOf(e.routeScore()) : "",
                        e.scoreGrade() != null ? e.scoreGrade() : "",
                        String.valueOf(e.twoOptApplied()),
                        String.valueOf(e.twoOptImprovementKm()),
                        String.valueOf(e.feasible()),
                        e.createdAt().toString()
                });
            }
        }
        byte[] bytes = sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new ExportResult(bytes, "text/csv", "benchmark-export.csv");
    }

    public ExportResult exportExcel(UUID organizationId, int limit) throws IOException {
        List<BenchmarkEntry> entries = repository.findByOrganization(organizationId, limit);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Benchmark Results");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Strategy", "Type", "Pedidos", "Tempo(ms)",
                                 "Distância(km)", "Score", "Grau", "2-Opt", "Melhoria 2-Opt(km)",
                                 "Viável", "Criado em"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (BenchmarkEntry e : entries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(e.strategyIdentifier());
                row.createCell(1).setCellValue(e.strategyType());
                row.createCell(2).setCellValue(e.orderCount());
                row.createCell(3).setCellValue(e.executionTimeMs());
                row.createCell(4).setCellValue(e.totalDistanceKm());
                row.createCell(5).setCellValue(e.routeScore() != null ? e.routeScore() : 0);
                row.createCell(6).setCellValue(e.scoreGrade() != null ? e.scoreGrade() : "");
                row.createCell(7).setCellValue(e.twoOptApplied() ? "Sim" : "Não");
                row.createCell(8).setCellValue(e.twoOptImprovementKm());
                row.createCell(9).setCellValue(e.feasible() ? "Sim" : "Não");
                row.createCell(10).setCellValue(e.createdAt().toString());
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ExportResult(out.toByteArray(),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "benchmark-export.xlsx");
        }
    }
}
