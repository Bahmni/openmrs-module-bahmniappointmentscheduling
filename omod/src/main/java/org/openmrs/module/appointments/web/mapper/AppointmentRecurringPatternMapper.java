package org.openmrs.module.appointments.web.mapper;

import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.openmrs.module.appointments.service.impl.RecurringAppointmentType.DAY;
import static org.openmrs.module.appointments.service.impl.RecurringAppointmentType.WEEK;
import static org.openmrs.module.appointments.service.impl.RecurringAppointmentType.valueOf;

@Component
public class AppointmentRecurringPatternMapper {

    public AppointmentRecurringPattern fromRequest(RecurringPattern recurringPattern) {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setEndDate(recurringPattern.getEndDate());
        appointmentRecurringPattern.setPeriod(recurringPattern.getPeriod());
        appointmentRecurringPattern.setFrequency(recurringPattern.getFrequency());
        String recurringPatternType = recurringPattern.getType();
        if (recurringPatternType == null) {
            throw new IllegalArgumentException(String
                    .format("Valid recurrence type should be provided. Valid types are %s and %s",  DAY, WEEK));
        }
        appointmentRecurringPattern.setType(valueOf(recurringPatternType.toUpperCase()));
        if (appointmentRecurringPattern.getType() == WEEK) {
            appointmentRecurringPattern.setDaysOfWeek(recurringPattern.getDaysOfWeek().stream().map(String::toUpperCase)
                    .collect(Collectors.joining(",")));
        }
        return appointmentRecurringPattern;
    }
}