package com.theraflow.therapist;


import com.theraflow.security.model.TheraflowUser;
import com.theraflow.therapist.dto.TherapistRequest;
import com.theraflow.therapist.dto.TherapistResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TherapistController.class,
        properties = "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=")
public class TherapistControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TherapistService therapistService;

    @DisplayName("Should successfully create therapist profile with valid request")
    @Test
    void createTherapistProfileSuccess() throws Exception {
        UUID randomAccountId = UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
        TheraflowUser user = new TheraflowUser(
                randomAccountId,
                "valid-email",
                "password_hash",
                true
        );
        TherapistRequest request = request();
        TherapistResponse response = response(randomAccountId);

        when(therapistService.createProfile(request, randomAccountId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/therapist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(therapistRequestJson())
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .with(csrf())
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountId").value(response.accountId()))
                .andExpect(jsonPath("$.firstName").value(response.firstName()))
                .andExpect(jsonPath("$.lastName").value(response.lastName()))
                .andExpect(jsonPath("$.licenseNumber").value(response.licenseNumber()))
                .andExpect(jsonPath("$.profTitle").value(response.profTitle()))
                .andExpect(jsonPath("$.createdAt").value(response.createdAt()))
                .andExpect(jsonPath("$.updatedAt").value(response.updatedAt()));
    }

    private TherapistRequest request() {
        return new TherapistRequest(
                "Test",
                "Therapist",
                "RPR 123",
                "DOC"
        );
    }

    private String therapistRequestJson() {
        return """
                {
                  "firstName": "Test",
                  "lastName": "Therapist",
                  "licenseNumber": "RPR 123",
                  "profTitle": "DOC"
                }
                """;
    }

    private TherapistResponse response(UUID accountId) {
        Instant now = Instant.now();
        UUID id = UUID.fromString("cc837472-3c4b-4d77-b825-a4c1cf3a1dc5");
        return new TherapistResponse(
                id,
                accountId,
                "Test",
                "Therapist",
                "RPR 123",
                "DOC",
                now,
                now
        );
    }
}
