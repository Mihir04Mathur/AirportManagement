package airport.interfaces;

import airport.exception.AirportException;

import java.util.List;

/** INTERFACE: a DAO that can search its records by a keyword. */
public interface Searchable<T> {
    List<T> search(String keyword) throws AirportException;
}
