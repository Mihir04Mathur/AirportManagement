package airport.model;

import airport.interfaces.Exportable;

/** A ticket booking (joined with passenger / flight names for display). */
public class Booking implements Exportable {
    private int id;
    private int passengerId;
    private int flightId;
    private String seatNo;
    private String travelClass;
    private double fare;
    private String bookingTime;
    private String status;
    // display helpers
    private String passengerName;
    private String flightNo;
    private String route;
    private String paymentMethod;
    private double refundAmount;

    public Booking() { }

    public Booking(int passengerId, int flightId, String travelClass, double fare) {
        this.passengerId = passengerId;
        this.flightId = flightId;
        this.travelClass = travelClass;
        this.fare = fare;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }
    public int getFlightId() { return flightId; }
    public void setFlightId(int flightId) { this.flightId = flightId; }
    public String getSeatNo() { return seatNo; }
    public void setSeatNo(String seatNo) { this.seatNo = seatNo; }
    public String getTravelClass() { return travelClass; }
    public void setTravelClass(String travelClass) { this.travelClass = travelClass; }
    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }
    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public String getFlightNo() { return flightNo; }
    public void setFlightNo(String flightNo) { this.flightNo = flightNo; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(double refundAmount) { this.refundAmount = refundAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    @Override
    public String toString() { return "#" + id + "  " + passengerName + "  " + flightNo + "  Seat " + seatNo; }

    /** A booking is active unless it was cancelled. */
    public boolean isActive() { return !"CANCELLED".equals(status); }

    @Override
    public String[] csvHeader() {
        return new String[]{"Booking ID", "Passenger", "Flight", "Route", "Seat", "Class", "Fare", "Refund", "Payment", "Booked At", "Status"};
    }

    @Override
    public String[] csvRow() {
        return new String[]{String.valueOf(id), passengerName, flightNo, route, seatNo, travelClass,
                String.format("%.2f", fare), String.format("%.2f", refundAmount), paymentMethod == null ? "" : paymentMethod, bookingTime, status};
    }
}
