package airport.model;

import airport.exception.PaymentException;
import airport.interfaces.Payable;

/** ABSTRACTION: abstract Payment implements Payable. */
public abstract class Payment implements Payable {
    private double amount;   // fare (before processing fee)

    protected Payment(double amount) { this.amount = amount; }

    public abstract String getMethod();
    /** Each payment type validates its own details. */
    public abstract void validate() throws PaymentException;
    /** Overridden by CardPayment (2% fee). */
    public double getProcessingFee() { return 0; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    @Override
    public double getTotalCharge() {
        return Math.round((amount + getProcessingFee()) * 100.0) / 100.0;
    }

    @Override
    public boolean pay() throws PaymentException {
        validate();
        return true;
    }
}
