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
        // 1. 手动创建实例 (不依赖 Spring)
        jwtTokenProvider = new JwtTokenProvider();

        // 2. 只有通过反射，才能给 private 字段赋值
        // 注意：Secret 必须足够长 (至少32个字符)，否则 HMAC-SHA 算法会报错
        String secret = "AAAAAAAABBBBBBBCCCCCCCCDDDDDDDDEEEEEEEE";
        long expiration = 1000 * 60 * 60;

        // 使用 Spring 的工具类强行注入值
        //
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
        // 1. 验证这个 Token 是有效的 (调用 validateToken)
        assertTrue(jwtTokenProvider.validateToken(token));
        // 2. 验证从 Token 里解出来的用户名，依然是 "TestUsername" (调用 getUsername)
        assertEquals(testUser, jwtTokenProvider.getUsername(token));
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.generateToken(null));
    }

    @Test
    void testValidateToken_ReturnFalse_WhenTokenExpired() {
        // 不需要重新设置 Secret，沿用 setUp() 里的就行
        // 只修改过期时间为 -1000 毫秒 (负数)
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
        // --- 1. Arrange: 制造假 Token (偷梁换柱) ---

        // 准备一把错误的密钥 (模拟黑客的密钥)
        // 注意：根据 HS256 算法要求，这个字符串也得足够长(>=32字节)
        String wrongSecret = "ThisIsAWrongSecretKeyForHackingTestOnly123456";
        Key wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes());

        // 用这把错误的密钥，手动生成一个 Token
        String tamperedToken = Jwts.builder()
                .setSubject("HackerUser")
                .signWith(wrongKey) // <--- 关键点：用错误的 Key 签名
                .compact();

        // --- 2. Act & Assert: 验证能否识破 ---
        // 你的 jwtTokenProvider 拿着正确的 Key 去解这个 Token，
        // 发现签名对不上，必须抛出 SignatureException
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
        // 1. Arrange: 准备一个用户
        String testUser = "HappyUser";

        // 2. Act 1: 生成 Token
        String token = jwtTokenProvider.generateToken(testUser);

        // Assert 1: 确保生成出来的不是垃圾
        assertNotNull(token);
        assertFalse(token.isEmpty()); // 或者 assertTrue(token.length() > 0);

        // 3. Act 2 & Assert 2: 验证 Token 是否有效
        // (刚生成的 Token，肯定应该是有效的)
        assertTrue(jwtTokenProvider.validateToken(token), "刚生成的 Token 应该是有效的");

        // 4. Act 3 & Assert 3: 解析 Token 里的内容
        // (存进去是 HappyUser，取出来也得是 HappyUser)
        String extractedUsername = jwtTokenProvider.getUsername(token);
        assertEquals(testUser, extractedUsername, "解析出的用户名应该和生成时一致");
    }

}