package com.xjfdy.cuisine_backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import io.jsonwebtoken.security.SignatureException;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        String secret = "AAAAAAAABBBBBBBCCCCCCCCDDDDDDDDEEEEEEEE";
        long expiration = 1000 * 60 * 60;

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationDate", expiration);

    }

    // test validatetoken() below
    @Test
    void testValidateToken_ReturnTrue() {
        String testUser = "TestUsername";
        String token = jwtTokenProvider.generateToken(testUser);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(testUser, jwtTokenProvider.getUsername(token));
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.generateToken(null));
    }

    @Test
    void testValidateToken_ReturnFalse_WhenTokenExpired() {
        long expiredDate = -1000;
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationDate", expiredDate);

        String expiredUser = "ExpiredUsername";
        String expiredToken = jwtTokenProvider.generateToken(expiredUser);

        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    void testValidateToken_ReturnFalse_WhenTokenInvalid() {
        String invalidToken = "aqew";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_ReturnFalse_WhenTokenEmpty() {
        String emptyToken = "";
        assertFalse(jwtTokenProvider.validateToken(emptyToken));
    }

    //Test getUsername() below
    @Test
    void testGetUsername_WhenTokenValid() {
        String user = "UserA";
        String token = jwtTokenProvider.generateToken(user);

        assertEquals("UserA", jwtTokenProvider.getUsername(token));
    }

    @Test
    void testGetUsername_ThrowException_WhenTokenExpired() {
        long expire = -1000;
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationDate", expire);

        String user = "User";
        String expiredToken = jwtTokenProvider.generateToken(user);
        assertThrows(ExpiredJwtException.class, () -> jwtTokenProvider.getUsername(expiredToken));
    }

    @Test
    void testGetUsername_ThrowException_WhenTokenSignedWithWrongKey() {
        String wrongSecret = "ThisIsAWrongSecretKeyForHackingTestOnly123456";
        Key wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes());

        String tamperedToken = Jwts.builder()
                .setSubject("HackerUser")
                .signWith(wrongKey)
                .compact();

        assertThrows(SignatureException.class,
                () -> jwtTokenProvider.getUsername(tamperedToken));
    }

    @Test
    void testGetUsername_ThrowException_WhenTokenNull() {
        String token = null;
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.getUsername(token));
    }

    @Test
    void testTokenLifecycle_Success() {
        String testUser = "HappyUser";

        String token = jwtTokenProvider.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        assertTrue(jwtTokenProvider.validateToken(token), "刚生成的 Token 应该是有效的");

        String extractedUsername = jwtTokenProvider.getUsername(token);
        assertEquals(testUser, extractedUsername, "解析出的用户名应该和生成时一致");
    }

}