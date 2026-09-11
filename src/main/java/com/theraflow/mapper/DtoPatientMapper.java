package com.theraflow.mapper;

import com.theraflow.dto.PatientRequest;
import com.theraflow.dto.PatientResponse;
import com.theraflow.model.patient.Patient;
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
