package org.openmrs.module.fhirappnt.api.translators;

import org.hl7.fhir.r4.model.Appointment;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirUpdatableTranslator;

public interface AppointmentParticipantTranslator<T> extends OpenmrsFhirUpdatableTranslator<T, Appointment.AppointmentParticipantComponent> {
}
