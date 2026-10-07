package airport.model;

public class InternationalFlight extends Flight {
    private static final double TAX = 0.12;
    private static final double AIRPORT_FEE = 1500;
    private boolean visaRequired;

    public InternationalFlight() { }

    public InternationalFlight(int id, String flightNo, String airline, String origin, String destination,
                               String dep, String arr, int total, int available, double baseFare,
                               Integer pilotId, boolean visaRequired) {
        super(id, flightNo, airline, origin, destination, dep, arr, total, available, baseFare, pilotId);
        this.visaRequired = visaRequired;
    }

    @Override public String getType() { return "INTERNATIONAL"; }
    /** Method overriding: 12% tax + fixed airport development fee. */
    @Override protected double taxAndFees(double fare) { return fare * TAX + AIRPORT_FEE; }
    @Override public String getExtraInfo() { return visaRequired ? "Visa required" : "No visa needed"; }

    public boolean isVisaRequired() { return visaRequired; }
    public void setVisaRequired(boolean visaRequired) { this.visaRequired = visaRequired; }
}
