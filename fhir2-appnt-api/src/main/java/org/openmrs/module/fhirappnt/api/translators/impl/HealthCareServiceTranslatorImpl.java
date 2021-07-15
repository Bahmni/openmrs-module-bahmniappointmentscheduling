package org.openmrs.module.fhirappnt.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.fhir2.api.dao.FhirLocationDao;
import org.openmrs.module.fhir2.api.translators.impl.AbstractReferenceHandlingTranslator;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;
import org.openmrs.module.fhirappnt.api.translators.AppointmentSpecialityTranslator;
import org.openmrs.module.fhirappnt.api.translators.HealthCareServiceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Component
@Setter(AccessLevel.PACKAGE)
public class HealthCareServiceTranslatorImpl extends AbstractReferenceHandlingTranslator implements HealthCareServiceTranslator {

    @Autowired
    private AppointmentSpecialityTranslator specialityTranslator;

    @Autowired
    private FhirLocationDao locationDao;

    @Override
    public HealthcareService toFhirResource(AppointmentServiceDefinition appointmentService) {
        if (appointmentService == null) {
            return null;
        }
        HealthcareService service = new HealthcareService();
        service.setId(appointmentService.getUuid());
        service.setName(appointmentService.getName());
        service.setComment(appointmentService.getDescription());
        if (appointmentService.getSpeciality() != null) {
            service.addSpecialty(specialityTranslator.toFhirResource(appointmentService.getSpeciality()));
        }
        if (appointmentService.getLocation() != null) {
            service.addLocation(createLocationReference(appointmentService.getLocation()));
        }

        HealthcareService.HealthcareServiceAvailableTimeComponent availableTimeComponent = new HealthcareService.HealthcareServiceAvailableTimeComponent();
        if (appointmentService.getStartTime() != null) {
            availableTimeComponent.setAvailableStartTime(appointmentService.getStartTime().toString());
        }
        if (appointmentService.getEndTime() != null) {
            availableTimeComponent.setAvailableEndTime(appointmentService.getEndTime().toString());
        }
        service.addAvailableTime(availableTimeComponent);

        addHealthCareServiceExtension(service, "maxAppointmentsLimit", appointmentService.getMaxAppointmentsLimit().toString());
        addHealthCareServiceExtension(service, "durationMins", appointmentService.getDurationMins().toString());
        addHealthCareServiceExtension(service, "color", appointmentService.getColor());
        addHealthCareServiceExtension(service, "initialAppointmentStatus", appointmentService.getInitialAppointmentStatus().toString());

        return service;
    }

    @Override
    public AppointmentServiceDefinition toOpenmrsType(HealthcareService healthcareService) {
        return toOpenmrsType(new AppointmentServiceDefinition(), healthcareService);
    }

    @Override
    public AppointmentServiceDefinition toOpenmrsType(AppointmentServiceDefinition appointmentServiceDefinition, HealthcareService healthcareService) {
        if (healthcareService == null) {
            return appointmentServiceDefinition;
        }
        appointmentServiceDefinition.setUuid(healthcareService.getId());
        appointmentServiceDefinition.setName(healthcareService.getName());
        appointmentServiceDefinition.setDescription(healthcareService.getComment());
        if (healthcareService.hasSpecialty()) {
            appointmentServiceDefinition.setSpeciality(specialityTranslator.toOpenmrsType(healthcareService.getSpecialty().get(0)));
        }
        appointmentServiceDefinition.setLocation(locationDao.getLocationByUuid(getReferenceId(healthcareService.getLocation().get(0))));

        getAppointmentServiceExtension(healthcareService).ifPresent(ext -> ext.getExtension()
                .forEach(e -> addHealthCareServiceComponent(appointmentServiceDefinition, e.getUrl(), ((StringType) e.getValue()).getValue())));


        return appointmentServiceDefinition;
    }



    public void addHealthCareServiceComponent(@NotNull AppointmentServiceDefinition serviceDefinition, @NotNull String url, @NotNull String value) {
        if (value == null || url == null || !url.startsWith(AppointmentFhirConstants.OPENMRS_FHIR_EXT_HEALTH_CARE_SERVICE + "#")) {
            return;
        }
        String val = url.substring(url.lastIndexOf('#') + 1);
        switch (val) {
            case "maxAppointmentsLimit":
                serviceDefinition.setMaxAppointmentsLimit(Integer.valueOf(val));
                break;
            case "durationMins":
                serviceDefinition.setDurationMins(Integer.valueOf(val));
                break;
            case "color":
                serviceDefinition.setColor(val);
                break;
            case "initialAppointmentStatus":
                serviceDefinition.setInitialAppointmentStatus(AppointmentStatus.valueOf(val));
                break;
        }
    }

    private void addHealthCareServiceExtension(@NotNull HealthcareService service, @NotNull java.lang.String extensionProperty,
                                               @NotNull String value) {
        if (value == null) {
            return;
        }

        getAppointmentServiceExtension(service)
                .orElseGet(() -> service.addExtension().setUrl(AppointmentFhirConstants.OPENMRS_FHIR_EXT_HEALTH_CARE_SERVICE))
                .addExtension(AppointmentFhirConstants.OPENMRS_FHIR_EXT_HEALTH_CARE_SERVICE + "#" + extensionProperty, new StringType(value));
    }

    private Optional<Extension> getAppointmentServiceExtension(@NotNull HealthcareService service) {
        return Optional.ofNullable(service.getExtensionByUrl(AppointmentFhirConstants.OPENMRS_FHIR_EXT_HEALTH_CARE_SERVICE));

    }
}
