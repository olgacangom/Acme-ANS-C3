
package acme.features.customer.bookingRecord;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.BookingRecord;
import acme.entities.passenger.Passenger;
import acme.realms.Customer;

@GuiService
public class CustomerBookingRecordDeleteService extends AbstractGuiService<Customer, BookingRecord> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerBookingRecordRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean authorised = false;

		if (super.getRequest().hasData("id")) {
			int bookingRecordId = super.getRequest().getData("id", int.class);
			BookingRecord bookingRecord = this.repository.findBookingRecordById(bookingRecordId);

			if (bookingRecord != null) {
				Booking booking = bookingRecord.getBooking();
				int customerAccountId = super.getRequest().getPrincipal().getActiveRealm().getUserAccount().getId();
				boolean isCustomer = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);
				boolean isOwner = booking.getCustomer().getUserAccount().getId() == customerAccountId;
				boolean isDraft = booking.isDraftMode();

				authorised = isCustomer && isOwner && isDraft;
			}
		}

		super.getResponse().setAuthorised(authorised);
	}

	@Override
	public void load() {
		int bookingRecordId = super.getRequest().getData("id", int.class);
		BookingRecord bookingRecord = this.repository.findBookingRecordById(bookingRecordId);
		super.getBuffer().addData(bookingRecord);
	}

	@Override
	public void bind(final BookingRecord bookingRecord) {
		super.bindObject(bookingRecord);
	}

	@Override
	public void validate(final BookingRecord bookingRecord) {

	}

	@Override
	public void perform(final BookingRecord bookingRecord) {
		this.repository.delete(bookingRecord);
	}

	@Override
	public void unbind(final BookingRecord bookingRecord) {
		Dataset dataset;

		Booking boog = this.repository.findOneBookingByBookingRecord(bookingRecord.getId());

		Passenger passenger = bookingRecord.getPassenger();

		dataset = super.unbindObject(bookingRecord);

		dataset.put("bookingLocator", boog.getLocatorCode());
		dataset.put("passengerName", passenger.getFullName());

		dataset.put("draftMode", bookingRecord.getBooking().isDraftMode());
		super.addPayload(dataset, bookingRecord);

		super.getResponse().addData(dataset);
	}
}
