package com.wind.security.authentication.jwt;

import com.wind.common.exception.BaseException;
import com.wind.security.authentication.AuthenticationTokenCodecService;
import com.wind.security.authentication.AuthenticationTokenUserMap;
import com.wind.security.authentication.WindAuthenticationToken;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wuxp
 * @date 2025-05-20 15:15
 **/
class DefaultJwtAuthenticationTokenCodecServiceTests {

    private final AuthenticationTokenUserMap tokenUserMap = getTokenUserMap();

    private final AuthenticationTokenCodecService tokenCodecService = createCodeService(tokenUserMap);

    @Test
    void testGenerateToken() {
        WindAuthenticationToken token = tokenCodecService.generateToken("1", RandomStringUtils.randomAlphabetic(12), Collections.emptyMap());
        WindAuthenticationToken parsed = tokenCodecService.parseAndValidateToken(token.getTokenValue());
        Assertions.assertEquals(token.getId(), parsed.getId());
        Assertions.assertEquals(token.getSubject(), parsed.getSubject());
    }

    @Test
    void testParseAndValidateTokenWithException() {
        WindAuthenticationToken token = tokenCodecService.generateToken("1", RandomStringUtils.randomAlphabetic(12), Collections.emptyMap());
        tokenUserMap.remove(token.getId());
        BaseException exception = Assertions.assertThrows(BaseException.class, () -> tokenCodecService.parseAndValidateToken(token.getTokenValue()));
        Assertions.assertEquals("invalid access token", exception.getMessage());
    }

    @Test
    void testGenerateRefreshToken() {
        WindAuthenticationToken token = tokenCodecService.generateRefreshToken("1");
        WindAuthenticationToken parsed = tokenCodecService.parseAndValidateRefreshToken(token.getTokenValue());
        Assertions.assertEquals(token.getId(), parsed.getId());
        Assertions.assertEquals(token.getSubject(), parsed.getSubject());
    }

    @Test
    void testParseAndValidateRefreshTokenWithException() {
        WindAuthenticationToken token = tokenCodecService.generateRefreshToken("1");
        tokenCodecService.revokeRefreshToken(token.getTokenValue());
        BaseException exception = Assertions.assertThrows(BaseException.class, () -> tokenCodecService.parseAndValidateRefreshToken(token.getTokenValue()));
        Assertions.assertEquals("invalid refresh token", exception.getMessage());
    }

    private AuthenticationTokenCodecService createCodeService(AuthenticationTokenUserMap tokenStore) {
        JwtTokenCodec tokenCodec = new JwtTokenCodec(JwtTokenCodecTests.jwtProperties(Duration.ofHours(1)));
        return new DefaultJwtAuthenticationTokenCodecService(tokenCodec, tokenStore);
    }

    private AuthenticationTokenUserMap getTokenUserMap() {
        return new AuthenticationTokenUserMap() {

            private final Map<String, String> caches = new HashMap<>();

            @Override
            public void put(String tokenId, String userId) {
                caches.put(tokenId, userId);
            }

            @Override
            public String getUserId(String tokenId) {
                return caches.get(tokenId);
            }

            @Override
            public void remove(String tokenId) {
                caches.remove(tokenId);
            }
        };
    }
}
