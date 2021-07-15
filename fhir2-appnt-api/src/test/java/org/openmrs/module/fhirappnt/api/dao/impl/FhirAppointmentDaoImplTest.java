package org.openmrs.module.fhirappnt.api.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.when;

import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FhirAppointmentDaoImplTest {

    private static final String APPOINTMENT_UUID = "75504r42-3ca8-11e3-bf2b-0800271c1b77";

    private static final String WRONG_APPOINTMENT_UUID = "1099AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    @Mock
    private AppointmentsService appointmentsService;

    private FhirAppointmentDaoImpl fhirAppointmentDao;

    private Appointment appointment;

    @Before
    public void setup() {
        fhirAppointmentDao = new FhirAppointmentDaoImpl();
        fhirAppointmentDao.setAppointmentsService(appointmentsService);

        appointment = new Appointment();
    }

    @Test
    public void shouldGetAppointmentByUuid() {
        appointment.setUuid(APPOINTMENT_UUID);
        when(appointmentsService.getAppointmentByUuid(APPOINTMENT_UUID)).thenReturn(appointment);
        Appointment appointment = fhirAppointmentDao.getAppointmentByUuid(APPOINTMENT_UUID);
        assertThat(appointment, notNullValue());
        assertThat(appointment.getUuid(), notNullValue());
        assertThat(appointment.getUuid(), equalTo(APPOINTMENT_UUID));
    }

    @Test
    public void shouldReturnNullWhenGetAppointmentIsCalledWithUnknownUuid() {
        Appointment appointment = fhirAppointmentDao.getAppointmentByUuid(WRONG_APPOINTMENT_UUID);
        assertThat(appointment, nullValue());
    }
}
