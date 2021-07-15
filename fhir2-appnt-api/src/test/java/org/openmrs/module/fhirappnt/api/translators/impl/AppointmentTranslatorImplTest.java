package org.openmrs.module.fhirappnt.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import org.exparity.hamcrest.date.DateMatchers;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.codesystems.ServiceType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhirappnt.api.AppointmentFhirConstants;
import org.openmrs.module.fhirappnt.api.translators.AppointmentParticipantTranslator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentTranslatorImplTest {

    private static String APPOINTMENT_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private static String PATIENT_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private static String PROVIDER_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private static String SERVICE_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private static String SERVICE_TYPE_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    @Mock
    private AppointmentParticipantTranslator<Patient> patientTranslator;

    @Mock
    private AppointmentParticipantTranslator<Provider> providerTranslator;

    @Mock
    private AppointmentParticipantTranslator<AppointmentProvider> appointmentParticipantTranslator;

    @Mock
    AppointmentParticipantTranslator<AppointmentServiceDefinition> serviceDefinitionAppointmentParticipantTranslator;

    @Mock
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    private AppointmentTranslatorImpl appointmentTranslator;

    private Appointment appointment;

    @Before
    public void setUp() {
        appointmentTranslator = new AppointmentTranslatorImpl();
        appointmentTranslator.setPatientTranslator(patientTranslator);
        appointmentTranslator.setProviderTranslator(providerTranslator);
        appointmentTranslator.setAppointmentParticipantTranslator(appointmentParticipantTranslator);
        appointmentTranslator.setAppointmentServiceDefinitionService(appointmentServiceDefinitionService);
        appointmentTranslator.setServiceDefinitionAppointmentParticipantTranslator(serviceDefinitionAppointmentParticipantTranslator);
        appointment = new Appointment();
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);
        appointment.setPatient(patient);
    }

    @Test
    public void toFhirResourceShouldTranslateUuidToId() {
        appointment.setUuid(APPOINTMENT_UUID);
        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(APPOINTMENT_UUID));
    }

    @Test
    public void toFhirTypeShouldMapStatusRequestedToProposed() {
        appointment.setStatus(AppointmentStatus.Requested);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.PROPOSED));
    }

    @Test
    public void toFhirTypeShouldMapStatusScheduledToBooked() {
        appointment.setStatus(AppointmentStatus.Scheduled);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.BOOKED));
    }

    @Test
    public void toFhirTypeShouldMapStatusCheckedInToCheckedIn() {
        appointment.setStatus(AppointmentStatus.CheckedIn);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.CHECKEDIN));
    }

    @Test
    public void toFhirTypeShouldMapStatusCompletedToFulfilled() {
        appointment.setStatus(AppointmentStatus.Completed);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.FULFILLED));
    }

    @Test
    public void toFhirTypeShouldMapStatusMissedToNoShow() {
        appointment.setStatus(AppointmentStatus.Missed);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.NOSHOW));
    }

    @Test
    public void toFhirTypeShouldMapStatusCancelledToCancelled() {
        appointment.setStatus(AppointmentStatus.Cancelled);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.CANCELLED));
    }

    @Test
    public void toFhirTypeShouldMapStartDateTimeToStart() {
        appointment.setStartDateTime(new Date());

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStart(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toFhirTypeShouldMapEndDateTimeToEnd() {
        appointment.setEndDateTime(new Date());

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getEnd(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toFhirTypeShouldMapDateCreatedToDateCreated() {
        appointment.setDateCreated(new Date());

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getCreated(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toFhirTypeShouldMapDateChangedToLastDateUpdated() {
        appointment.setDateChanged(new Date());

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toFhirTypeShouldConvertCommentsToFhirAppointmentComment() {
        appointment.setComments("Test Appointment");

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getComment(), equalTo("Test Appointment"));
    }

    @Test
    public void toFhirTypeShouldAddPatientAsParticipant() {
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);
        appointment.setPatient(patient);
        org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent participantComponent = new org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent();
        Reference patientReference = new Reference().setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID)
                .setType(FhirConstants.PATIENT).setIdentifier(new Identifier().setValue(PATIENT_UUID));
        participantComponent.setActor(patientReference);

        when(patientTranslator.toFhirResource(patient)).thenReturn(participantComponent);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getParticipant().size(), greaterThanOrEqualTo(1));
        assertThat(result.getParticipant().get(0).getActor(), equalTo(patientReference));
    }

    @Test
    public void toFhirTypeShouldAddAppointmentProviderAsParticipant() {
        Provider provider = new Provider();
        provider.setUuid(PROVIDER_UUID);
        Set<AppointmentProvider>  appointmentProviders = new HashSet<>();
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(provider);
        appointmentProviders.add(appointmentProvider);
        appointment.setProviders(appointmentProviders);
        org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent participantComponent = new org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent();
        Reference providerReference = new Reference().setReference(FhirConstants.PRACTITIONER + "/" + PROVIDER_UUID)
                .setType(FhirConstants.PRACTITIONER).setIdentifier(new Identifier().setValue(PROVIDER_UUID));
        participantComponent.setActor(providerReference);

        when(appointmentParticipantTranslator.toFhirResource(appointmentProvider)).thenReturn(participantComponent);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getParticipant().size(), greaterThanOrEqualTo(1));
        assertThat(result.getParticipant().get(0).getActor(), equalTo(providerReference));
    }

    @Test
    public void toFhirTypeShouldAddProviderAsParticipant() {
        Provider provider = new Provider();
        provider.setUuid(PROVIDER_UUID);
        appointment.setProvider(provider);
        org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent participantComponent = new org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent();
        Reference providerReference = new Reference().setReference(FhirConstants.PRACTITIONER + "/" + PROVIDER_UUID)
                .setType(FhirConstants.PRACTITIONER).setIdentifier(new Identifier().setValue(PROVIDER_UUID));
        participantComponent.setActor(providerReference);

        when(providerTranslator.toFhirResource(provider)).thenReturn(participantComponent);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getParticipant().size(), greaterThanOrEqualTo(1));
        assertThat(result.getParticipant().get(0).getActor(), equalTo(providerReference));
    }

    @Test
    public void toFhirTypeShouldReturnNullIfAppointmentServiceDefinitionIsNull() {
        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getParticipant().isEmpty(), equalTo(true));
    }

    @Test
    public void toFhirTypeShouldAddHealthCareServiceAsParticipant() {
        AppointmentServiceDefinition appointmentService = new AppointmentServiceDefinition();
        appointmentService.setUuid(SERVICE_UUID);
        appointmentService.setName("Outpatient");
        appointment.setService(appointmentService);
        org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent participantComponent = new org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent();
        Reference serviceReference = new Reference().setReference("HealthCareService" + "/" + SERVICE_UUID)
                .setType("HealthCareService").setIdentifier(new Identifier().setValue(SERVICE_UUID));
        participantComponent.setActor(serviceReference);

        when(serviceDefinitionAppointmentParticipantTranslator.toFhirResource(appointmentService)).thenReturn(participantComponent);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getParticipant().size(), greaterThanOrEqualTo(1));
        assertThat(result.getParticipant().get(0).getActor(), equalTo(serviceReference));
    }

    @Test
    public void toFhirTypeShouldReturnNullIfServiceTypeIsNull() {

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getServiceType().isEmpty(), equalTo(true));
    }

    @Test
    public void toFhirTypeShouldTranslateServiceTypeCorrectly() {
        AppointmentServiceType serviceType = new AppointmentServiceType();
        serviceType.setUuid(SERVICE_TYPE_UUID);
        appointment.setServiceType(serviceType);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getServiceType().size(), greaterThanOrEqualTo(1));
        assertThat(result.getServiceType().get(0).getCoding().get(0).getCode(), equalTo(SERVICE_TYPE_UUID));
    }

    @Test
    public void toFhirTypeShouldReturnNullIfAppointmentKindIsNull() {

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getAppointmentType().isEmpty(), equalTo(true));
    }

    @Test
    public void toFhirTypeShouldTranslateScheduledToRoutineAppointmentTyeRoutine() {
        appointment.setAppointmentKind(AppointmentKind.Scheduled);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getAppointmentType(), notNullValue());
        assertThat(result.getAppointmentType().getCoding().get(0).getCode(), equalTo("ROUTINE"));
    }

    @Test
    public void toFhirTypeShouldTranslateWalkInToRoutineAppointmentTyeWalkIn() {
        appointment.setAppointmentKind(AppointmentKind.WalkIn);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getAppointmentType(), notNullValue());
        assertThat(result.getAppointmentType().getCoding().get(0).getCode(), equalTo("WALKIN"));
    }












    @Test
    public void toOpenmrsTypeShouldTranslateIdToUuid() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setId(APPOINTMENT_UUID);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getUuid(), equalTo(APPOINTMENT_UUID));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusProposedToRequested() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.PROPOSED);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.Requested));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusBookedToScheduled() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.BOOKED);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.Scheduled));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusCheckedInToCheckedIn() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.CHECKEDIN);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.CheckedIn));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusNoShowToMissed() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.NOSHOW);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.Missed));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusCancelledToCancelled() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.CANCELLED);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.Cancelled));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusFulfilledToCompleted() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.FULFILLED);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.Completed));
    }

    @Test
    public void toOpenmrsTypeShouldMapStartToStartDateTime() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStart(new Date());

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStartDateTime(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toOpenmrsTypeShouldMapEndToEndDateTime() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setEnd(new Date());

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getEndDateTime(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toOpenmrsTypeShouldMapFhirCommentToComments() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setComment("Test Appointment");

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getComments(), equalTo("Test Appointment"));
    }

    @Test
    public void toOpenmrsTypeShouldTranslateParticipantToPatient() {
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);
        appointment.setPatient(patient);
        org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent participantComponent = new org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent();
        Reference patientReference = new Reference().setReference(FhirConstants.PATIENT + "/" + PATIENT_UUID)
                .setType(FhirConstants.PATIENT).setIdentifier(new Identifier().setValue(PATIENT_UUID));
        participantComponent.setActor(patientReference);
        participantComponent.addType(new CodeableConcept().addCoding(new Coding(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE, "Patient", "Patient")));

        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.addParticipant(participantComponent);

        when(patientTranslator.toOpenmrsType(participantComponent)).thenReturn(patient);

        Appointment result = appointmentTranslator.toOpenmrsType(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getPatient(), notNullValue());
        assertThat(result.getPatient(), equalTo(patient));
    }

    @Test
    public void toOpenmrsTypeShouldTranslateParticipantToAppointmentProvider() {
        Provider provider = new Provider();
        provider.setUuid(PROVIDER_UUID);
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(provider);

        org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent participantComponent = new org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent();
        Reference providerReference = new Reference().setReference(FhirConstants.PRACTITIONER + "/" + PROVIDER_UUID)
                .setType(FhirConstants.PRACTITIONER).setIdentifier(new Identifier().setValue(PROVIDER_UUID));
        participantComponent.setActor(providerReference);
        participantComponent.addType(new CodeableConcept().addCoding(new Coding(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE, "AppointmentPractitioner", "AppointmentPractitioner")));


        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.addParticipant(participantComponent);

        when(appointmentParticipantTranslator.toOpenmrsType(participantComponent)).thenReturn(appointmentProvider);

        Appointment result = appointmentTranslator.toOpenmrsType(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getProviders().size(), greaterThanOrEqualTo(1));
        assertThat(result.getProviders().iterator().next().getProvider(), equalTo(provider));
    }

    @Test
    public void toOpenmrsTypeShouldTranslateParticipantToProvider() {
        Provider provider = new Provider();
        provider.setUuid(PROVIDER_UUID);

        org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent participantComponent = new org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent();
        Reference providerReference = new Reference().setReference(FhirConstants.PRACTITIONER + "/" + PROVIDER_UUID)
                .setType(FhirConstants.PRACTITIONER).setIdentifier(new Identifier().setValue(PROVIDER_UUID));
        participantComponent.setActor(providerReference);
        participantComponent.addType(new CodeableConcept().addCoding(new Coding(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE, "Practitioner", "Practitioner")));


        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.addParticipant(participantComponent);

        when(providerTranslator.toOpenmrsType(participantComponent)).thenReturn(provider);

        Appointment result = appointmentTranslator.toOpenmrsType(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getProvider(), notNullValue());
        assertThat(result.getProvider(), equalTo(provider));
    }

    @Test
    public void toOpenmrsTypeShouldTranslateParticipantToAppointmentService() {
        AppointmentServiceDefinition appointmentService = new AppointmentServiceDefinition();
        appointmentService.setUuid(SERVICE_UUID);
        appointmentService.setName("Outpatient");

        org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent participantComponent = new org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent();
        Reference serviceReference = new Reference().setReference("HealthCareService" + "/" + SERVICE_UUID)
                .setType("HealthCareService").setIdentifier(new Identifier().setValue(SERVICE_UUID));
        participantComponent.setActor(serviceReference);
        participantComponent.addType(new CodeableConcept().addCoding(new Coding(AppointmentFhirConstants.APPOINTMENT_PARTICIPANT_TYPE, "HealthCareService", "HealthCareService")));


        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.addParticipant(participantComponent);

        when(serviceDefinitionAppointmentParticipantTranslator.toOpenmrsType(participantComponent)).thenReturn(appointmentService);

        Appointment result = appointmentTranslator.toOpenmrsType(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getService(), notNullValue());
        assertThat(result.getService(), equalTo(appointmentService));
    }

    @Test
    public void toOpenmrsTypeShouldTranslateServiceTypeCorrectly() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.addServiceType(new CodeableConcept().addCoding(new Coding("", SERVICE_TYPE_UUID,"OutPatient")));

        AppointmentServiceType serviceType = new AppointmentServiceType();
        serviceType.setUuid(SERVICE_TYPE_UUID);

        when(appointmentServiceDefinitionService.getAppointmentServiceTypeByUuid(SERVICE_TYPE_UUID)).thenReturn(serviceType);
        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getServiceType().getUuid(), equalTo(SERVICE_TYPE_UUID));
    }

    @Test
    public void toOpenmrsTypeShouldTranslateAppointmentTypeScheduledCorrectly() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setAppointmentType(new CodeableConcept().addCoding(new Coding("", "ROUTINE","")));

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getAppointmentKind(), equalTo(AppointmentKind.Scheduled));
    }

    @Test
    public void toOpenmrsTypeShouldTranslateAppointmentTypeWalkInCorrectly() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setAppointmentType(new CodeableConcept().addCoding(new Coding("", "WALKIN","")));

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getAppointmentKind(), equalTo(AppointmentKind.WalkIn));
    }

}

