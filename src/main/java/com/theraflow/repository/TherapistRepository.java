package com.theraflow.repository;

import com.theraflow.model.Therapist;

public interface TherapistRepository {
    Therapist createProfile(Therapist therapist);
    Therapist updateProfile(Therapist therapist);
}
