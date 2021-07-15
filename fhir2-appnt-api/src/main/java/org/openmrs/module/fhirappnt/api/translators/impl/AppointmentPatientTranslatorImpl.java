package org.openmrs.module.fhirappnt.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.Patient;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.impl.AbstractReferenceHandlingTranslator;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;
import org.openmrs.module.fhirappnt.api.translators.AppointmentParticipantTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class AppointmentPatientTranslatorImpl extends AbstractReferenceHandlingTranslator implements AppointmentParticipantTranslator<Patient> {

    @Autowired
    private PatientReferenceTranslator patientReferenceTranslator;

    @Override
    public Appointment.AppointmentParticipantComponent toFhirResource(Patient patient) {
        if (patient == null) {
            return null;
        }
        Appointment.AppointmentParticipantComponent participant = new Appointment.AppointmentParticipantComponent();
        CodeableConcept role = new CodeableConcept();
        role.addCoding(new Coding(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE, "Patient", "Patient"));
        participant.setRequired(Appointment.ParticipantRequired.REQUIRED);
        participant.setActor(patientReferenceTranslator.toFhirResource(patient));
        participant.addType(role);
        participant.setStatus(Appointment.ParticipationStatus.ACCEPTED);

        return participant;
    }

    @Override
    public Patient toOpenmrsType(Appointment.AppointmentParticipantComponent appointmentParticipantComponent) {
        return toOpenmrsType(new Patient(), appointmentParticipantComponent);
    }

    @Override
    public Patient toOpenmrsType(Patient patient, Appointment.AppointmentParticipantComponent appointmentParticipantComponent) {
        if (appointmentParticipantComponent == null) {
            return patient;
        }

        return patientReferenceTranslator.toOpenmrsType(appointmentParticipantComponent.getActor());
    }
}