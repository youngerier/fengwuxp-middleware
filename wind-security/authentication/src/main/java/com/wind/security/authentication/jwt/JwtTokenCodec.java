package com.wind.security.authentication.jwt;

import com.alibaba.fastjson2.JSON;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.wind.common.exception.AssertUtils;
import com.wind.security.authentication.WindAuthenticationToken;
import com.wind.security.authentication.WindAuthenticationUser;
import com.wind.sequence.SequenceGenerator;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * jwt token 编码解码
 *
 * @author wuxp
 * @date 2023-09-24 16:59
 **/
public final class JwtTokenCodec {

    private static final String AUTHENTICATION_VARIABLE_NAME = "authentication";

    private static final String JWT_AUTH_KEY_ID = "Jwt-Auth-Codec-Wind";

    private final JwtProperties properties;

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder jwtDecoder;

    private final JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();

    public JwtTokenCodec(JwtProperties properties) {
        this.properties = properties;
        RSAKey rsaKey = generateRsaKey(properties.getKeyPair());
        this.jwtEncoder = buildJwtEncoder(rsaKey);
        this.jwtDecoder = buildJwtDecoder(rsaKey);
    }

    /**
     * jwt parse token
     * 注意：如果 token 过期了，将抛出 {@link JwtExpiredException} 异常
     *
     * @param jwtToken jwt token
     * @return jwt token payload
     */
    @NotNull
    public WindAuthenticationToken parse(@NotBlank String jwtToken) {
        AssertUtils.hasText(jwtToken, "argument token must not null");
        Jwt jwt = jwtDecoder.decode(jwtToken);
        Map<String, Object> claims = jwt.getClaims();
        WindAuthenticationUser user = JSON.to(WindAuthenticationUser.class, claims.get(AUTHENTICATION_VARIABLE_NAME));
        Instant expiresAt = Objects.requireNonNull(jwt.getExpiresAt(), "token expire must not null");
        AssertUtils.state(expiresAt.isAfter(Instant.now()), () -> new JwtExpiredException("token is expired"));
        return new WindAuthenticationToken(jwt.getId(), jwtToken, jwt.getSubject(), user, expiresAt.toEpochMilli());
    }

    /**
     * 生成用户 token
     *
     * @param user 用户信息
     * @return 用户 token
     */
    @NotNull
    public WindAuthenticationToken encoding(WindAuthenticationUser user) {
        return encoding(user, properties.getEffectiveTime());
    }

    /**
     * 生成用户 token
     *
     * @param user          用户信息
     * @param effectiveTime token 有效期
     * @return 用户 token
     */
    public WindAuthenticationToken encoding(WindAuthenticationUser user, Duration effectiveTime) {
        Jwt jwt = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        jwsHeader,
                        newJwtBuilder(String.valueOf(user.getId()), effectiveTime)
                                .claim(AUTHENTICATION_VARIABLE_NAME, user)
                                .id(DigestUtils.sha256Hex(String.format("%s@%s", SequenceGenerator.randomNumeric(512), user)))
                                .build()
                )
        );
        return new WindAuthenticationToken(jwt.getId(), jwt.getTokenValue(), jwt.getSubject(), user,
                Objects.requireNonNull(jwt.getExpiresAt()).toEpochMilli());
    }

    /**
     * 生成 refresh token
     *
     * @param userId 用户 id
     * @return refresh token
     */
    @NotNull
    public WindAuthenticationToken encodingRefreshToken(String userId) {
        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,
                newJwtBuilder(userId, properties.getRefreshEffectiveTime())
                        // refresh token id 增加 refresh@ 前缀
                        .id("refresh@" + DigestUtils.sha256Hex(String.format("%s#%s", SequenceGenerator.randomNumeric(512), userId)))
                        .build()));
        return new WindAuthenticationToken(jwt.getId(), jwt.getTokenValue(), jwt.getSubject(), null,
                Objects.requireNonNull(jwt.getExpiresAt()).toEpochMilli());
    }

    /**
     * 解析 验证 refresh token
     * 注意：如果 token 过期了，将抛出 {@link JwtExpiredException} 异常
     *
     * @param refreshToken refresh token
     * @return 用户 id
     */
    @NotNull
    public WindAuthenticationToken parseRefreshToken(@NotBlank String refreshToken) {
        AssertUtils.hasText(refreshToken, "argument token must not null");
        Jwt jwt = jwtDecoder.decode(refreshToken);
        Instant expiresAt = Objects.requireNonNull(jwt.getExpiresAt(), "refresh token expire must not null");
        AssertUtils.notNull(expiresAt, "jwt token expireAt must not null");
        AssertUtils.state(expiresAt.isAfter(Instant.now()), () -> new JwtExpiredException("refresh token is expired"));
        return new WindAuthenticationToken(jwt.getId(), refreshToken, jwt.getSubject(), null, expiresAt.toEpochMilli());
    }

    private JwtClaimsSet.Builder newJwtBuilder(String userId, Duration effectiveTime) {
        return JwtClaimsSet.builder()
                .expiresAt(Instant.now().plusSeconds(effectiveTime.getSeconds()))
                .audience(Collections.singletonList(properties.getAudience()))
                .issuer(properties.getIssuer())
                .subject(userId);
    }

    private JwtDecoder buildJwtDecoder(RSAKey rsaKey) {
        DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, new ImmutableJWKSet<>(new JWKSet(rsaKey))));
        return new NimbusJwtDecoder(processor);
    }

    private JwtEncoder buildJwtEncoder(RSAKey rsaKey) {
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    private RSAKey generateRsaKey(KeyPair keyPair) {
        // https://github.com/spring-projects/spring-security/blob/main/oauth2/oauth2-jose/src/test/java/org/springframework/security/oauth2/jose/TestKeys.java
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .keyID(JWT_AUTH_KEY_ID)
                .build();
    }

}
