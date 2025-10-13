
package acme.features.customer.bookingRecord;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.BookingRecord;
import acme.entities.passenger.Passenger;
import acme.realms.Customer;

@GuiService
public class CustomerBookingRecordCreateService extends AbstractGuiService<Customer, BookingRecord> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerBookingRecordRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean isCustomer = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);
		super.getResponse().setAuthorised(isCustomer);
	}

	@Override
	public void load() {
		BookingRecord booking = new BookingRecord();
		super.getBuffer().addData(booking);
	}

	@Override
	public void bind(final BookingRecord bookingRecord) {
		super.bindObject(bookingRecord, "booking", "passenger");
	}

	@Override
	public void validate(final BookingRecord bookingRecord) {
		Passenger passenger = bookingRecord.getPassenger();
		Booking booking = bookingRecord.getBooking();

		// Validación: el booking debe estar en modo borrador
		if (booking != null)
			super.state(booking.isDraftMode(), "booking", "acme.validation.booking-record.booking-not-draft");

		//No permitir duplicados
		BookingRecord bookingRecordCompare = null;
		if (passenger != null && booking != null)
			bookingRecordCompare = this.repository.findBookingRecordBybookingIdPassengerId(passenger.getId(), booking.getId());
		boolean status1 = bookingRecordCompare == null || bookingRecordCompare.getId() == bookingRecord.getId();
		super.state(status1, "*", "acme.validation.confirmation.message.booking-record.create");
	}

	@Override
	public void perform(final BookingRecord bookingRecord) {
		this.repository.save(bookingRecord);
	}

	@Override
	public void unbind(final BookingRecord bookingRecord) {
		Integer customerId = super.getRequest().getPrincipal().getActiveRealm().getId();

		Collection<Passenger> passengers = this.repository.getAllPassengersByCustomer(customerId);
		Collection<Booking> bookings = this.repository.findAllDraftBookingsByCustomerId(customerId);
		SelectChoices passengerChoices = SelectChoices.from(passengers, "fullName", bookingRecord.getPassenger());
		SelectChoices bookingChoices = SelectChoices.from(bookings, "locatorCode", bookingRecord.getBooking());
		Dataset dataset = super.unbindObject(bookingRecord, "passenger", "booking");
		dataset.put("passengers", passengerChoices);
		dataset.put("bookings", bookingChoices);

		super.getResponse().addData(dataset);
	}

}
