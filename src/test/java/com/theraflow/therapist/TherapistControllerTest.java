package com.theraflow.therapist;


import com.theraflow.account.model.AccountType;
import com.theraflow.application.JwtAuthTokenProvider;
import com.theraflow.application.JwtProvider;
import com.theraflow.security.JwtAuthenticationFilter;
import com.theraflow.security.SecurityConfiguration;
import com.theraflow.security.exception.CustomAuthenticationEntryPoint;
import com.theraflow.security.model.TheraflowUser;
import com.theraflow.therapist.dto.TherapistRequest;
import com.theraflow.therapist.dto.TherapistResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TherapistController.class,
        properties = "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
)
@Import({SecurityConfiguration.class,
        JwtAuthenticationFilter.class,
        CustomAuthenticationEntryPoint.class})
public class TherapistControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    private TherapistService therapistService;
    @MockitoBean
    private CustomAuthenticationEntryPoint entryPoint;
    @MockitoBean
    private JwtProvider jwtService;
    @MockitoBean
    private JwtAuthTokenProvider authTokenService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @DisplayName("Should successfully create therapist profile with valid request")
    @Test
    void createTherapistProfileSuccess() throws Exception {
        UUID randomAccountId = UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
        String token = "valid_token";
        TheraflowUser user = new TheraflowUser(
                randomAccountId,
                "valid-email",
                "password_hash",
                true,
                AccountType.THERAPIST
        );
        TherapistRequest request = request();
        TherapistResponse response = response(randomAccountId);

        when(therapistService.createProfile(request, randomAccountId)).thenReturn(response);
        when(authTokenService.extractUserDetails(token)).thenReturn(user);

        mockMvc.perform(post("/api/v1/therapist")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(therapistRequestJson())
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountId").value(response.accountId().toString()))
                .andExpect(jsonPath("$.firstName").value(response.firstName()))
                .andExpect(jsonPath("$.lastName").value(response.lastName()))
                .andExpect(jsonPath("$.licenseNumber").value(response.licenseNumber()))
                .andExpect(jsonPath("$.profTitle").value(response.profTitle()))
                .andExpect(jsonPath("$.createdAt").value(response.createdAt().toString()))
                .andExpect(jsonPath("$.updatedAt").value(response.updatedAt().toString()));
    }

    @DisplayName("Should fail to create therapist profile with invalid account type")
    @Test
    void createTherapistProfileError() throws Exception {
        UUID randomAccountId = UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
        TheraflowUser user = new TheraflowUser(
                randomAccountId,
                "valid-email",
                "password_hash",
                true,
                AccountType.GUARDIAN
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
                .andExpect(status().isForbidden());
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
