package airport.model;

import airport.exception.PaymentException;

public class CardPayment extends Payment {
    private final String cardHolder;
    private final String cardNumber;
    private final String cvv;

    public CardPayment(double amount, String cardHolder, String cardNumber, String cvv) {
        super(amount);
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber == null ? "" : cardNumber.replaceAll("\\s", "");
        this.cvv = cvv;
    }

    @Override public String getMethod() { return "CARD"; }
    @Override public double getProcessingFee() { return getAmount() * 0.02; }

    @Override
    public void validate() throws PaymentException {
        if (cardHolder == null || cardHolder.isBlank()) throw new PaymentException("Card holder name is required.");
        if (!cardNumber.matches("\\d{16}")) throw new PaymentException("Card number must be 16 digits.");
        if (cvv == null || !cvv.matches("\\d{3}")) throw new PaymentException("CVV must be 3 digits.");
    }

    public String getCardHolder() { return cardHolder; }
    public String getLast4() { return cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : cardNumber; }
}
