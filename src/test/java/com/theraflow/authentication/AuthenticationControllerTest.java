package com.theraflow.authentication;

import com.theraflow.application.JwtAuthTokenProvider;
import com.theraflow.application.JwtProvider;
import com.theraflow.authentication.dto.AccountRequest;
import com.theraflow.authentication.dto.AccountResponse;
import com.theraflow.authentication.model.AccountType;
import com.theraflow.exception.InvalidCredentialsException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.security.JwtAuthenticationFilter;
import com.theraflow.security.SecurityConfiguration;
import com.theraflow.security.exception.CustomAuthenticationEntryPoint;
import com.theraflow.security.model.TheraflowUser;
import com.theraflow.therapist.dto.ChangePasswordRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthenticationController.class)
@Import({SecurityConfiguration.class})
public class AuthenticationControllerTest {

    @MockitoBean
    private AuthenticationService authenticationService;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private JwtAuthTokenProvider authTokenProvider;
    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Should create and return new account for valid input data")
    @Test
    void signUp_success() throws Exception {
        String email = "valid_email@gmail.com";
        String password = "123StringPassword!";
        AccountType type = AccountType.THERAPIST;

        AccountRequest request = new AccountRequest(email, password, type);
        UUID accId = UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
        Instant createdAt = Instant.parse("2026-08-18T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-08-18T10:00:00Z");
        AccountResponse expected = new AccountResponse(
                accId,
                email,
                type,
                createdAt,
                updatedAt
        );

        when(authenticationService.signUp(request)).thenReturn(expected);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "valid_email@gmail.com",
                                  "rawPassword": "123StringPassword!",
                                  "type": "THERAPIST"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        AccountResponse actual = objectMapper.readValue(result.getResponse().getContentAsString(), AccountResponse.class);

        assertThat(actual).isEqualTo(expected);
        verify(authenticationService).signUp(request);
    }

    @DisplayName("Should return BAD_REQUEST when registration email is invalid")
    @Test
    void signUp_error() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "rawPassword": "123StringPassword!",
                                  "type": "THERAPIST"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.VALIDATION_FAILED.name()))
                .andExpect(jsonPath("$.params[0].name").value("email"));

        verifyNoInteractions(authenticationService);
    }

    @DisplayName("Should verify email for an authenticated account")
    @Test
    void emailVerification_success() throws Exception {
        String token = "verification-access";
        mockMvc.perform(get("/api/v1/auth/verify-email")
                        .param("token", token)
                      )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(authenticationService).verifyEmail(token);
    }

    @DisplayName("Should change password and return no content status when given a valid request")
    @Test
    void changePassword_success() throws Exception {
        UUID accountId = UUID.randomUUID();
        TheraflowUser user = new TheraflowUser(
                accountId,
                "valid-email",
                "password_hash",
                true,
                AccountType.THERAPIST
        );

        String oldPassword = "old-password";
        String newPassword = "new-password";
        ChangePasswordRequest req = new ChangePasswordRequest(oldPassword, newPassword);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "oldPassword": "old-password",
                                "newPassword": "new-password"
                                }
                                """)
                        .with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())))
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(authenticationService).changePassword(req, user.getAccountId());
    }

    @DisplayName("Should return 401 and InvalidAccessException with PASSWORD_INCORRECT code for wrong current password")
    @Test
    void changePassword_error() throws Exception {
        ErrorCode expected = ErrorCode.PASSWORD_INCORRECT;
        UUID accountId = UUID.randomUUID();
        TheraflowUser user = new TheraflowUser(
                accountId,
                "valid-email",
                "password_hash",
                true,
                AccountType.THERAPIST
        );

        String oldPassword = "wrong-password";
        String newPassword = "new-password";
        ChangePasswordRequest req = new ChangePasswordRequest(oldPassword, newPassword);

        doThrow(new InvalidCredentialsException(ErrorCode.PASSWORD_INCORRECT)).when(authenticationService).changePassword(req, accountId);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "oldPassword": "wrong-password",
                                "newPassword": "new-password"
                                }
                                """)
                        .with(user(user))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(expected.name()));

        verify(authenticationService).changePassword(req, accountId);
    }
}
