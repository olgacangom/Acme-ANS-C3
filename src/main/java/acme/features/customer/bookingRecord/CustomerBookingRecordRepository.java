
package acme.features.customer.bookingRecord;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.booking.Booking;
import acme.entities.booking.BookingRecord;
import acme.entities.passenger.Passenger;

@Repository
public interface CustomerBookingRecordRepository extends AbstractRepository {

	@Query("select br from BookingRecord br where br.id = :bookingRecordId")
	BookingRecord findBookingRecordById(@Param("bookingRecordId") Integer bookingRecordId);

	@Query("select br from BookingRecord br where br.booking.id = :bookingId")
	Collection<BookingRecord> findBookingRecordByBookingId(@Param("bookingId") Integer bookingId);

	@Query("select br from BookingRecord br where br.booking.id = :bookingId AND br.passenger.id = :passengerId")
	BookingRecord findBookingRecordBybookingIdPassengerId(@Param("bookingId") Integer bookingId, @Param("passengerId") Integer passengerId);

	@Query("select br from BookingRecord br where br.passenger.id = :passengerId")
	Collection<BookingRecord> findBookingRecordByPassengerId(@Param("passengerId") Integer passengerId);

	@Query("select b from Booking b where b.customer.id = :customerId and b.draftMode = true")
	Collection<Booking> findAllDraftBookingsByCustomerId(@Param("customerId") int customerId);

	@Query("select p from Passenger p where p.customer.userAccount.id = :customerId and p.draftMode = false")
	Collection<Passenger> findAllPublishedPassengersByCustomerId(@Param("customerId") int customerId);

	@Query("SELECT p FROM Passenger p WHERE p.customer.id=:customerId")
	Collection<Passenger> getAllPassengersByCustomer(int customerId);

	@Query("SELECT b FROM Booking b WHERE b.customer.id=:customerId and b.draftMode=false")
	Collection<Booking> getBookingsByCustomerId(int customerId);

	@Query("SELECT br FROM BookingRecord br WHERE br.booking.customer.id=:customerId")
	Collection<BookingRecord> getBookingRecordsByCustomerId(int customerId);
}
