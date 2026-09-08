package com.theraflow.model.about;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A physical address at which a therapist provides services.
 *
 * @param id              stable identifier used to edit or delete this address
 * @param street          street name
 * @param buildingNumber  building or house number
 * @param apartmentNumber optional apartment, suite, or office number
 * @param city            city or locality
 * @param region          optional state, province, or administrative region
 * @param postalCode      postal or ZIP code
 * @param countryCode     ISO 3166-1 alpha-2 country code, for example {@code PL}
 * @param phoneNumber     contact phone number for this location
 */
@NullMarked
public record Address(
        UUID id,
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
