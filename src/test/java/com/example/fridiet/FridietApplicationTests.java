package com.example.fridiet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.fridiet.config.FirebaseConfig;
import com.example.fridiet.service.ExpirationSchedulerService;
import com.example.fridiet.service.ExpirationNotificationScheduler;
import com.example.fridiet.service.FcmNotificationService;
import software.amazon.awssdk.services.s3.S3Client;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.config.location=classpath:/application-isolated-test.yml",
        "spring.datasource.url=jdbc:h2:mem:application_context;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class FridietApplicationTests {
    @MockitoBean FirebaseConfig firebaseConfig;
    @MockitoBean S3Client s3Client;
    @MockitoBean FcmNotificationService notifications;
    @MockitoBean ExpirationSchedulerService expiration;
    @MockitoBean ExpirationNotificationScheduler expirationNotifications;
    @Autowired MockMvc mvc;

	@Test
	void contextLoads() {
	}

    @Test void publicAuthPathsAndProtectedApisRemainSeparated() throws Exception {
        mvc.perform(options("/api/calendar/meals")).andExpect(status().isOk());
        mvc.perform(get("/login/oauth2/code/google")).andExpect(status().isBadRequest());
        mvc.perform(get("/link/oauth2/code/kakao")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/email/verify")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/calendar/meals")).andExpect(status().isUnauthorized());
    }

}
