
package acme.features.administrator.airport;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Administrator;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.airport.Airport;
import acme.entities.airport.OperationalScope;

@GuiService
public class AdministratorAirportCreateService extends AbstractGuiService<Administrator, Airport> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AdministratorAirportRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean isAdministrator = super.getRequest().getPrincipal().hasRealmOfType(Administrator.class);
		super.getResponse().setAuthorised(isAdministrator);
	}

	@Override
	public void load() {
		Airport airport;

		airport = new Airport();
		airport.setName("");
		airport.setIataCode("");
		airport.setOperationalScope(null);
		airport.setCity("");
		airport.setCountry("");
		airport.setWebsite("");
		airport.setEmail("");
		airport.setPhoneNumber("");

		super.getBuffer().addData(airport);
	}

	@Override
	public void bind(final Airport airport) {
		super.bindObject(airport, "name", "iataCode", "operationalScope", "city", "country", "website", "email", "phoneNumber");
	}

	@Override
	public void validate(final Airport airport) {
		{
			boolean confirmation;
			confirmation = super.getRequest().getData("confirmation", boolean.class);

			super.state(confirmation, "confirmation", "acme.validation.confirmation.message");
		}

	}

	@Override
	public void perform(final Airport airport) {
		this.repository.save(airport);
	}

	@Override
	public void unbind(final Airport airport) {
		SelectChoices choices;
		Dataset dataset;

		choices = SelectChoices.from(OperationalScope.class, airport.getOperationalScope());

		dataset = super.unbindObject(airport, "name", "iataCode", "operationalScope", "city", "country", "website", "email", "phoneNumber");
		dataset.put("operationalScopes", choices);
		dataset.put("confirmation", false);

		super.getResponse().addData(dataset);

	}
}
