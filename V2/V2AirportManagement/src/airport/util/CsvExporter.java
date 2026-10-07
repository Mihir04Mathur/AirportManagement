package airport.util;

import airport.exception.ExportException;
import airport.interfaces.Exportable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** File handling with java.io / java.nio: writes CSV files and text reports. */
public final class CsvExporter {
    private CsvExporter() { }

    /** Writes any list of Exportable objects (generic wildcard) to a CSV file. */
    public static Path export(List<? extends Exportable> items, Path file) throws ExportException {
        if (items == null || items.isEmpty()) throw new ExportException("Nothing to export - there are no records.");
        try {
            createParent(file);
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                w.write('\uFEFF');                         // BOM so Excel shows ₹ and arrows correctly
                w.write(line(items.get(0).csvHeader()));
                for (Exportable item : items) w.write(line(item.csvRow()));
            }
            return file;
        } catch (IOException e) {
            throw new ExportException("Could not write " + file.getFileName() + ": " + e.getMessage(), e);   // exception chaining
        }
    }

    /** Writes plain text (used for the full report) with PrintWriter + FileWriter. */
    public static Path writeText(String text, Path file) throws ExportException {
        try {
            createParent(file);
            try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file.toFile(), StandardCharsets.UTF_8)))) {
                pw.print(text);
            }
            return file;
        } catch (IOException e) {
            throw new ExportException("Could not write " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    private static void createParent(Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
    }

    private static String line(String[] cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(cells[i]));
        }
        return sb.append("\r\n").toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}
