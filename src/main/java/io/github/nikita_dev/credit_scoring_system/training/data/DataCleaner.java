package io.github.nikita_dev.credit_scoring_system.training.data;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class DataCleaner {
    private static final List<String> DESIRED_COLUMNS = List.of(
            "loan_amnt", "term", "int_rate", "grade", "emp_length",
            "home_ownership", "annual_inc", "verification_status",
            "purpose", "dti", "fico_range_low", "fico_range_high",
            "open_acc", "pub_rec", "revol_bal", "revol_util", "total_acc",
            "initial_list_status", "application_type", "loan_status"
    );

    public void cleanCsvFile(Path inputCsvPath, Path outputCsvPath) throws IOException, CsvValidationException {
        log.info("Starting data cleaning process. Input: '{}', Output: '{}'", inputCsvPath.getFileName(), outputCsvPath.getFileName());

        prepareOutputDirectory(outputCsvPath);
        try (
                CSVReader reader = new CSVReader(new FileReader(inputCsvPath.toFile()));
                ICSVWriter writer = new CSVWriterBuilder(new FileWriter(outputCsvPath.toFile()))
                        .withSeparator(ICSVWriter.DEFAULT_SEPARATOR)
                        .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
                        .withEscapeChar(ICSVWriter.DEFAULT_ESCAPE_CHARACTER)
                        .withLineEnd(ICSVWriter.DEFAULT_LINE_END)
                        .build()
        ) {
            Map<String, Integer> headerMap = processAndWriteHeader(reader, writer);
            processAndWriteRows(reader, writer, headerMap);
        }
    }

    private void prepareOutputDirectory(Path outputPath) throws IOException {
        Path parentDir = outputPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            log.debug("Creating output directory: {}", parentDir);
            Files.createDirectories(parentDir);
        }
    }

    private Map<String, Integer> processAndWriteHeader(CSVReader reader, ICSVWriter writer) throws IOException, CsvValidationException {
        log.debug("Processing CSV header...");
        String[] header = reader.readNext();
        if (header == null) {
            throw new IOException("CSV file is empty or corrupted.");
        }

        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            headerMap.put(header[i].trim(), i);
        }
        log.debug("Built header map for {} columns.", headerMap.size());

        for (String desiredColumn : DESIRED_COLUMNS) {
            if (!headerMap.containsKey(desiredColumn)) {
                log.warn("Desired column '{}' not found in the input CSV header.", desiredColumn);
            }
        }

        List<String> outputHeader = DESIRED_COLUMNS.stream()
                .filter(col -> !col.equals("loan_status"))
                .collect(Collectors.toList());
        outputHeader.add("is_approved");

        writer.writeNext(outputHeader.toArray(new String[0]));
        log.debug("Successfully wrote new header to the output file.");
        return headerMap;
    }

    private void processAndWriteRows(CSVReader reader, ICSVWriter writer, Map<String, Integer> headerMap) throws IOException, CsvValidationException {
        log.debug("Starting to process data rows...");
        String[] row;
        long processedCount = 0;
        long writtenCount = 0;
        long skippedCount = 0;

        while ((row = reader.readNext()) != null) {
            processedCount++;
            String loanStatus = row[headerMap.get("loan_status")];

            if (!isRelevantStatus(loanStatus)) {
                skippedCount++;
                continue;
            }

            String[] cleanedRow = buildRow(row, headerMap, loanStatus);
            writer.writeNext(cleanedRow);
            writtenCount++;
        }

        log.info("Data cleaning finished. Processed: {}, Written: {}, Skipped: {}.", processedCount, writtenCount, skippedCount);
    }

    private String[] buildRow(String[] originalRow, Map<String, Integer> headerMap, String loanStatus) {
        List<String> cleaned = DESIRED_COLUMNS.stream()
                .filter(col -> !col.equals("loan_status"))
                .map(col -> transformValue(col, originalRow[headerMap.get(col)]))
                .collect(Collectors.toList());

        cleaned.add(mapLoanStatusToApproval(loanStatus));
        return cleaned.toArray(new String[0]);
    }

    private String mapLoanStatusToApproval(String loanStatus) {
        return loanStatus.equals("Fully Paid") ? "1" : "0";
    }

    private boolean isRelevantStatus(String status) {
        return status.equals("Fully Paid") || status.equals("Charged Off") || status.equals("Default");
    }

    private String transformValue(String colName, String value) {
        if (value == null) return "";
        value = value.trim();

        return switch (colName) {
            case "term" -> value.replaceAll("\\D+", "");
            case "int_rate", "revol_util" -> value.replace("%", "");
            case "emp_length" -> transformEmpLength(value);
            default -> value;
        };
    }

    private String transformEmpLength(String empLength) {
        if (empLength == null || empLength.equalsIgnoreCase("n/a")) return "";
        if (empLength.contains("< 1 year")) return "0";
        if (empLength.contains("10+ years")) return "10";
        return empLength.replaceAll("\\D+", "");
    }
}

