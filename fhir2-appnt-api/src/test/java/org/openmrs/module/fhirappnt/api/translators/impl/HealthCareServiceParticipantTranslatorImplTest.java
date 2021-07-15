package org.openmrs.module.fhirappnt.api.translators.impl;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HealthCareServiceParticipantTranslatorImplTest {

    private static String SERVICE_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    @Mock
    AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    private HealthCareServiceParticipantTranslatorImpl healthCareServiceParticipantTranslator;

    @Before
    public void setup() {
        healthCareServiceParticipantTranslator = new HealthCareServiceParticipantTranslatorImpl();
        healthCareServiceParticipantTranslator.setAppointmentServiceDefinitionService(appointmentServiceDefinitionService);
    }

    @Test
    public void toFhirResourceShouldReturnNullIfHealthCareServiceIsNull() {
        Appointment.AppointmentParticipantComponent participantComponent = healthCareServiceParticipantTranslator.toFhirResource(null);
        assertThat(participantComponent, nullValue());
    }

    @Test
    public void toFhirResourceShouldSetRoleToHealthCareService() {
        AppointmentServiceDefinition  appointmentServiceDefinition = new AppointmentServiceDefinition();
        Appointment.AppointmentParticipantComponent participantComponent = healthCareServiceParticipantTranslator.toFhirResource(appointmentServiceDefinition);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getSystem(), equalTo(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE));
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getCode(), equalTo("HealthCareService"));
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getDisplay(), equalTo("HealthCareService"));
    }

    @Test
    public void toFhirResourceShouldSetParticipantToRequired() {
        AppointmentServiceDefinition  appointmentServiceDefinition = new AppointmentServiceDefinition();
        Appointment.AppointmentParticipantComponent participantComponent = healthCareServiceParticipantTranslator.toFhirResource(appointmentServiceDefinition);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getRequired(), equalTo(Appointment.ParticipantRequired.OPTIONAL));
    }


    @Test
    public void toFhirResourceShouldSetActorToHealthCareService() {
        AppointmentServiceDefinition  serviceDefinition = new AppointmentServiceDefinition();
        serviceDefinition.setUuid(SERVICE_UUID);

        Reference serviceReference = new Reference().setReference("HealthCareService" + "/" + SERVICE_UUID)
                .setType("HealthCareService").setIdentifier(new Identifier().setValue(SERVICE_UUID));

        Appointment.AppointmentParticipantComponent result = healthCareServiceParticipantTranslator.toFhirResource(serviceDefinition);
        assertThat(result, notNullValue());
        assertThat(result.getActor().getType(), equalTo("HealthCareService"));
        assertThat(result.getActor().getReference().contains(SERVICE_UUID), equalTo(true));
    }

    @Test
    public void toOpenmrsTypeShouldReturnAnEmptyParticipantObject() {
        AppointmentServiceDefinition serviceDefinition = healthCareServiceParticipantTranslator.toOpenmrsType(null);
        assertThat(serviceDefinition, nullValue());
    }

    @Test
    public void toOpenmrsTypeShouldTranslateParticipantActorToHealthCareService() {
        AppointmentServiceDefinition  serviceDefinition = new AppointmentServiceDefinition();
        serviceDefinition.setUuid(SERVICE_UUID);
        Appointment.AppointmentParticipantComponent participantComponent = new Appointment.AppointmentParticipantComponent();

        Reference serviceReference = new Reference().setReference("HealthCareService" + "/" + SERVICE_UUID)
                .setType("HealthCareService").setIdentifier(new Identifier().setValue(SERVICE_UUID));

        participantComponent.setActor(serviceReference);

        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid(SERVICE_UUID)).thenReturn(serviceDefinition);

        AppointmentServiceDefinition result = healthCareServiceParticipantTranslator.toOpenmrsType(new AppointmentServiceDefinition(), participantComponent);
        assertThat(result, notNullValue());
        assertThat(result.getUuid(), notNullValue());
        assertThat(result.getUuid(), equalTo(SERVICE_UUID));
    }
}
