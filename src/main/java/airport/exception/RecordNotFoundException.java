package airport.exception;

/** Thrown when a record with the given id does not exist. */
public class RecordNotFoundException extends AirportException {
    public RecordNotFoundException(String entity, int id) { super(entity + " #" + id + " was not found."); }
    public RecordNotFoundException(String message) { super(message); }
}
