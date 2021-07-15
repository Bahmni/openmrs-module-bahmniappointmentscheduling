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
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentPractitionerTranslatorImplTest {
    private static String PROVIDER_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    @Mock
    private FhirPractitionerDao practitionerDao;

    private AppointmentPractitionerTranslatorImpl  appointmentPractitionerTranslator;

    @Before
    public void setup() {
        appointmentPractitionerTranslator = new AppointmentPractitionerTranslatorImpl();
        appointmentPractitionerTranslator.setPractitionerDao(practitionerDao);
    }

    @Test
    public void toFhirResourceShouldReturnNullIfPractitionerIsNull() {
        Appointment.AppointmentParticipantComponent participantComponent = appointmentPractitionerTranslator.toFhirResource(null);
        assertThat(participantComponent, nullValue());
    }

    @Test
    public void toFhirResourceShouldSetRoleToPractioner() {
        Provider provider = new Provider();
        Appointment.AppointmentParticipantComponent participantComponent = appointmentPractitionerTranslator.toFhirResource(provider);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getSystem(), equalTo(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE));
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getCode(), equalTo("Practitioner"));
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getDisplay(), equalTo("Practitioner"));
    }

    @Test
    public void toFhirResourceShouldSetParticipantToRequired() {
        Provider provider = new Provider();
        Appointment.AppointmentParticipantComponent participantComponent = appointmentPractitionerTranslator.toFhirResource(provider);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getRequired(), equalTo(Appointment.ParticipantRequired.OPTIONAL));
    }

    @Test
    public void toFhirResourceShouldSetActorToPractitioner() {
        Provider provider = new Provider();
        provider.setUuid(PROVIDER_UUID);
        Appointment.AppointmentParticipantComponent participantComponent = appointmentPractitionerTranslator.toFhirResource(provider);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getActor().getType(), equalTo(FhirConstants.PRACTITIONER));
    }

    @Test
    public void toOpenmrsTypeShouldReturnEmptyProviderObject() {
        Provider provider = new Provider();
        Provider result = appointmentPractitionerTranslator.toOpenmrsType(provider,null);
        assertThat(result, equalTo(provider));
    }

    @Test
    public void toOpenmrsTypeShouldTranslateParticipantActorToProvider() {
        Provider provider = new Provider();
        provider.setUuid(PROVIDER_UUID);
        Appointment.AppointmentParticipantComponent participantComponent = new Appointment.AppointmentParticipantComponent();

        Reference providerReference = new Reference().setReference(FhirConstants.PRACTITIONER + "/" + PROVIDER_UUID)
                .setType(FhirConstants.PRACTITIONER).setIdentifier(new Identifier().setValue(PROVIDER_UUID));
        participantComponent.setActor(providerReference);

        when(practitionerDao.getProviderByUuid(PROVIDER_UUID)).thenReturn(provider);

        Provider result = appointmentPractitionerTranslator.toOpenmrsType(provider, participantComponent);
        assertThat(result, notNullValue());
        assertThat(result.getUuid(), notNullValue());
        assertThat(result.getUuid(), equalTo(PROVIDER_UUID));
    }
}
