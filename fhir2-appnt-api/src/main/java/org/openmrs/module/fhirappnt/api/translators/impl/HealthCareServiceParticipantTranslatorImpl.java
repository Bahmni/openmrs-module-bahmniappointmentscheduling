package org.openmrs.module.fhirappnt.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.fhir2.api.translators.impl.AbstractReferenceHandlingTranslator;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;
import org.openmrs.module.fhirappnt.api.translators.AppointmentParticipantTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@Setter(AccessLevel.PACKAGE)
public class HealthCareServiceParticipantTranslatorImpl extends AbstractReferenceHandlingTranslator implements AppointmentParticipantTranslator<AppointmentServiceDefinition> {

    @Autowired
    AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Override
    public Appointment.AppointmentParticipantComponent toFhirResource(AppointmentServiceDefinition serviceDefinition) {
        if (serviceDefinition == null) {
            return null;
        }
        Appointment.AppointmentParticipantComponent participant = new Appointment.AppointmentParticipantComponent();
        CodeableConcept role = new CodeableConcept();
        role.addCoding(new Coding(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE, "HealthCareService", "HealthCareService"));
        participant.setRequired(Appointment.ParticipantRequired.REQUIRED);
        participant.setActor(createHeathCareServiceReference(serviceDefinition));
        participant.addType(role);
        participant.setRequired(Appointment.ParticipantRequired.OPTIONAL);
        participant.setStatus(Appointment.ParticipationStatus.ACCEPTED);

        return participant;
    }

    @Override
    public AppointmentServiceDefinition toOpenmrsType(Appointment.AppointmentParticipantComponent appointmentParticipantComponent) {
        return toOpenmrsType(new AppointmentServiceDefinition(), appointmentParticipantComponent);
    }

    @Override
    public AppointmentServiceDefinition toOpenmrsType(AppointmentServiceDefinition serviceDefinition, Appointment.AppointmentParticipantComponent appointmentParticipantComponent) {
        if (appointmentParticipantComponent == null) {
            return null;
        }

        return appointmentServiceDefinitionService.getAppointmentServiceByUuid(getReferenceId(appointmentParticipantComponent.getActor()));
    }

    private Reference createHeathCareServiceReference(@NotNull AppointmentServiceDefinition serviceDefinition) {
        Reference reference = (new Reference()).setReference("HealthCareService/" + serviceDefinition.getUuid()).setType("HealthCareService");
        if (serviceDefinition.getName() != null) {
            reference.setDisplay(serviceDefinition.getName());
        }

        return reference;
    }
}
