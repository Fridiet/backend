package com.example.fridiet.controller;

import com.example.fridiet.dto.LoginRequest;
import com.example.fridiet.dto.LoginResponse;
import com.example.fridiet.entity.User;
import com.example.fridiet.service.AuthService;
import com.example.fridiet.service.OAuthService;
import com.example.fridiet.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponentsBuilder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginClaimsTest {
    private static final String SECRET="isolated-test-signing-key-only-not-a-real-secret-12345678901234567890";
    private JwtUtil jwt;
    private final User user=User.builder().id(123L).email("test@example.invalid")
            .name("테스트 사용자").healthCondition("없음").healthGoal("건강 유지").build();

    @BeforeEach void setup() {
        jwt=new JwtUtil();
        ReflectionTestUtils.setField(jwt,"SECRET_KEY",SECRET);
    }

    private Claims claims(String token) {
        assertTrue(jwt.validateToken(token));
        assertEquals(123L,jwt.extractUserId(token));
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();
    }

    private void assertProfile(String token) {
        Claims c=claims(token);
        assertEquals(user.getName(),c.get("name",String.class));
        assertEquals(user.getEmail(),c.get("email",String.class));
        assertEquals(user.getHealthCondition(),c.get("healthCondition",String.class));
        assertEquals(user.getHealthGoal(),c.get("healthGoal",String.class));
    }

    @ParameterizedTest @ValueSource(strings={"KAKAO","GOOGLE"})
    void socialLoginCarriesNameInExistingCallback(String provider) {
        OAuthService service=mock(OAuthService.class);
        when(service.processKakaoLogin("test-code")).thenReturn(user);
        when(service.processGoogleLogin("test-code")).thenReturn(user);
        OAuthController controller=new OAuthController(service,jwt);
        ReflectionTestUtils.setField(controller,"frontendRedirectUri","fridiet://login-callback");
        var response=provider.equals("KAKAO")?controller.kakaoLogin("test-code"):controller.googleLogin("test-code");
        assertEquals(302,response.getStatusCode().value());
        var uri=response.getHeaders().getLocation();
        assertNotNull(uri);
        assertEquals("fridiet",uri.getScheme());
        assertEquals("login-callback",uri.getHost());
        assertProfile(UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("token"));
    }

    @Test void emailLoginAlsoCarriesProfile() {
        AuthService service=mock(AuthService.class);
        LoginRequest request=mock(LoginRequest.class);
        when(service.login(request)).thenReturn(user);
        var response=new AuthController(service,jwt).login(request);
        assertEquals(200,response.getStatusCode().value());
        assertProfile(((LoginResponse)response.getBody()).getToken());
    }

    @Test void oldTokensRemainValid() {
        String old=Jwts.builder().subject("123").claim("email",user.getEmail())
                .expiration(new Date(System.currentTimeMillis()+60000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        assertTrue(jwt.validateToken(old));
        assertEquals(123L,jwt.extractUserId(old));
        assertNull(claims(old).get("name"));
    }

    @Test void missingOptionalProfileDoesNotBreakLogin() {
        String token=jwt.createToken(123L,user.getEmail(),null,null,null);
        assertTrue(jwt.validateToken(token));
        assertNull(claims(token).get("name"));
    }
}
