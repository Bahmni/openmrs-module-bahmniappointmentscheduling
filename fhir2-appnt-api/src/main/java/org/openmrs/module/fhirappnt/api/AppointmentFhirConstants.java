package org.openmrs.module.fhirappnt.api;

import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.FhirConstants;

@NoArgsConstructor
public class AppointmentFhirConstants extends FhirConstants {

    public static final String APPOINTMENT_SPECIALITY_VALUESET_URI = HL7_FHIR_VALUE_SET_PREFIX
            + "/c80-practice-codes";

    public static final String APPOINTMENT_PARTICIPANT_TYPE = HL7_FHIR_VALUE_SET_PREFIX
            + "/encounter-participant-type";

    public static final String APPOINTMENT_SERVICE_TYPE = HL7_FHIR_VALUE_SET_PREFIX
            + "/service-type";

    public static final String OPENMRS_FHIR_EXT_HEALTH_CARE_SERVICE= "https://fhir.openmrs.org/ext/health-care-service";

}
