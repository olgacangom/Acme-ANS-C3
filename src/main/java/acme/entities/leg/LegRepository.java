
package acme.entities.leg;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import acme.client.repositories.AbstractRepository;

public interface LegRepository extends AbstractRepository {

	@Query("select t from Leg t where t.flight.id = :flightId")
	List<Leg> findAllByFlightId(@Param("flightId") Integer flightId);

	@Query("select l.scheduledDeparture from Leg l where l.flight.id = :flightId order by l.scheduledDeparture")
	List<Date> findDepartureByFlightId(@Param("flightId") Integer flightId);
}
