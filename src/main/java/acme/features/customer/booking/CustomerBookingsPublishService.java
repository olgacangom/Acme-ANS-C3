
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
import acme.entities.booking.BookingRecord;
import acme.entities.booking.FlightRepository;
import acme.entities.booking.TravelClass;
import acme.entities.flight.Flight;
import acme.entities.passenger.Passenger;
import acme.realms.Customer;

@GuiService
public class CustomerBookingsPublishService extends AbstractGuiService<Customer, Booking> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerBookingsRepository	repository;

	@Autowired
	private FlightRepository			flightRepository;

	//	@Autowired
	//	private LegRepository				legRepository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {

		int id = super.getRequest().getData("id", Integer.class);
		Booking booking = this.repository.findBookingById(id);

		boolean authorised = false;

		if (booking != null && booking.isDraftMode()) {
			int principalId = super.getRequest().getPrincipal().getActiveRealm().getId();
			boolean isOwner = booking.getCustomer().getId() == principalId;

			Object flightData = super.getRequest().getData().get("flight");
			if (flightData instanceof String flightKey) {
				flightKey = flightKey.trim();

				if (flightKey.equals("0"))
					authorised = isOwner;
				else if (flightKey.matches("\\d+")) {
					int flightId = Integer.parseInt(flightKey);
					Flight flight = this.flightRepository.findFlightById(flightId);
					boolean flightValid = flight != null && !flight.getDraftMode() && this.flightRepository.findAllFlights().contains(flight);
					authorised = isOwner && flightValid;
				}
			}

		}

		super.getResponse().setAuthorised(authorised);
	}

	@Override
	public void load() {
		Booking booking;
		int bookingId = super.getRequest().getData("id", int.class);

		booking = this.repository.findBookingById(bookingId);

		super.getBuffer().addData(booking);
	}

	@Override
	public void bind(final Booking booking) {
		int flightId;
		Flight flight;

		flightId = super.getRequest().getData("flight", int.class);
		flight = this.flightRepository.findFlightById(flightId);

		super.bindObject(booking, "locatorCode", "lastNibble", "travelClass");
		booking.setFlight(flight);
	}

	@Override
	public void validate(final Booking booking) {
		if (booking.getLastNibble().isBlank())
			super.state(false, "lastNibble", "acme.validation.confirmation.message.lastNibble");

		Collection<BookingRecord> br = this.repository.findBookingRecordByBookingId(booking.getId());
		if (br.isEmpty())
			super.state(false, "passengers", "acme.validation.confirmation.message.passenger");

		// Validar que el código de localizador sea único
		//		Booking b = this.repository.findBookingByLocatorCode(booking.getLocatorCode());
		//		if (b != null && b.getId() != booking.getId())
		//			super.state(false, "locatorCode", "acme.validation.confirmation.message.booking.locator-code");

		// Validar que exista al menos un pasajero asociado
		// Todos los pasajeros deben estar publicados
		boolean passengerNotPublished = false;
		Collection<Passenger> passengerPublished = this.repository.findPassengersByBookingId(booking.getId());
		for (Passenger p : passengerPublished)
			if (p.isDraftMode() == true)
				passengerNotPublished = true;
		if (passengerNotPublished)
			super.state(false, "passengers", "acme.validation.confirmation.message.passenger.notPublished");
	}

	@Override
	public void perform(final Booking booking) {
		booking.setDraftMode(false);
		booking.setPurchaseMoment(MomentHelper.getCurrentMoment());
		this.repository.save(booking);
	}

	@Override
	public void unbind(final Booking booking) {
		Dataset dataset;
		SelectChoices choices;
		SelectChoices flightChoices;

		Date today = MomentHelper.getCurrentMoment();
		Collection<Flight> flightsInFuture = this.repository.findAllPublishedFlightsWithFutureDeparture(today);
		flightChoices = SelectChoices.from(flightsInFuture, "tag", booking.getFlight());
		choices = SelectChoices.from(TravelClass.class, booking.getTravelClass());
		Collection<String> passengers = this.repository.findPassengersNameByBooking(booking.getId());

		dataset = super.unbindObject(booking, "locatorCode", "purchaseMoment", "price", "draftMode", "lastNibble");
		dataset.put("travelClass", choices);
		dataset.put("passengers", passengers);
		dataset.put("flight", flightChoices.getSelected().getKey());
		dataset.put("flights", flightChoices);

		super.getResponse().addData(dataset);
	}

}
