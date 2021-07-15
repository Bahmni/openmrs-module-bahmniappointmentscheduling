package org.openmrs.module.fhirappnt.api.translators.impl;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.fhir2.api.translators.AppointmentTranslator;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;
import org.openmrs.module.fhirappnt.api.translators.AppointmentParticipantTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Setter(AccessLevel.PACKAGE)
public class AppointmentTranslatorImpl implements AppointmentTranslator<org.openmrs.module.appointments.model.Appointment> {

    @Autowired
    private AppointmentParticipantTranslator<Patient> patientTranslator;

    @Autowired
    private AppointmentParticipantTranslator<Provider> providerTranslator;

    @Autowired
    private AppointmentParticipantTranslator<AppointmentProvider> appointmentParticipantTranslator;

    @Autowired
    AppointmentParticipantTranslator<AppointmentServiceDefinition> serviceDefinitionAppointmentParticipantTranslator;

    @Autowired
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Override
    public Appointment toFhirResource(org.openmrs.module.appointments.model.Appointment appointment) {
        Appointment fhirAppointment  = new Appointment();
        fhirAppointment.setId(appointment.getUuid());
        fhirAppointment.setStart(appointment.getStartDateTime());
        fhirAppointment.setEnd(appointment.getEndDateTime());
        fhirAppointment.addParticipant(patientTranslator.toFhirResource(appointment.getPatient()));
        if (appointment.getProvider() != null){
            fhirAppointment.addParticipant(providerTranslator.toFhirResource(appointment.getProvider()));
        }

        if (appointment.getProviders() != null) {
            for (AppointmentProvider provider : appointment.getProviders()) {
                fhirAppointment.addParticipant(appointmentParticipantTranslator.toFhirResource(provider));
            }
        }
        if (appointment.getService() != null) {
            fhirAppointment.addParticipant(serviceDefinitionAppointmentParticipantTranslator.toFhirResource(appointment.getService()));
        }
        if (appointment.getServiceType() != null) {
            fhirAppointment.addServiceType(new CodeableConcept().addCoding(new Coding(AppointmentFhirConstants.APPOINTMENT_SERVICE_TYPE, appointment.getServiceType().getUuid(), appointment.getServiceType().getName())));
        }
        if (appointment.getAppointmentKind() != null) {
            switch (appointment.getAppointmentKind()) {
                case Scheduled:
                    fhirAppointment.setAppointmentType(new CodeableConcept().addCoding(new Coding("", "ROUTINE", "Routine appointment ")));
                    break;
                case WalkIn:
                    fhirAppointment.setAppointmentType(new CodeableConcept().addCoding(new Coding("", "WALKIN", "A previously unscheduled walk-in visit")));
            }
        }

        switch (appointment.getStatus()) {
            case Requested :
                fhirAppointment.setStatus(Appointment.AppointmentStatus.PROPOSED);
                break;
            case Scheduled:
                fhirAppointment.setStatus(Appointment.AppointmentStatus.BOOKED);
                break;
            case CheckedIn:
                fhirAppointment.setStatus(Appointment.AppointmentStatus.CHECKEDIN);
                break;
            case Completed:
                fhirAppointment.setStatus(Appointment.AppointmentStatus.FULFILLED);
                break;
            case Missed:
                fhirAppointment.setStatus(Appointment.AppointmentStatus.NOSHOW);
                break;
            case Cancelled:
                fhirAppointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
                break;
        }

        fhirAppointment.setComment(appointment.getComments());
        fhirAppointment.setCreated(appointment.getDateCreated());
        fhirAppointment.getMeta().setLastUpdated(appointment.getDateChanged());

        return fhirAppointment;
    }

    @Override
    public org.openmrs.module.appointments.model.Appointment toOpenmrsType(Appointment appointment) {
        return toOpenmrsType(new org.openmrs.module.appointments.model.Appointment(), appointment);
    }

    @Override
    public org.openmrs.module.appointments.model.Appointment toOpenmrsType(org.openmrs.module.appointments.model.Appointment appointment, Appointment fhirAppointment) {
        appointment.setUuid(fhirAppointment.getId());
        appointment.setStartDateTime(fhirAppointment.getStart());
        appointment.setEndDateTime(fhirAppointment.getEnd());

        Set<AppointmentProvider> providers = new HashSet<>();

        if (fhirAppointment.hasParticipant()) {
            for (Appointment.AppointmentParticipantComponent participantComponent : fhirAppointment.getParticipant()) {
                if (participantComponent.getType().get(0).getCoding().get(0).getCode() == "Patient") {
                    appointment.setPatient(patientTranslator.toOpenmrsType(participantComponent));
                } else if (participantComponent.getType().get(0).getCoding().get(0).getCode() == "Practitioner") {
                    appointment.setProvider(providerTranslator.toOpenmrsType(participantComponent));
                } else if (participantComponent.getType().get(0).getCoding().get(0).getCode() == "AppointmentPractitioner") {
                    AppointmentProvider provider = appointmentParticipantTranslator.toOpenmrsType(participantComponent);
                    provider.setAppointment(appointment);
                    providers.add(provider);
                    appointment.setProviders(providers);
                } else {
                    appointment.setService(serviceDefinitionAppointmentParticipantTranslator.toOpenmrsType(participantComponent));
                }
            }
        }

        if (fhirAppointment.hasServiceType()) {
            appointment.setServiceType(appointmentServiceDefinitionService.getAppointmentServiceTypeByUuid(fhirAppointment.getServiceType().get(0).getCoding().get(0).getCode()));
        }

        if (fhirAppointment.hasAppointmentType())  {
            if (fhirAppointment.getAppointmentType().getCoding().get(0).getCode()  == "ROUTINE")  {
                appointment.setAppointmentKind(AppointmentKind.Scheduled);
            } else if (fhirAppointment.getAppointmentType().getCoding().get(0).getCode()  == "WALKIN")  {
                appointment.setAppointmentKind(AppointmentKind.WalkIn);
            }
        }

        if (fhirAppointment.hasStatus()) {
            switch (fhirAppointment.getStatus()) {
                case PROPOSED:
                    appointment.setStatus(AppointmentStatus.Requested);
                    break;
                case BOOKED:
                    appointment.setStatus(AppointmentStatus.Scheduled);
                    break;
                case CHECKEDIN:
                    appointment.setStatus(AppointmentStatus.CheckedIn);
                    break;
                case FULFILLED:
                    appointment.setStatus(AppointmentStatus.Completed);
                    break;
                case NOSHOW:
                    appointment.setStatus(AppointmentStatus.Missed);
                    break;
                case CANCELLED:
                    appointment.setStatus(AppointmentStatus.Cancelled);
                    break;
            }
        }
        appointment.setComments(fhirAppointment.getComment());

        return appointment;
    }
}