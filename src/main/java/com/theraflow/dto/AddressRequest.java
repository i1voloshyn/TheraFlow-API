package com.theraflow.dto;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record AddressRequest(
        String street,
        String buildingNumber,
        @Nullable String apartmentNumber,
        String city,
        @Nullable String region,
        String postalCode,
        String countryCode,
        String phoneNumber
) {
}
