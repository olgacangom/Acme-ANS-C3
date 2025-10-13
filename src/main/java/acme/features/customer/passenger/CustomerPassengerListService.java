
package acme.features.customer.passenger;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.passenger.Passenger;
import acme.realms.Customer;

@GuiService
public class CustomerPassengerListService extends AbstractGuiService<Customer, Passenger> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerPassengerRepository repository;

	// AbstractGuiService  interface -------------------------------------------


	@Override
	public void authorise() {
		boolean isCustomer = false;
		int customerId = super.getRequest().getPrincipal().getActiveRealm().getId();

		if (super.getRequest().getData().containsKey("bookingId")) {
			int bookingId = super.getRequest().getData("bookingId", int.class);
			Booking booking = this.repository.getBookingById(bookingId);

			isCustomer = booking != null && booking.getCustomer().getId() == customerId;
		} else
			isCustomer = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);

		super.getResponse().setAuthorised(isCustomer);
	}

	@Override
	public void load() {
		int customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Collection<Passenger> passengers;

		if (!super.getRequest().getData().containsKey("bookingId"))
			passengers = this.repository.findPassengersByCustomerId(customerId);
		else {
			Integer bookingId = super.getRequest().getData("bookingId", int.class);
			passengers = this.repository.findPassengerByBookingId(bookingId);
		}

		super.getBuffer().addData(passengers);
	}

	@Override
	public void unbind(final Passenger passenger) {
		Boolean containsBookingId;

		Dataset dataset = super.unbindObject(passenger, "fullName", "email", "passportNumber", "birthDate", "draftMode", "specialNeeds");
		containsBookingId = super.getRequest().getData().containsKey("bookingId");
		super.getResponse().addGlobal("containsBookingId", containsBookingId);
		super.getResponse().addData(dataset);
	}

}
