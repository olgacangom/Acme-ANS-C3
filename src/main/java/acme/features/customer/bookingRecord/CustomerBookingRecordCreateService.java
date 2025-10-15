
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
import acme.features.customer.booking.CustomerBookingsRepository;
import acme.features.customer.passenger.CustomerPassengerRepository;
import acme.realms.Customer;

@GuiService
public class CustomerBookingRecordCreateService extends AbstractGuiService<Customer, BookingRecord> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerBookingRecordRepository	repository;

	@Autowired
	private CustomerBookingsRepository		bookingRepository;

	@Autowired
	private CustomerPassengerRepository		passengerRepository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean isCustomer = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);
		boolean authorised = isCustomer;
		try {
			if (super.getRequest().hasData("booking")) {
				int bookingId = super.getRequest().getData("booking", int.class);
				int passengerId = super.getRequest().getData("passenger", int.class);
				if (bookingId != 0 || passengerId != 0) {
					Booking booking = this.bookingRepository.findBookingById(bookingId);
					Passenger passenger = this.passengerRepository.findPassengerById(passengerId);
					int customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
					boolean belongsToCustomer = booking != null && booking.getCustomer().getId() == customerId;
					boolean isPassenger = passenger != null && passenger.getCustomer().getId() == customerId;
					boolean isDraft = booking != null && booking.isDraftMode();

					authorised = isCustomer && belongsToCustomer && isDraft && isPassenger;
				}

			}
		} catch (Throwable e) {
			authorised = false;
		} 

		super.getResponse().setAuthorised(authorised);
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
		Booking booking = bookingRecord.getBooking();

		// Validación: el booking debe estar en modo borrador
		if (booking != null)
			super.state(booking.isDraftMode(), "booking", "acme.validation.booking-record.booking-not-draft");

		//No permitir duplicados
		if (bookingRecord.getPassenger() != null) {
			BookingRecord br = this.repository.findBookingRecordBybookingIdPassengerId(bookingRecord.getBooking().getId(), bookingRecord.getPassenger().getId());
			if (br != null)
				super.state(false, "*", "acme.validation.confirmation.message.booking-record.create");
		}
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
