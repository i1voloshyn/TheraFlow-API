package com.theraflow.service;

import com.theraflow.TestcontainersConfiguration;
import com.theraflow.dto.AboutRequest;
import com.theraflow.dto.TherapistRequest;
import com.theraflow.dto.TherapistResponse;
import com.theraflow.mapper.DtoTherapistMapper;
import com.theraflow.model.About;
import com.theraflow.model.Account;
import com.theraflow.model.AccountType;
import com.theraflow.model.Article;
import com.theraflow.model.Education;
import com.theraflow.model.Experience;
import com.theraflow.model.Language;
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
    void createTherapistProfile_shouldPersistMappedTherapistAndReturnCompleteResponse() {
        TherapistRequest request = requestWith("Doctor");

        TherapistResponse actual = therapistService.createTherapistProfile(request, accountId);

        assertThat(actual.accountId()).isEqualTo(accountId);
        assertThat(actual.id()).isNotNull();
        assertThat(actual.createdAt()).isNotNull();
        assertThat(actual.updatedAt()).isNotNull();
    }

    @Test
    void createTherapistProfile_shouldPreserveNullOptionalFields() {
        TherapistRequest request = requestWith(null);

        TherapistResponse actual = therapistService.createTherapistProfile(request, accountId);

        assertThat(actual.accountId()).isEqualTo(accountId);
        assertThat(actual.id()).isNotNull();
        assertThat(actual.profTitle()).isNull();
    }

    @Test
    void updateTherapistProfile_shouldSuccessfullyUpdateProfile() {
        TherapistRequest request = requestWith("Mgr");
        TherapistRequest updateRequest = requestWith("Doctor");

        TherapistResponse actual = therapistService.createTherapistProfile(request, accountId);

        therapistService.updateTherapistProfile(updateRequest, actual.id());

        Optional<Therapist> updated = therapistRepository.findById(actual.id());

        assertThat(updated).isNotEmpty();
        assertThat(updated.get().getId()).isEqualTo(actual.id());
        assertThat(updated.get().getProfessionalTitle()).isEqualTo("Doctor");

    }

    @Test
    void updateTherapistAbout_shouldAddAndEditCompleteAboutField() {
        TherapistResponse therapist = therapistService.createTherapistProfile(requestWith("Doctor"), accountId);
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

        therapistService.updateTherapistAbout(initialRequest, accountId);
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

        therapistService.updateTherapistAbout(editedRequest, accountId);
        entityManager.flush();
        entityManager.clear();

        Therapist edited = therapistRepository.findById(therapist.id()).orElseThrow();
        assertThat(edited.getAbout()).isEqualTo(toAbout(editedRequest));
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
