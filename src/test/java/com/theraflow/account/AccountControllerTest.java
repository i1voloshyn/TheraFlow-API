package com.theraflow.account;

import com.theraflow.account.dto.AccountRequest;
import com.theraflow.account.dto.AccountResponse;
import com.theraflow.account.dto.SignUpResponse;
import com.theraflow.account.model.AccountType;
import com.theraflow.authentication.AuthenticationService;
import com.theraflow.exception.InvalidCredentialsException;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.security.exception.CustomAuthenticationEntryPoint;
import com.theraflow.security.SecurityConfiguration;
import com.theraflow.security.JwtAuthenticationFilter;
import com.theraflow.application.JwtAuthTokenService;
import com.theraflow.authentication.model.LoginRequest;
import com.theraflow.security.model.TheraflowUser;
import com.theraflow.authentication.model.AuthTokenPair;
import com.theraflow.therapist.dto.ChangePasswordRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfiguration.class, CustomAuthenticationEntryPoint.class, JwtAuthenticationFilter.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtAuthTokenService jwtAuthenticationService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @DisplayName("Should create and return new account for valid input data")
    @Test
    void signUp_success() throws Exception {
        String email = "valid_email@gmail.com";
        String password = "123StringPassword!";
        AccountType type = AccountType.THERAPIST;
        AuthTokenPair tokens = new AuthTokenPair("some-valid-access", "some-valid-refresh");

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

        SignUpResponse response = new SignUpResponse(expected, tokens);

        when(accountService.signUp(request)).thenReturn(response);
        when(authenticationService.authenticate(new LoginRequest(email, password))).thenReturn(tokens);

        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
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
        SignUpResponse actual = objectMapper.readValue(result.getResponse().getContentAsString(), SignUpResponse.class);

        assertThat(actual.account()).isEqualTo(expected);
        verify(accountService).signUp(request);
    }

    @DisplayName("Should return BAD_REQUEST when registration email is invalid")
    @Test
    void signUp_error() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
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
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.params[0].name").value("email"));

        verifyNoInteractions(accountService);
    }

    @DisplayName("Should reject email verification for an unauthenticated account")
    @Test
    void emailVerification_error1() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/verify-email")
                        .param("access", "verification-access"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(accountService);
    }

    @DisplayName("Should verify email for an authenticated account")
    @Test
    void emailVerification_success() throws Exception {
        String token = "verification-access";
        TheraflowUser user = new TheraflowUser(
                UUID.randomUUID(),
                "valid-email@gmail.com",
                "password_hash",
                true
        );

        mockMvc.perform(get("/api/v1/accounts/verify-email")
                        .param("token", token)
                        .with(user(user)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(accountService).verifyEmail(token);
    }

    @DisplayName("Should change password and return no content status when given a valid request")
    @Test
    void changePassword_success() throws Exception {
        UUID accountId = UUID.randomUUID();
        TheraflowUser user = new TheraflowUser(
                accountId,
                "valid-email",
                "password_hash",
                true
        );

        String oldPassword = "old-password";
        String newPassword = "new-password";
        ChangePasswordRequest req = new ChangePasswordRequest(oldPassword, newPassword);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/accounts/change-password")
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

        verify(accountService).changePassword(req, user.getAccountId());
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
                true
        );

        String oldPassword = "wrong-password";
        String newPassword = "new-password";
        ChangePasswordRequest req = new ChangePasswordRequest(oldPassword, newPassword);

        doThrow(new InvalidCredentialsException(ErrorCode.PASSWORD_INCORRECT)).when(accountService).changePassword(req, accountId);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/accounts/change-password")
                        .header("Authorization", "valid-access")
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

        verify(accountService).changePassword(req, accountId);
    }

}
