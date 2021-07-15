package org.openmrs.module.fhirappnt.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.translators.impl.AbstractReferenceHandlingTranslator;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;
import org.openmrs.module.fhirappnt.api.translators.AppointmentParticipantTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class AppointmentPractitionerTranslatorImpl extends AbstractReferenceHandlingTranslator implements AppointmentParticipantTranslator<Provider> {

    @Autowired
    private FhirPractitionerDao practitionerDao;

    @Override
    public Appointment.AppointmentParticipantComponent toFhirResource(Provider provider) {
        if (provider == null) {
            return null;
        }
        Appointment.AppointmentParticipantComponent participant = new Appointment.AppointmentParticipantComponent();
        CodeableConcept role = new CodeableConcept();
        role.addCoding(new Coding(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE, "Practitioner", "Practitioner"));
        participant.setRequired(Appointment.ParticipantRequired.OPTIONAL);
        participant.setActor(createPractitionerReference(provider));
        participant.addType(role);

        return participant;
    }

    @Override
    public Provider toOpenmrsType(Appointment.AppointmentParticipantComponent appointmentParticipantComponent) {
        return toOpenmrsType(new Provider(), appointmentParticipantComponent);
    }

    @Override
    public Provider toOpenmrsType(Provider appointmentProvider, Appointment.AppointmentParticipantComponent appointmentParticipantComponent) {
        if (appointmentParticipantComponent == null) {
            return appointmentProvider;
        }

        return practitionerDao.getProviderByUuid(getReferenceId(appointmentParticipantComponent.getActor()));
    }
}
