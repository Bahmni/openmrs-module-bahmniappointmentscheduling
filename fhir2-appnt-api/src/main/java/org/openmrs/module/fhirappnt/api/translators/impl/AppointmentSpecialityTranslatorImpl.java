package org.openmrs.module.fhirappnt.api.translators.impl;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;
import org.openmrs.module.fhirappnt.api.translators.AppointmentSpecialityTranslator;
import org.openmrs.module.appointments.model.Speciality;
import org.springframework.stereotype.Component;

@Component
public class AppointmentSpecialityTranslatorImpl  implements AppointmentSpecialityTranslator {

    @Override
    public CodeableConcept toFhirResource(Speciality speciality) {
        if (speciality == null) {
            return null;
        }

        CodeableConcept code = new CodeableConcept();
        code.addCoding(new Coding(AppointmentFhirConstants.APPOINTMENT_SPECIALITY_VALUESET_URI,speciality.getUuid(), speciality.getName()));

        return code;
    }

    @Override
    public Speciality toOpenmrsType(CodeableConcept codeableConcept) {
        return toOpenmrsType(new Speciality(), codeableConcept);
    }

    @Override
    public Speciality toOpenmrsType(Speciality speciality, CodeableConcept codeableConcept) {
        if (codeableConcept == null) {
            return speciality;
        }

        if (codeableConcept.hasCoding()) {
            speciality.setUuid(codeableConcept.getCoding().get(0).getCode());
            speciality.setName(codeableConcept.getCoding().get(0).getDisplay());
        }

        return speciality;
    }
}
