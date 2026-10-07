package airport.model;

import airport.exception.PaymentException;

/** INTERFACE: anything that can be paid for (implemented by Payment). */
public interface Payable {
    boolean pay() throws PaymentException;
    double getTotalCharge();
}
