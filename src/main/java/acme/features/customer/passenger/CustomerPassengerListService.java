
package acme.features.customer.passenger;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
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
		//		int customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		//		Collection<Passenger> passengers = this.repository.findPassengersByCustomerId(customerId);
		//		boolean status = passengers.stream().allMatch(b -> b.getCustomer().getId() == customerId) && super.getRequest().getPrincipal().hasRealmOfType(Customer.class);
		//		super.getResponse().setAuthorised(status);
		boolean isCustomer = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);
		super.getResponse().setAuthorised(isCustomer);
	}

	@Override
	public void load() {
		int customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Collection<Passenger> passengers = this.repository.findPassengersByCustomerId(customerId);
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
