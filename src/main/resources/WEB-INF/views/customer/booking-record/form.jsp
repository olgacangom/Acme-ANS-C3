<%--
- form.jsp
-
- Copyright (C) 2012-2025 Rafael Corchuelo.
-
- In keeping with the traditional purpose of furthering education and research, it is
- the policy of the copyright owner to permit non-commercial use and redistribution of
- this software. It has been tested carefully, but it is not guaranteed for any particular
- purposes.  The copyright owner does not offer any warranties or representations, nor do
- they accept any liabilities with respect to them.
--%>

<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>

	<jstl:choose>	
		<jstl:when test="${acme:anyOf(_command, 'show|delete')}">
			<acme:input-textbox code="customer.booking-record.form.label.booking" path="bookingLocator" readonly="true"/>
    		<acme:input-textbox code="customer.booking-record.form.label.passenger" path="passengerName" readonly="true"/>
			<acme:submit code="customer.booking-record.form.button.delete" action="/customer/booking-record/delete"/>
			
		</jstl:when>
        	
			<jstl:when test="${_command == 'create'}" >
				<acme:input-select code="customer.booking-record.form.label.booking" path="booking" choices="${bookings}"/>
				<acme:input-select code="customer.booking-record.form.label.passenger" path="passenger" choices="${passengers}"/>
				<acme:submit code="customer.booking-record.form.button.create" action="/customer/booking-record/create"/>
			</jstl:when>
	</jstl:choose>
</acme:form>
