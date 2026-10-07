package airport.model;

import airport.exception.PaymentException;

public class UPIPayment extends Payment {
    private final String upiId;

    public UPIPayment(double amount, String upiId) {
        super(amount);
        this.upiId = upiId == null ? "" : upiId.trim();
    }

    @Override public String getMethod() { return "UPI"; }

    @Override
    public void validate() throws PaymentException {
        if (!upiId.matches("[\\w.\\-]{2,}@[a-zA-Z]{2,}"))
            throw new PaymentException("Invalid UPI ID (example: name@okbank).");
    }

    public String getUpiId() { return upiId; }
}
