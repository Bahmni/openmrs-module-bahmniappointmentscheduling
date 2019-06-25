package org.openmrs.module.appointments.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.service.RecurringAppointmentService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.*;
import org.openmrs.module.appointments.web.helper.RecurringAppointmentsHelper;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointment")
public class AppointmentController {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private RecurringAppointmentService recurringAppointmentService;

    @Autowired
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private AppointmentServiceMapper appointmentServiceMapper;

    @Autowired
    private RecurringAppointmentsHelper recurringAppointmentsHelper;

    @RequestMapping(method = RequestMethod.GET, value = "all")
    @ResponseBody
    public List<AppointmentDefaultResponse> getAllAppointments(@RequestParam(value = "forDate", required = false) String forDate) throws ParseException {
        List<Appointment> appointments = appointmentsService.getAllAppointments(DateUtil.convertToLocalDateFromUTC(forDate));
        return appointmentMapper.constructResponse(appointments);
    }
    @RequestMapping( method = RequestMethod.POST, value = "search")
    @ResponseBody
    public List<AppointmentDefaultResponse> searchAppointments( @Valid @RequestBody AppointmentQuery searchQuery) throws IOException {
        Appointment appointment = appointmentMapper.mapQueryToAppointment(searchQuery);
        if (searchQuery.getStatus() == null) {
            appointment.setStatus(null);
        }
        List<Appointment> appointments =  appointmentsService.search(appointment);
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> saveAppointment(@Valid @RequestBody AppointmentRequest appointmentRequest) throws IOException {
        try {
            RecurringPattern recurringPattern = appointmentRequest.getRecurringPattern();
            if (recurringPattern == null) {
                Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
                appointmentsService.validateAndSave(appointment);
                return new ResponseEntity<>(appointmentMapper.constructResponse(appointment), HttpStatus.OK);
            } else {
                recurringAppointmentsHelper.validateRecurringPattern(recurringPattern);
                AppointmentRecurringPattern appointmentRecurringPattern = appointmentMapper
                        .fromRequestRecurringPattern(recurringPattern);
                List<Appointment> appointmentsList = recurringAppointmentsHelper.generateRecurringAppointments(appointmentRecurringPattern,
                        appointmentRequest);

                recurringAppointmentService.validateAndSave(appointmentRecurringPattern, appointmentsList);
                return new ResponseEntity<>(appointmentMapper.constructResponse(appointmentsList), HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("Runtime error while trying to create new appointment", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping( method = RequestMethod.GET, value = "futureAppointmentsForServiceType")
    @ResponseBody
    public List<AppointmentDefaultResponse> getAllFututreAppointmentsForGivenServiceType(@RequestParam(value = "appointmentServiceTypeUuid", required = true) String serviceTypeUuid) {
        AppointmentServiceType appointmentServiceType = appointmentServiceDefinitionService.getAppointmentServiceTypeByUuid(serviceTypeUuid);
        List<Appointment> appointments = appointmentsService.getAllFutureAppointmentsForServiceType(appointmentServiceType);
        return appointmentMapper.constructResponse(appointments);
    }

    @RequestMapping(method = RequestMethod.GET, value = "appointmentSummary")
    @ResponseBody
    public List<AppointmentsSummary> getAllAppointmentsSummary(@RequestParam(value = "startDate") String startDateString, @RequestParam(value = "endDate") String endDateString) throws ParseException {
        List<AppointmentsSummary> appointmentsSummaryList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = DateUtil.convertToLocalDateFromUTC(startDateString);
        Date endDate = DateUtil.convertToLocalDateFromUTC(endDateString);
        List<AppointmentServiceDefinition> appointmentServiceDefinitions = appointmentServiceDefinitionService.getAllAppointmentServices(false);
        for (AppointmentServiceDefinition appointmentServiceDefinition : appointmentServiceDefinitions) {
            List<Appointment> appointmentsForService =
                    appointmentsService.getAppointmentsForService(
                            appointmentServiceDefinition, startDate, endDate,
                            Arrays.asList(
                                    AppointmentStatus.Completed,
                                    AppointmentStatus.Scheduled,
                                    AppointmentStatus.CheckedIn,
                                    AppointmentStatus.Missed));

            Map<Date, List<Appointment>> appointmentsGroupedByDate =
                    appointmentsForService.stream().collect(Collectors.groupingBy(Appointment::getDateFromStartDateTime));

            Map<String, DailyAppointmentServiceSummary> appointmentCountMap = new LinkedHashMap<>();
            for (Map.Entry<Date, List<Appointment>> appointmentDateMap : appointmentsGroupedByDate.entrySet()) {
                List<Appointment> appointments = appointmentDateMap.getValue();
                Long missedAppointmentsCount = appointments.stream().filter(s-> s.getStatus().equals(AppointmentStatus.Missed)).count();
                DailyAppointmentServiceSummary dailyAppointmentServiceSummary = new DailyAppointmentServiceSummary(
                        appointmentDateMap.getKey(), appointmentServiceDefinition.getUuid(), appointments.size(),Math.toIntExact(missedAppointmentsCount));
                appointmentCountMap.put(simpleDateFormat.format(appointmentDateMap.getKey()), dailyAppointmentServiceSummary);
            }

            AppointmentsSummary appointmentsSummary = new AppointmentsSummary(appointmentServiceMapper.constructDefaultResponse(appointmentServiceDefinition), appointmentCountMap);
            appointmentsSummaryList.add(appointmentsSummary);
        }
        return appointmentsSummaryList;
    }

    @RequestMapping(method = RequestMethod.POST, value="undoStatusChange/{appointmentUuid}")
    @ResponseBody
    public ResponseEntity<Object> undoStatusChange(@PathVariable("appointmentUuid")String appointmentUuid) throws ParseException {
        try{
            Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
            if(appointment == null){
                throw new RuntimeException("Appointment does not exist");
            }
            appointmentsService.undoStatusChange(appointment);
            return new ResponseEntity<>(appointmentMapper.constructResponse(appointment), HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to undo appointment status", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public AppointmentDefaultResponse getAppointmentByUuid(@RequestParam(value = "uuid") String uuid)  {
        Appointment appointment = appointmentsService.getAppointmentByUuid(uuid);
        if(appointment == null) {
            log.error("Invalid. Appointment does not exist. UUID - " + uuid);
            throw new RuntimeException("Appointment does not exist");
        }
        return appointmentMapper.constructResponse(appointment);
    }

    @RequestMapping(method = RequestMethod.POST, value="/{appointmentUuid}/providerResponse")
    @ResponseBody
    public ResponseEntity<Object> updateAppointmentProviderResponse(@PathVariable("appointmentUuid")String appointmentUuid, @RequestBody AppointmentProviderDetail providerResponse) throws ParseException {
        try {
            Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
            if(appointment == null){
                throw new RuntimeException("Appointment does not exist");
            }
            AppointmentProvider appointmentProviderProvider = appointmentMapper.mapAppointmentProvider(providerResponse);
            appointmentsService.updateAppointmentProviderResponse(appointmentProviderProvider);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (RuntimeException e) {
            log.error("Runtime error while trying to update appointment provider response", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    //TODO write test
    @RequestMapping(method = RequestMethod.POST, value="/{uuid}/reschedule")
    @ResponseBody
    public ResponseEntity<Object> rescheduleAppointment(@PathVariable("uuid") String prevAppointmentUuid,
                                                        @Valid @RequestBody AppointmentRequest appointmentRequest,
                                                        @RequestParam(value = "retainNumber", required = false, defaultValue = "false") boolean retainAppointmentNumber)
            throws ParseException {
        try {
            appointmentRequest.setUuid(null);
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
            Appointment rescheduledAppointment = appointmentsService.reschedule(prevAppointmentUuid, appointment, false);
            return new ResponseEntity<>(appointmentMapper.constructResponse(rescheduledAppointment), HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to create new appointment", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value="/{appointmentUuid}")
    @ResponseBody
    public ResponseEntity<Object> editAppointment(@Valid @RequestBody AppointmentRequest appointmentRequest,
                                                  @RequestParam(value = "applyForAll") boolean applyForAll) {
        try {
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
            if (applyForAll) {
                List<Appointment> updatedAppointments = recurringAppointmentService.update(appointment);
                return new ResponseEntity<>(appointmentMapper.constructResponse(updatedAppointments), HttpStatus.OK);
            } else {
                Appointment updatedAppointment = appointmentsService.update(appointment);
                return new ResponseEntity<>(appointmentMapper.constructResponse(updatedAppointment), HttpStatus.OK);
            }
        } catch (RuntimeException e) {
            log.error("Runtime error while trying to update an appointment", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
