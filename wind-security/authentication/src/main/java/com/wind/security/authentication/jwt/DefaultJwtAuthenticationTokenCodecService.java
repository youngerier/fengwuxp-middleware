package com.wind.security.authentication.jwt;

import com.wind.common.exception.AssertUtils;
import com.wind.security.authentication.AuthenticationTokenCodecService;
import com.wind.security.authentication.AuthenticationTokenUserMap;
import com.wind.security.authentication.WindAuthenticationToken;
import com.wind.security.authentication.WindAuthenticationUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author wuxp
 * @date 2025-05-20 14:38
 **/
@AllArgsConstructor
@Slf4j
public class DefaultJwtAuthenticationTokenCodecService implements AuthenticationTokenCodecService {

    private final JwtTokenCodec jwtTokenCodec;

    private final AuthenticationTokenUserMap tokenUserMap;

    @Override
    public WindAuthenticationToken generateToken(String userId, String userName, Map<String, Object> attributes) {
        AssertUtils.notNull(userId, "argument userId must not null");
        AssertUtils.hasText(userName, "argument userName must not null");
        WindAuthenticationToken result = jwtTokenCodec.encoding(new WindAuthenticationUser(userId, userName, attributes == null ?
                Collections.emptyMap() : attributes));
        tokenUserMap.put(result.getId(), userId);
        return result;
    }

    @Override
    public WindAuthenticationToken generateRefreshToken(String userId) {
        WindAuthenticationToken result = jwtTokenCodec.encodingRefreshToken(userId);
        tokenUserMap.put(result.getId(), userId);
        return result;
    }

    @Override
    public WindAuthenticationToken parseAndValidateToken(String accessToken) {
        WindAuthenticationToken result = jwtTokenCodec.parse(accessToken);
        String userId = tokenUserMap.getUserId(result.getId());
        AssertUtils.hasText(userId, "invalid access token");
        AssertUtils.isTrue(Objects.equals(userId, result.getSubject()), "invalid access token user");
        return result;
    }

    @Override
    public WindAuthenticationToken parseAndValidateRefreshToken(String refreshToken) {
        WindAuthenticationToken result = jwtTokenCodec.parseRefreshToken(refreshToken);
        String userId = tokenUserMap.getUserId(result.getId());
        AssertUtils.hasText(userId, "invalid refresh token");
        AssertUtils.isTrue(Objects.equals(userId, result.getSubject()), "invalid refresh token user");
        return result;
    }

    @Override
    public void revokeAccessToken(String accessToken) {
        tokenUserMap.remove(jwtTokenCodec.parse(accessToken).getId());
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        tokenUserMap.remove(jwtTokenCodec.parseRefreshToken(refreshToken).getId());
    }
}
