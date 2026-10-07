package airport.interfaces;

import airport.exception.AirportException;

import java.util.List;
import java.util.Optional;

/** GENERIC INTERFACE: the standard create / read / update / delete contract for DAO classes. */
public interface CrudDAO<T> {
    void add(T item) throws AirportException;
    void update(T item) throws AirportException;
    void delete(int id) throws AirportException;
    List<T> getAll() throws AirportException;
    /** Optional avoids returning null when nothing is found. */
    Optional<T> findById(int id) throws AirportException;
}
