
package acme.features.airlineManager.dashboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.entities.airport.Airport;
import acme.entities.flight.Flight;
import acme.entities.leg.Leg;
import acme.entities.leg.Status;
import acme.forms.AirlineManagerDashboard;
import acme.realms.AirlineManager;

public class AirlineManagerDashboardShowService extends AbstractGuiService<AirlineManager, AirlineManagerDashboard> {

	@Autowired
	private AirlineManagerDashboardRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status;

		status = super.getRequest().getPrincipal().hasRealmOfType(AirlineManager.class);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		AirlineManagerDashboard dashboard;
		int managerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		AirlineManager manager = this.repository.findAirlineManagerById(managerId);
		List<AirlineManager> managers = new ArrayList<>(this.repository.findAirlineManagers());
		List<AirlineManager> managersRanking = managers.stream().sorted(Comparator.comparing(m -> -m.getYearsOfExperience())).collect(Collectors.toList());
		int ranking = managersRanking.indexOf(manager) + 1;
		int yearsToRetire = MomentHelper.getBaseMoment().getYear() - manager.getBirthDate().getYear();
		//ratio
		double onTimeFlights = 0;
		double delayedFlights = 0;
		for (Flight flight : this.repository.findFlightsByAirlineManagerId(managerId))
			for (Leg leg : this.repository.findLegsByFlightId(flight.getId()))
				if (leg.getStatus() == Status.ON_TIME) {
					onTimeFlights++;
					break;
				} else if (leg.getStatus() == Status.DELAYED) {
					delayedFlights++;
					break;
				}
		//popular airport
		List<Flight> flights = new ArrayList<>(this.repository.findFlightsByAirlineManagerId(managerId));
		Map<Airport, Integer> popularAirport = new HashMap<>();
		for (Flight flight : flights) {
			List<Leg> legs = new ArrayList<>(this.repository.findLegsByFlightId(flight.getId())).stream().sorted(Comparator.comparing(leg -> leg.getScheduledDeparture())).toList();
			if (legs.isEmpty())
				continue;
			if (!popularAirport.containsKey(legs.get(0).getDepartureAirport()))
				popularAirport.put(legs.get(0).getDepartureAirport(), 1);
			else
				popularAirport.put(legs.get(0).getDepartureAirport(), popularAirport.get(legs.get(0).getDepartureAirport()) + 1);

			if (!popularAirport.containsKey(legs.get(legs.size() - 1).getArrivalAirport()))
				popularAirport.put(legs.get(legs.size() - 1).getArrivalAirport(), 1);
			else
				popularAirport.put(legs.get(legs.size() - 1).getArrivalAirport(), popularAirport.get(legs.get(legs.size() - 1).getArrivalAirport()) + 1);
		}
		Airport mostPopularAirport = popularAirport.keySet().stream().sorted(Comparator.comparing(airport -> popularAirport.get(airport))).findFirst().get();
		Airport lessPopularAirport = popularAirport.keySet().stream().sorted(Comparator.comparing(airport -> -popularAirport.get(airport))).findFirst().get();

		dashboard = new AirlineManagerDashboard();
		dashboard.setRanking(ranking);
		dashboard.setYearsToRetire(yearsToRetire);
		dashboard.setRatioOfOntimeAndDelayedFlights(onTimeFlights / delayedFlights);
		dashboard.setMostPopularAirport(mostPopularAirport);
		dashboard.setLessPopularAirport(lessPopularAirport);
		super.getBuffer().addData(dashboard);
	}

	@Override
	public void unbind(final AirlineManagerDashboard object) {
		assert object != null;

		Dataset dataset;

		dataset = super.unbindObject(object, "ranking", "yearsToRetire", "ratioOfOntimeAndDelayedFlights", "numberofLegsByStatus", "averageFlightCost", "deviationFlightCost", "maximumFlightCost", "minimumFlightCost");

		dataset.put("mostPopularAirport", object.getMostPopularAirport().getIataCode());
		dataset.put("lessPopularAirport", object.getLessPopularAirport().getIataCode());

		super.getResponse().addData(dataset);
	}
}
