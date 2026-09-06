package com.theraflow.controller;

import com.theraflow.dto.AccountRequest;
import com.theraflow.dto.AccountResponse;
import com.theraflow.dto.ChangePasswordRequest;
import com.theraflow.exception.CurrentPasswordMismatchException;
import com.theraflow.exception.exceptionHandler.GlobalExceptionControllerAdvice;
import com.theraflow.model.AccountType;
import com.theraflow.security.AccountPrincipalService;
import com.theraflow.security.SecurityConfig;
import com.theraflow.security.jwt.JWTService;
import com.theraflow.security.model.AccountPrincipal;
import com.theraflow.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AccountController.class,
        properties = "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
)
@Import(SecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    GlobalExceptionControllerAdvice advice;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private AccountPrincipalService accountPrincipalService;

    @MockitoBean
    private JWTService jwtService;

    @DisplayName("Should create and return new account for valid input data")
    @Test
    void register_shouldReturnNewAccount() throws Exception {
        String email = "valid_email@gmail.com";
        String password = "123StringPassword!";
        AccountType type = AccountType.THERAPIST;
        String token = "some-valid-token";
        AccountRequest request = new AccountRequest(email, password, type);
        UUID accId = UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
        Instant createdAt = Instant.parse("2026-08-18T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-08-18T10:00:00Z");
        AccountResponse response = new AccountResponse(
                accId,
                email,
                token,
                type,
                createdAt,
                updatedAt
        );

        when(accountService.createAccount(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "valid_email@gmail.com",
                                  "rawPassword": "123StringPassword!",
                                  "type": "THERAPIST"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(accId.toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.type").value(type.name()))
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()))
                .andExpect(jsonPath("$.rawPassword").doesNotExist());

        verify(accountService).createAccount(request);
    }

    @DisplayName("Should change password and return no content status when given a valid request")
    @Test
    void changePassword_successTest() throws Exception {
        UUID accountId = UUID.randomUUID();
        AccountPrincipal accountPrincipal = new AccountPrincipal(
                accountId,
                "valid-email",
                "password_hash",
                List.of(),
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
                        .with(user(accountPrincipal))
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(accountService).changePassword(req, accountId);
    }

    @DisplayName("Should return 401 and CurrentPasswordMissmatchException for wrong current password")
    @Test
    void changePassword_failureTest() throws Exception {
        UUID accountId = UUID.randomUUID();
        AccountPrincipal accountPrincipal = new AccountPrincipal(
                accountId,
                "valid-email",
                "password_hash",
                List.of(),
                true
        );

        String oldPassword = "wrong-password";
        String newPassword = "new-password";
        ChangePasswordRequest req = new ChangePasswordRequest(oldPassword, newPassword);

        doThrow(new CurrentPasswordMismatchException()).when(accountService).changePassword(req, accountId);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/accounts/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "oldPassword": "wrong-password",
                                "newPassword": "new-password"
                                }
                                """)
                        .with(user(accountPrincipal))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_MISMATCH"))
                .andExpect(jsonPath("$.message")
                        .value("The old password does not match your current password"));

        verify(accountService).changePassword(req, accountId);
    }

}
