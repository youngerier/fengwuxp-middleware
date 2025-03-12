package com.wind.security.authentication.jwt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Base64Utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;

class JwtTokenCodecTest {

    private final JwtTokenCodec jwtTokenCodec = new JwtTokenCodec(jwtProperties(Duration.ofMinutes(1)));

    @Test
    void testCodecUserToken() {
        JwtUser user = new JwtUser(1L, "", Collections.emptyMap());
        JwtToken token = jwtTokenCodec.encoding(user);
        JwtUser result = jwtTokenCodec.parse(token.getTokenValue()).getUser();
        Assertions.assertEquals(user, result);
    }

    @Test
    void testCodecUserTokenExpired() throws Exception {
        JwtTokenCodec codec = new JwtTokenCodec(jwtProperties(Duration.ofMillis(100)));
        JwtUser user = new JwtUser(1L, "", Collections.emptyMap());
        JwtToken token = codec.encoding(user);
        Thread.sleep(101);
        JwtExpiredException exception = Assertions.assertThrows(JwtExpiredException.class, () -> codec.parse(token.getTokenValue()));
        Assertions.assertEquals("token is expired", exception.getMessage());
    }

    @Test
    void testCodecRefreshToken() {
        JwtToken token = jwtTokenCodec.encodingRefreshToken(1L);
        Assertions.assertEquals(1L, token.getSubjectAsLong());
    }

    @Test
    void testRefreshTokenExpired() throws Exception {
        JwtTokenCodec codec = new JwtTokenCodec(jwtProperties(Duration.ofMillis(100)));
        JwtToken token = codec.encodingRefreshToken(1L);
        Thread.sleep(101);
        String tokenValue = token.getTokenValue();
        JwtExpiredException exception = Assertions.assertThrows(JwtExpiredException.class, () -> codec.parseRefreshToken(tokenValue));
        Assertions.assertEquals("refresh token is expired", exception.getMessage());
    }


    private JwtProperties jwtProperties(Duration duration) {
        KeyPair keyPair = genKeyPir();
        String publicKey = Base64Utils.encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64Utils.encodeToString(keyPair.getPrivate().getEncoded());
        JwtProperties result = new JwtProperties();
        if (duration != null) {
            result.setEffectiveTime(duration);
            result.setRefreshEffectiveTime(duration);
        }
        result.setIssuer("test");
        result.setAudience("test");
        result.setRsaPublicKey(publicKey);
        result.setRsaPrivateKey(privateKey);
        return result;
    }

    private KeyPair genKeyPir() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}