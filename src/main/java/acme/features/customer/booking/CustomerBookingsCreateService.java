
package acme.features.customer.booking;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.TravelClass;
import acme.entities.flight.Flight;
import acme.entities.passenger.Passenger;
import acme.realms.Customer;

@GuiService
public class CustomerBookingsCreateService extends AbstractGuiService<Customer, Booking> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerBookingsRepository repository;

	//	@Autowired
	//	private LegRepository				legRepository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void authorise() {
		boolean status;
		status = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Booking booking;
		int customerId;
		Customer customer;
		Date today = MomentHelper.getCurrentMoment();

		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		customer = this.repository.findCustomerById(customerId);

		booking = new Booking();
		booking.setCustomer(customer);
		booking.setPurchaseMoment(today);
		booking.setDraftMode(true);

		super.getBuffer().addData(booking);
	}

	@Override
	public void bind(final Booking booking) {
		//		Flight flight;
		//		int flightId;
		//
		//		flightId = super.getRequest().getData("flight", int.class);
		//		flight = this.flightRepository.findFlightById(flightId);

		super.bindObject(booking, "flight", "locatorCode", "purchaseMoment", "price", "travelClass", "lastNibble");
		//		booking.setFlight(flight);

	}

	@Override
	public void validate(final Booking booking) {
		Collection<Booking> bookings = this.repository.findAllBookingByLocatorCode(booking.getLocatorCode());
		boolean status = bookings.isEmpty();
		super.state(status, "locatorCode", "acme.validation.confirmation.message.booking.locator-code");
	}

	@Override
	public void perform(final Booking booking) {
		//		Date today = MomentHelper.getCurrentMoment();
		//		booking.setPurchaseMoment(today);
		this.repository.save(booking);
	}

	@Override
	public void unbind(final Booking booking) {
		Dataset dataset;
		SelectChoices choices;
		SelectChoices flightChoices;

		Date today = MomentHelper.getCurrentMoment();

		// Usamos directamente el método del repositorio que ya filtra vuelos publicados con al menos un Leg futuro
		Collection<Flight> flightsInFuture = this.repository.findAllPublishedFlightsWithFutureDeparture(today);

		flightChoices = SelectChoices.from(flightsInFuture, "tag", booking.getFlight());
		choices = SelectChoices.from(TravelClass.class, booking.getTravelClass());

		Collection<Passenger> passengerN = this.repository.findPassengersByBookingId(booking.getId());
		Collection<String> passengers = passengerN.stream().map(p -> p.getFullName()).toList();

		dataset = super.unbindObject(booking, "locatorCode", "purchaseMoment", "price", "draftMode", "lastNibble");
		dataset.put("travelClass", choices);
		dataset.put("passengers", passengers);
		dataset.put("flight", flightChoices.getSelected().getKey());
		dataset.put("flights", flightChoices);

		super.getResponse().addData(dataset);
	}

}
