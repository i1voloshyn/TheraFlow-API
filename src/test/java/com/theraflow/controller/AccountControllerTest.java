package com.theraflow.controller;

import com.theraflow.dto.AccountRequest;
import com.theraflow.dto.AccountResponse;
import com.theraflow.dto.ChangePasswordRequest;
import com.theraflow.model.AccountType;
import com.theraflow.security.jwt.JwtAuthenticationFilter;
import com.theraflow.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AccountController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

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
                                """))
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

//    @Test
//    @WithUserDetails
//    void changePassword_successTest() {
//        UUID id = UUID.randomUUID();
//        String oldPassword = "old-password";
//        String newPassword = "new-password";
//        ChangePasswordRequest req = new ChangePasswordRequest(oldPassword, newPassword);
//        RestTestClient client = RestTestClient.bindTo(mockMvc).build();
//
//        client.patch().uri("/api/v1/accounts/change-password")
//                .body(req)
//                .header("Authentication", "Bearer 123456")
//                .exchange()
//                .expectStatus().isNoContent();
//
//        verify(accountService).changePassword(req, id);
//    }

}
