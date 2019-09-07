package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AbstractAppointmentRecurringPatternMapper;
import org.openmrs.module.appointments.web.mapper.RecurringAppointmentMapper;
import org.openmrs.module.appointments.web.mapper.RecurringPatternMapper;
import org.openmrs.module.appointments.web.service.impl.RecurringAppointmentsService;
import org.openmrs.module.appointments.web.validators.Validator;
import org.openmrs.module.appointments.web.validators.impl.RecurringPatternValidator;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class RecurringAppointmentsControllerTest {

    @InjectMocks
    private RecurringAppointmentsController recurringAppointmentsController;

    @Mock
    private AppointmentRecurringPatternService appointmentRecurringPatternService;

    @Mock
    private RecurringAppointmentsService recurringAppointmentsService;

    @Mock
    private RecurringPatternValidator recurringPatternValidator;

    @Mock
    private RecurringPatternMapper recurringPatternMapper;

    @Mock
    private RecurringAppointmentMapper recurringAppointmentMapper;

    @Mock
    @Qualifier("singleAppointmentRecurringPatternMapper")
    private AbstractAppointmentRecurringPatternMapper singleAppointmentRecurringPatternMapper;

    @Mock
    @Qualifier("allAppointmentRecurringPatternMapper")
    private AbstractAppointmentRecurringPatternMapper allAppointmentRecurringPatternMapper;

    @Mock
    Validator<RecurringAppointmentRequest> appointmentRequestEditValidator;

    @Mock
    private AppointmentsService appointmentsService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldSaveAnAppointmentWhenRecurringPatternAlsoPresentsInRequest() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(new Date());
        recurringAppointmentRequest.getAppointmentRequest().setUuid("someUuid");
        recurringAppointmentRequest.getAppointmentRequest().setServiceUuid("someServiceUuid");
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Appointment appointmentOne = new Appointment();
        appointmentOne.setUuid("appointmentUuid");
        Appointment appointmentTwo = new Appointment();
        appointmentTwo.setUuid("appointmentUuid");
        List<Appointment> appointments = Arrays.asList(appointmentOne, appointmentTwo);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setAppointments(new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo)));
        when(recurringPatternMapper.fromRequest(recurringPattern)).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPatternService.validateAndSave(appointmentRecurringPattern)).thenReturn(appointments);
        when(recurringAppointmentsService.generateRecurringAppointments(recurringAppointmentRequest)).thenReturn(new ArrayList<>());
        when(recurringAppointmentMapper.constructResponse(Arrays.asList(appointmentOne, appointmentTwo))).thenReturn(Collections.emptyList());
        doNothing().when(recurringPatternValidator).validate(recurringPattern, mock(Errors.class));
        appointmentRecurringPattern.setAppointments(Collections.emptySet());

        ResponseEntity<Object> responseEntity = recurringAppointmentsController.save(recurringAppointmentRequest);

        verify(recurringPatternMapper, times(1)).fromRequest(recurringPattern);
        verify(appointmentRecurringPatternService, times(1)).validateAndSave(appointmentRecurringPattern);
        verify(recurringAppointmentsService, times(1)).generateRecurringAppointments(recurringAppointmentRequest);
        verify(recurringAppointmentMapper, times(1)).constructResponse(Collections.emptyList());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    // TODO : Write a test when there are errors

    @Test
    public void shouldCallUpdateOfRecurringAppointmentServiceWhenApplyForAllIsTrue() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        Appointment appointmentMock = mock(Appointment.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        when((recurringAppointmentRequest.getAppointmentRequest())).thenReturn(appointmentRequest);
        when(appointmentRequestEditValidator.validate(recurringAppointmentRequest)).thenReturn(true);
        when(recurringAppointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(true);
        when(recurringAppointmentRequest.getTimeZone()).thenReturn("UTC");
        when(allAppointmentRecurringPatternMapper.fromRequest(recurringAppointmentRequest)).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointmentMock)));
        when(appointmentRecurringPatternService.update(any())).thenReturn(mock(AppointmentRecurringPattern.class));

        recurringAppointmentsController.editAppointment(recurringAppointmentRequest);

        verify(allAppointmentRecurringPatternMapper).fromRequest(recurringAppointmentRequest);
        verify(appointmentRecurringPatternService).update(any());
    }

    @Test
    public void shouldUpdateSingleRecurringAppointmentByCreatingANewAppointmentReferencingTheExisitngAppointment() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);

        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.isRecurringAppointment()).thenReturn(true);
        when(recurringAppointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(false);

        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        Appointment appointment = mock(Appointment.class);

        when(appointmentRequestEditValidator.validate(recurringAppointmentRequest)).thenReturn(true);
        when(singleAppointmentRecurringPatternMapper.fromRequest(recurringAppointmentRequest)).thenReturn(appointmentRecurringPattern);
        final AppointmentRecurringPattern updatedRecurringAppointmentPattern = mock(AppointmentRecurringPattern.class);
        when(appointmentRecurringPatternService.update(any())).thenReturn(updatedRecurringAppointmentPattern);
        when(updatedRecurringAppointmentPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment)));
        when(appointment.getUuid()).thenReturn("uuid");
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn("uuid");

        final ResponseEntity<Object> responseEntity = recurringAppointmentsController.editAppointment(recurringAppointmentRequest);

        verify(singleAppointmentRecurringPatternMapper, times(1)).fromRequest(recurringAppointmentRequest);
        verify(appointmentRecurringPatternService, times(1)).update(appointmentRecurringPattern);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    }

    @Test
    public void shouldUpdateSingleRecurringAppointmentAndReturnTheNewAppointmentWhenAppointmentInRequestDoesNotHaveARelatedAppointment() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);

        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.isRecurringAppointment()).thenReturn(true);
        when(recurringAppointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(false);

        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        Appointment appointment = mock(Appointment.class);
        Appointment newAppointment = mock(Appointment.class);

        when(singleAppointmentRecurringPatternMapper.fromRequest(recurringAppointmentRequest)).thenReturn(appointmentRecurringPattern);
        final AppointmentRecurringPattern updatedRecurringAppointmentPattern = mock(AppointmentRecurringPattern.class);
        when(appointmentRecurringPatternService.update(any())).thenReturn(updatedRecurringAppointmentPattern);
        when(updatedRecurringAppointmentPattern.getActiveAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment, newAppointment)));
        when(appointment.getUuid()).thenReturn("uuid");
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn("uuid");
        when(newAppointment.getRelatedAppointment()).thenReturn(appointment);
        when(newAppointment.getUuid()).thenReturn("newUuid");

        when(appointment.getVoided()).thenReturn(true);
        when(appointmentRequestEditValidator.validate(recurringAppointmentRequest)).thenReturn(true);

        recurringAppointmentsController.editAppointment(recurringAppointmentRequest);

        verify(recurringAppointmentMapper, times(1)).constructResponse(newAppointment);

    }

    @Test
    public void shouldUpdateRecurringAppointmentAndReturnTheSameAppointmentWhenThisAppointmentHasRelatedAppointment() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);

        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.isRecurringAppointment()).thenReturn(true);
        when(recurringAppointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(false);

        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        Appointment appointment = mock(Appointment.class);
        Appointment oldRecurringAppointment = mock(Appointment.class);

        when(singleAppointmentRecurringPatternMapper.fromRequest(recurringAppointmentRequest)).thenReturn(appointmentRecurringPattern);
        final AppointmentRecurringPattern updatedRecurringAppointmentPattern = mock(AppointmentRecurringPattern.class);
        when(appointmentRecurringPatternService.update(any())).thenReturn(updatedRecurringAppointmentPattern);
        when(updatedRecurringAppointmentPattern.getActiveAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment, oldRecurringAppointment)));
        when(appointment.getUuid()).thenReturn("uuid");
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn("uuid");
        when(appointment.getRelatedAppointment()).thenReturn(oldRecurringAppointment);
        when(oldRecurringAppointment.getUuid()).thenReturn("oldUuid");
        when(appointment.getVoided()).thenReturn(false);
        when(appointmentRequestEditValidator.validate(recurringAppointmentRequest)).thenReturn(true);


        recurringAppointmentsController.editAppointment(recurringAppointmentRequest);

        verify(recurringAppointmentMapper, times(1)).constructResponse(appointment);
    }

    @Test
    public void shouldThrowExceptionWhenAppointmentRequestValidationFailed() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        when(recurringAppointmentRequest.isRecurringAppointment()).thenReturn(true);
        when(recurringAppointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(false);
        when(appointmentRequestEditValidator.validate(recurringAppointmentRequest)).thenReturn(false);
        final String error = "error";
        when(appointmentRequestEditValidator.getError()).thenReturn(error);

        final ResponseEntity<Object> responseEntity = recurringAppointmentsController.editAppointment(recurringAppointmentRequest);

        verify(appointmentRequestEditValidator, times(1)).getError();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        final String message = (String) ((LinkedHashMap) ((SimpleObject) responseEntity.getBody()).get("error")).get("message");
        assertEquals(error, message);
    }

    @Test
    public void shouldGetAppointmentByUuid() {
        String appointmentUuid = "appointment";
        Appointment appointment = new Appointment();
        appointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(appointment);

        recurringAppointmentsController.getAppointmentByUuid(appointmentUuid);

        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        verify(recurringAppointmentMapper, times(1)).constructResponse(appointment);
    }

    @Test
    public void shouldThrowExceptionIfAppointmentDoesNotExist() {
        when(appointmentsService.getAppointmentByUuid(any(String.class))).thenReturn(null);
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment does not exist");

        recurringAppointmentsController.getAppointmentByUuid("randomUuid");
    }
}