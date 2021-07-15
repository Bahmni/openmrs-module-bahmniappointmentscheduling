package org.openmrs.module.fhirappnt.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;
import org.openmrs.module.appointments.model.Speciality;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentSpecialityTranslatorImplTest {

    private static String SPECIALITY_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private static String SPECIALITY_NAME = "Oncologist";

    private AppointmentSpecialityTranslatorImpl appointmentSpecialityTranslator;

    private Speciality speciality;

    @Before
    public void setup() {
        appointmentSpecialityTranslator = new AppointmentSpecialityTranslatorImpl();
        speciality  = new Speciality();
    }

    @Test
    public void shouldTranslateSpecialityToCodeableConcept()  {
        speciality.setUuid(SPECIALITY_UUID);
        speciality.setName(SPECIALITY_NAME);

        CodeableConcept code = appointmentSpecialityTranslator.toFhirResource(speciality);
        assertThat(code, notNullValue());
        assertThat(code.getCoding().size(), equalTo(1));
        assertThat(code.getCoding().get(0).getSystem(), equalTo(AppointmentFhirConstants.APPOINTMENT_SPECIALITY_VALUESET_URI));
        assertThat(code.getCoding().get(0).getCode(), equalTo(SPECIALITY_UUID));
        assertThat(code.getCoding().get(0).getDisplay(), equalTo(SPECIALITY_NAME));
    }

    @Test
    public void shouldTranslateSpecialityCodeableConceptToSpeciality() {
        CodeableConcept code = new CodeableConcept();
        code.addCoding(new Coding(AppointmentFhirConstants.APPOINTMENT_SPECIALITY_VALUESET_URI, SPECIALITY_UUID, SPECIALITY_NAME));

        Speciality speciality = appointmentSpecialityTranslator.toOpenmrsType(new Speciality(), code);
        assertThat(speciality, notNullValue());
        assertThat(speciality.getUuid(), equalTo(SPECIALITY_UUID));
        assertThat(speciality.getName(), equalTo(SPECIALITY_NAME));
    }
}
