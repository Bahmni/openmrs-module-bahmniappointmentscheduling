package org.openmrs.module.fhirappnt.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Provider;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentProviderResponse;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;


@RunWith(MockitoJUnitRunner.class)
public class AppointmentProviderTranslatorImplTest {
    private static String PRACTIONER_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    @Mock
    private FhirPractitionerDao practitionerDao;

    private AppointmentProvider appointmentProvider;

    private AppointmentProviderTranslatorImpl appointmentProviderTranslator;

    @Before
    public void setup() {
        appointmentProviderTranslator = new AppointmentProviderTranslatorImpl();
        appointmentProviderTranslator.setPractitionerDao(practitionerDao);
        initProvider();
    }

    public void initProvider() {
        appointmentProvider = new AppointmentProvider();
        Provider provider = new Provider();
        provider.setUuid(PRACTIONER_UUID);
        appointmentProvider.setResponse(AppointmentProviderResponse.AWAITING);
        appointmentProvider.setProvider(provider);
    }

    @Test
    public void toFhirResourceShouldReturnNullIfPractitionerIsNull() {
        Appointment.AppointmentParticipantComponent participantComponent = appointmentProviderTranslator.toFhirResource(null);
        assertThat(participantComponent, nullValue());
    }

    @Test
    public void toFhirResourceShouldSetRoleToPractitioner() {
        Appointment.AppointmentParticipantComponent participantComponent = appointmentProviderTranslator.toFhirResource(appointmentProvider);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getSystem(), equalTo(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE));
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getCode(), equalTo("AppointmentPractitioner"));
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getDisplay(), equalTo("AppointmentPractitioner"));
    }

    @Test
    public void toFhirResourceShouldSetParticipantToRequired() {
        Appointment.AppointmentParticipantComponent participantComponent = appointmentProviderTranslator.toFhirResource(appointmentProvider);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getRequired(), equalTo(Appointment.ParticipantRequired.OPTIONAL));
    }

    @Test
    public void toFhirResourceShouldSetStatusToAccepted() {
        Appointment.AppointmentParticipantComponent participantComponent = appointmentProviderTranslator.toFhirResource(appointmentProvider);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getStatus(), equalTo(Appointment.ParticipationStatus.NEEDSACTION));
    }

    @Test
    public void toFhirResourceShouldSetActorToProvider() {
        Appointment.AppointmentParticipantComponent participantComponent = appointmentProviderTranslator.toFhirResource(appointmentProvider);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getActor().getType(), equalTo(FhirConstants.PRACTITIONER));
    }

    @Test
    public void toOpenmrsTypeShouldReturnEmptyProviderObject() {
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        AppointmentProvider result = appointmentProviderTranslator.toOpenmrsType(appointmentProvider, null);
        assertThat(result, equalTo(appointmentProvider));
    }

    @Test
    public void toOpenmrsTypeShouldTranslateParticipantActorToProvider() {
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        Provider provider = new Provider();
        provider.setUuid(PRACTIONER_UUID);
        appointmentProvider.setProvider(provider);
        Appointment.AppointmentParticipantComponent participantComponent = new Appointment.AppointmentParticipantComponent();

        Reference providerReference = new Reference().setReference(FhirConstants.PRACTITIONER + "/" + PRACTIONER_UUID)
                .setType(FhirConstants.PRACTITIONER).setIdentifier(new Identifier().setValue(PRACTIONER_UUID));
        participantComponent.setActor(providerReference);
        participantComponent.setStatus(Appointment.ParticipationStatus.ACCEPTED);

        when(practitionerDao.getProviderByUuid(PRACTIONER_UUID)).thenReturn(provider);

        AppointmentProvider result = appointmentProviderTranslator.toOpenmrsType(appointmentProvider, participantComponent);
        assertThat(result, notNullValue());
        assertThat(result.getUuid(), notNullValue());
        assertThat(result.getProvider().getUuid(), equalTo(PRACTIONER_UUID));
    }
}
