package airport.model;

public class DomesticFlight extends Flight {
    private static final double GST = 0.05;
    private String terminal;

    public DomesticFlight() { }

    public DomesticFlight(int id, String flightNo, String airline, String origin, String destination,
                          String dep, String arr, int total, int available, double baseFare,
                          Integer pilotId, String terminal) {
        super(id, flightNo, airline, origin, destination, dep, arr, total, available, baseFare, pilotId);
        this.terminal = terminal;
    }

    @Override public String getType() { return "DOMESTIC"; }
    /** Method overriding: 5% GST only. */
    @Override protected double taxAndFees(double fare) { return fare * GST; }
    @Override public String getExtraInfo() { return "Terminal " + terminal; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }
}
