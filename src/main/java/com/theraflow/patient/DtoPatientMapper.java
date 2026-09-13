package com.theraflow.patient;

import com.theraflow.patient.dto.PatientRequest;
import com.theraflow.patient.dto.PatientResponse;
import com.theraflow.patient.model.Patient;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface DtoPatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "therapist", ignore = true)
    Patient toPatient(PatientRequest request);


    PatientResponse toResponse(Patient patient);
}
