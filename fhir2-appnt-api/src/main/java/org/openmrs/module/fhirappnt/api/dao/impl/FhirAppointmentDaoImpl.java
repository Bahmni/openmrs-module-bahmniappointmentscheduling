package org.openmrs.module.fhirappnt.api.dao.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.fhir2.api.dao.FhirAppointmentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirAppointmentDaoImpl implements FhirAppointmentDao<Appointment> {

    @Autowired
    private AppointmentsService  appointmentsService;

    @Override
    public Appointment getAppointmentByUuid(String uuid) {
        return appointmentsService.getAppointmentByUuid(uuid);
    }
}
