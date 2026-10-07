package airport.model;

import airport.exception.PaymentException;

public class CashPayment extends Payment {
    private final double tendered;

    public CashPayment(double amount, double tendered) {
        super(amount);
        this.tendered = tendered;
    }

    @Override public String getMethod() { return "CASH"; }

    @Override
    public void validate() throws PaymentException {
        if (tendered < getTotalCharge())
            throw new PaymentException(String.format("Cash received (%.2f) is less than amount due (%.2f).", tendered, getTotalCharge()));
    }

    public double getTendered() { return tendered; }
    public double getChange() { return tendered - getTotalCharge(); }
}
