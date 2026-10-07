package airport.interfaces;

import airport.exception.PaymentException;

/** INTERFACE: anything that can be paid for (implemented by Payment). */
public interface Payable {
    boolean pay() throws PaymentException;
    double getTotalCharge();

    default String getReceiptLine() { return String.format("Amount charged: %.2f", getTotalCharge()); }
}
