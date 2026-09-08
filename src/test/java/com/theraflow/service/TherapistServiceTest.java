package com.theraflow.service;

import com.theraflow.TestcontainersConfiguration;
import com.theraflow.dto.AboutRequest;
import com.theraflow.dto.AddressRequest;
import com.theraflow.dto.ProfileDetailsResponse;
import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.exception.EntityNotFoundException;
import com.theraflow.mapper.DtoTherapistMapper;
import com.theraflow.model.About;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import com.theraflow.model.about.Article;
import com.theraflow.model.about.Education;
import com.theraflow.model.about.Experience;
import com.theraflow.model.about.Language;
import com.theraflow.model.Therapist;
import com.theraflow.repository.AccountRepository;
import com.theraflow.repository.TherapistRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@Import({TestcontainersConfiguration.class,
        TherapistService.class,
        DtoTherapistMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TherapistServiceTest {

    @Autowired
    private AccountRepository accountRepository;
    private UUID accountId;
    @Autowired
    private TherapistService therapistService;
    @Autowired
    private TherapistRepository therapistRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        Account account = Account.builder()
                .email("therapist@example.com")
                .passwordHash("password-hash")
                .type(AccountType.THERAPIST)
                .build();

        accountId = accountRepository.saveAndFlush(account).getId();
    }

    @Test
    void createTherapistProfile_shouldPersistMappedAndReturnCompleteResponse() {
        TherapistRequest request = requestWith("Doctor");

        TherapistResponse actual = therapistService.createProfile(request, accountId);

        assertThat(actual.accountId()).isEqualTo(accountId);
        assertThat(actual.id()).isNotNull();
        assertThat(actual.createdAt()).isNotNull();
        assertThat(actual.updatedAt()).isNotNull();
    }

    @Test
    void createProfile_shouldPreserveNullOptionalFields() {
        TherapistRequest request = requestWith(null);

        TherapistResponse actual = therapistService.createProfile(request, accountId);

        assertThat(actual.accountId()).isEqualTo(accountId);
        assertThat(actual.id()).isNotNull();
        assertThat(actual.profTitle()).isNull();
    }

    @Test
    void getProfileDetails_shouldReturnTherapistProfileWithAccountEmail() {
        TherapistResponse created = therapistService.createProfile(requestWith("Doctor"), accountId);

        ProfileDetailsResponse actual = therapistService.getProfileDetails(accountId);

        assertThat(actual.id()).isEqualTo(created.id());
        assertThat(actual.email()).isEqualTo("therapist@example.com");
        assertThat(actual.firstName()).isEqualTo("Jere");
        assertThat(actual.lastName()).isEqualTo("Miah");
        assertThat(actual.licenseNumber()).isEqualTo("LIC456");
        assertThat(actual.professionalTitle()).isEqualTo("Doctor");
    }

    @Test
    void updateTherapistProfile_shouldSuccessfullyUpdateProfile() {
        TherapistRequest request = requestWith("Mgr");
        TherapistRequest updateRequest = requestWith("Doctor");

        TherapistResponse actual = therapistService.createProfile(request, accountId);

        therapistService.updateProfile(updateRequest, accountId);

        Optional<Therapist> updated = therapistRepository.findById(actual.id());

        assertThat(updated).isNotEmpty();
        assertThat(updated.get().getId()).isEqualTo(actual.id());
        assertThat(updated.get().getProfessionalTitle()).isEqualTo("Doctor");

    }

    @Test
    void updateAbout_shouldAddAndEditCompleteAboutField() {
        TherapistResponse therapist = therapistService.createProfile(requestWith("Doctor"), accountId);
        AboutRequest initialRequest = new AboutRequest(
                "Pediatric physiotherapist supporting children from 6 months to 6 years old.",
                List.of(
                        new Language("Polish", "Native"),
                        new Language("English", "Fluent")
                ),
                List.of(new Education(
                        "University of Warsaw",
                        "Master of Psychology",
                        "Clinical Psychology",
                        LocalDate.of(2012, 10, 1),
                        LocalDate.of(2017, 6, 30)
                )),
                List.of(new Experience(
                        "Psychotherapist",
                        "TheraFlow Clinic",
                        LocalDate.of(2018, 1, 1),
                        null,
                        "Individual psychotherapy for adults"
                )),
                List.of(new Article(
                        "Understanding Anxiety",
                        URI.create("https://example.com/articles/understanding-anxiety"),
                        "TheraFlow Journal",
                        LocalDate.of(2025, 5, 10)
                ))
        );

        therapistService.updateAbout(initialRequest, accountId);
        entityManager.flush();
        entityManager.clear();

        Therapist persisted = therapistRepository.findById(therapist.id()).orElseThrow();
        assertThat(persisted.getAbout()).isEqualTo(toAbout(initialRequest));

        AboutRequest editedRequest = new AboutRequest(
                "Pediatric physiotherapist providing individualized developmental and movement therapy "
                        + "for children from 6 months to 6 years old.",
                List.of(new Language("English", "Fluent")),
                initialRequest.education(),
                initialRequest.experience(),
                initialRequest.articles()
        );

        therapistService.updateAbout(editedRequest, accountId);
        entityManager.flush();
        entityManager.clear();

        Therapist edited = therapistRepository.findById(therapist.id()).orElseThrow();
        assertThat(edited.getAbout()).isEqualTo(toAbout(editedRequest));
    }

    @Test
    void deleteAddress_shouldRemoveOnlySelectedAddressFromPersistedProfile() {
        TherapistResponse therapist = therapistService.createProfile(requestWith("Doctor"), accountId);
        AddressRequest addressToDelete = new AddressRequest(
                "Przemiarki",
                "23",
                "U12",
                "Kraków",
                "małopolskie",
                "30-384",
                "PL",
                "+48 12 345 67 89"
        );
        AddressRequest addressToKeep = new AddressRequest(
                "Długa",
                "10",
                null,
                "Kraków",
                "małopolskie",
                "31-146",
                "PL",
                "+48 12 987 65 43"
        );
        UUID deletedAddressId = therapistService.addAddress(addressToDelete, accountId);
        UUID retainedAddressId = therapistService.addAddress(addressToKeep, accountId);
        entityManager.flush();
        entityManager.clear();

        therapistService.deleteAddress(deletedAddressId, accountId);
        entityManager.flush();
        entityManager.clear();

        Therapist persisted = therapistRepository.findById(therapist.id()).orElseThrow();
        assertThat(persisted.getAddress())
                .singleElement()
                .satisfies(address -> {
                    assertThat(address.id()).isEqualTo(retainedAddressId);
                    assertThat(address.street()).isEqualTo(addressToKeep.street());
                    assertThat(address.buildingNumber()).isEqualTo(addressToKeep.buildingNumber());
                });
    }

    @Test
    void deleteAddress_whenAlreadyDeleted_shouldThrowEntityNotFoundException() {
        therapistService.createProfile(requestWith("Doctor"), accountId);
        AddressRequest request = new AddressRequest(
                "Przemiarki",
                "23",
                "U12",
                "Kraków",
                "małopolskie",
                "30-384",
                "PL",
                "+48 12 345 67 89"
        );
        UUID addressId = therapistService.addAddress(request, accountId);

        therapistService.deleteAddress(addressId, accountId);
        entityManager.flush();
        entityManager.clear();

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> therapistService.deleteAddress(addressId, accountId))
                .withMessage("Address with ID %s not found.", addressId);
    }


    private TherapistRequest requestWith(String professionalTitle) {
        return new TherapistRequest(
                "Jere",
                "Miah",
                "LIC456",
                professionalTitle
        );
    }

    private About toAbout(AboutRequest request) {
        return new About(
                request.bio(),
                request.languages(),
                request.education(),
                request.experience(),
                request.articles()
        );
    }
}
