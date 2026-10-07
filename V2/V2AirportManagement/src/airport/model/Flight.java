package airport.model;

import airport.interfaces.Bookable;
import airport.interfaces.Exportable;

/** Abstract Flight - implements the Bookable interface. */
public abstract class Flight implements Bookable, Exportable {
    public static final double BUSINESS_MULTIPLIER = 2.5;

    private int id;
    private String flightNo;
    private String airline;
    private String origin;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private int totalSeats;
    private int availableSeats;
    private double baseFare;
    private Integer pilotId;

    public Flight() { }

    public Flight(int id, String flightNo, String airline, String origin, String destination,
                  String departureTime, String arrivalTime, int totalSeats, int availableSeats,
                  double baseFare, Integer pilotId) {
        this.id = id;
        this.flightNo = flightNo;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.baseFare = baseFare;
        this.pilotId = pilotId;
    }

    /** Overridden by DomesticFlight / InternationalFlight (POLYMORPHISM). */
    public abstract String getType();
    protected abstract double taxAndFees(double fareBeforeTax);
    public abstract String getExtraInfo();

    @Override
    public double calculateFare(String travelClass) {
        double fare = baseFare;
        if ("BUSINESS".equalsIgnoreCase(travelClass)) fare *= BUSINESS_MULTIPLIER;
        return Math.round((fare + taxAndFees(fare)) * 100.0) / 100.0;
    }

    @Override public boolean isAvailable() { return availableSeats > 0; }
    @Override public int getAvailableSeats() { return availableSeats; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFlightNo() { return flightNo; }
    public void setFlightNo(String flightNo) { this.flightNo = flightNo; }
    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    @Override public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }
    public Integer getPilotId() { return pilotId; }
    public void setPilotId(Integer pilotId) { this.pilotId = pilotId; }

    @Override
    public String toString() {
        return flightNo + "  " + origin + " → " + destination + "  (" + departureTime + ")";
    }

    @Override
    public String[] csvHeader() {
        return new String[]{"ID", "Flight No", "Airline", "Origin", "Destination", "Departure", "Arrival",
                "Type", "Total Seats", "Available Seats", "Base Fare", "Info"};
    }

    @Override
    public String[] csvRow() {
        return new String[]{String.valueOf(id), flightNo, airline, origin, destination, departureTime, arrivalTime,
                getType(), String.valueOf(totalSeats), String.valueOf(availableSeats),
                String.format("%.2f", baseFare), getExtraInfo()};
    }
}
