package org.openmrs.module.fhirappnt.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhirappnt.api.Impl.FhirBahmniAppointmentServiceImpl;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.fhir2.api.dao.FhirAppointmentDao;
import org.openmrs.module.fhir2.api.translators.AppointmentTranslator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FhirBahmniAppointmentServiceImplTest {

    private static final String APPOINTMENT_UUID = "1085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    @Mock
    private AppointmentTranslator<Appointment> appointmentTranslator;

    @Mock
    private FhirAppointmentDao<Appointment> fhirAppointmentDao;

    private FhirBahmniAppointmentServiceImpl appointmentService;

    private Appointment appointment;

    @Before
    public void setup() {
        appointmentService = new FhirBahmniAppointmentServiceImpl();
        appointmentService.setAppointmentTranslator(appointmentTranslator);
        appointmentService.setFhirAppointmentDao(fhirAppointmentDao);

        appointment = new Appointment();
    }

    @Test
    public void getAppointmentByUuidShouldReturnMatchingAppointment() {
        appointment.setUuid(APPOINTMENT_UUID);

        org.hl7.fhir.r4.model.Appointment fhirAppointment = new org.hl7.fhir.r4.model.Appointment();
        fhirAppointment.setId(APPOINTMENT_UUID);

        when(fhirAppointmentDao.getAppointmentByUuid(APPOINTMENT_UUID)).thenReturn(appointment);
        when(appointmentTranslator.toFhirResource(appointment)).thenReturn(fhirAppointment);

        org.hl7.fhir.r4.model.Appointment result = appointmentService.getAppointmentByUuid(APPOINTMENT_UUID);
        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getId(), equalTo(APPOINTMENT_UUID));
    }
}

