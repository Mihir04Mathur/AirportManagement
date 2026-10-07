package airport.interfaces;

/** INTERFACE: an object that can be written as one row of a CSV file. */
public interface Exportable {
    String[] csvHeader();
    String[] csvRow();
}
