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
import org.openmrs.Patient;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentPatientTranslatorImplTest {
    private static String PATIENT_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    @Mock
    private PatientReferenceTranslator patientReferenceTranslator;

    private AppointmentPatientTranslatorImpl appointmentPatientTranslator;

    @Before
    public void setup() {
        appointmentPatientTranslator = new AppointmentPatientTranslatorImpl();
        appointmentPatientTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
    }

    @Test
    public void toFhirResourceShouldReturnNullIfPatientIsNull() {
        Appointment.AppointmentParticipantComponent participantComponent = appointmentPatientTranslator.toFhirResource(null);
        assertThat(participantComponent, nullValue());
    }

    @Test
    public void toFhirResourceShouldSetRoleToPatient() {
        Patient patient = new Patient();
        Appointment.AppointmentParticipantComponent participantComponent = appointmentPatientTranslator.toFhirResource(patient);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getSystem(), equalTo(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE));
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getCode(), equalTo("Patient"));
        assertThat(participantComponent.getType().get(0).getCoding().get(0).getDisplay(), equalTo("Patient"));
    }

    @Test
    public void toFhirResourceShouldSetParticipantToRequired() {
        Patient patient = new Patient();
        Appointment.AppointmentParticipantComponent participantComponent = appointmentPatientTranslator.toFhirResource(patient);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getRequired(), equalTo(Appointment.ParticipantRequired.REQUIRED));
    }

    @Test
    public void toFhirResourceShouldSetStatusToAccepted() {
        Patient patient = new Patient();
        Appointment.AppointmentParticipantComponent participantComponent = appointmentPatientTranslator.toFhirResource(patient);
        assertThat(participantComponent, notNullValue());
        assertThat(participantComponent.getStatus(), equalTo(Appointment.ParticipationStatus.ACCEPTED));
    }

    @Test
    public void toFhirResourceShouldSetActorToPatientPassed() {
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);

        Reference patientReference = new Reference().setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID)
                .setType(FhirConstants.PATIENT).setIdentifier(new Identifier().setValue(PATIENT_UUID));

        when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
        Appointment.AppointmentParticipantComponent result = appointmentPatientTranslator.toFhirResource(patient);
        assertThat(result, notNullValue());
        assertThat(result.getActor().getType(), equalTo(FhirConstants.PATIENT));
        assertThat(result.getActor().getReference().contains(PATIENT_UUID), equalTo(true));
    }

    @Test
    public void toOpenmrsTypeShouldReturnAnEmptyParticipantObject() {
        Patient patient = appointmentPatientTranslator.toOpenmrsType(null);
        assertThat(patient, notNullValue());
    }

    @Test
    public void toOpenmrsTypeShouldTranslateParticipantActorToPatient() {
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);
        Appointment.AppointmentParticipantComponent participantComponent = new Appointment.AppointmentParticipantComponent();

        Reference patientReference = new Reference().setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID)
                .setType(FhirConstants.PATIENT).setIdentifier(new Identifier().setValue(PATIENT_UUID));
        participantComponent.setActor(patientReference);

        when(patientReferenceTranslator.toOpenmrsType(patientReference)).thenReturn(patient);

        Patient result = appointmentPatientTranslator.toOpenmrsType(patient, participantComponent);
        assertThat(result, notNullValue());
        assertThat(result.getUuid(), notNullValue());
        assertThat(result.getUuid(), equalTo(PATIENT_UUID));
    }
}
