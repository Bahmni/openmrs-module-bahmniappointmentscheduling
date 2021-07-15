package org.openmrs.module.fhirappnt.api.translators;

import org.hl7.fhir.r4.model.HealthcareService;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirUpdatableTranslator;

public interface HealthCareServiceTranslator extends OpenmrsFhirUpdatableTranslator<AppointmentServiceDefinition, HealthcareService> {

}
