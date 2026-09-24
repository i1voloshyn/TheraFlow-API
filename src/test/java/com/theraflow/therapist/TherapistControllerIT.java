package com.theraflow.therapist;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.theraflow.TestcontainersConfiguration;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class TherapistControllerIT {

    @Autowired
    RestTestClient restTestClient;

    @RegisterExtension
    private static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("spring", "root"));

//    @DisplayName("Create Therapist profile should successfully create profile with valid request")
//    @Test
//    @WithMockUser
//    void createTherapistProfile_success() {
//        String jwt = registerAccount_withJwtReturn();
//
//        TherapistRequest request = new TherapistRequest(
//                "Test",
//                "Therapist",
//                "RPR 123",
//                "DOC"
//        );
//
//        var response = restTestClient
//                .post()
//                .uri("/api/v1/therapist")
//                .body(request)
//                .header("Authorization", "Bearer " + jwt)
//                .exchange()
//                .expectStatus().isCreated()
//                .returnResult(TherapistResponse.class)
//                .getResponseBody();
//
//        assertThat(response.id()).isNotNull();
//        assertThat(response.createdAt()).isNotNull();
//    }
//
//    private String registerAccount_withJwtReturn() {
//        String email = "valid_email@gmail.com";
//        String password = "123StringPassword!";
//        AccountType type = AccountType.THERAPIST;
//        AccountRequest request = new AccountRequest(email, password, type);
//
//        restTestClient.post()
//                .uri("/api/v1/accounts")
//                .body(request)
//                .exchange()
//                .expectStatus()
//                .isCreated()
//                .returnResult(AccountResponse.class);
//
//        return login_withJwtReturn(new LoginRequest(email, password));
//    }
//
//    private String login_withJwtReturn(LoginRequest request) {
//        return restTestClient.post()
//                .uri("api/v1/auth/login")
//                .body(request)
//                .exchange()
//                .returnResult(LoginResponse.class)
//                .getResponseBody()
//                .access();
//
//    }

}