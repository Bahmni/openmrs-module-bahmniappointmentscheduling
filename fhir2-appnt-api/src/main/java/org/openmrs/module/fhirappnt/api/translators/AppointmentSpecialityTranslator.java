package org.openmrs.module.fhirappnt.api.translators;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirUpdatableTranslator;

public interface AppointmentSpecialityTranslator extends OpenmrsFhirUpdatableTranslator<Speciality, CodeableConcept>{

}
