package com.wind.security.authentication.jwt;

import com.wind.common.WindConstants;
import com.wind.common.exception.AssertUtils;
import com.wind.security.authentication.AuthenticationTokenCodecService;
import com.wind.security.authentication.AuthenticationTokenUserMap;
import com.wind.security.authentication.WindAuthenticationToken;
import com.wind.security.authentication.WindAuthenticationUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Objects;

/**
 * @author wuxp
 * @date 2025-05-20 14:38
 **/
@AllArgsConstructor
@Slf4j
public class DefaultJwtAuthenticationTokenCodecService implements AuthenticationTokenCodecService {

    private final JwtTokenCodec jwtTokenCodec;

    private final AuthenticationTokenUserMap delegate;

    @Override
    public WindAuthenticationToken generateToken(WindAuthenticationUser user, Duration ttl) {
        WindAuthenticationToken result = ttl == null ? jwtTokenCodec.encoding(user) : jwtTokenCodec.encoding(user, ttl);
        userTokenWrapper().put(String.valueOf(user.getId()), result.getId());
        return result;
    }

    @Override
    public WindAuthenticationToken generateRefreshToken(String userId) {
        WindAuthenticationToken result = jwtTokenCodec.encodingRefreshToken(userId);
        refreshTokenUserWrapper().put(userId, result.getId());
        return result;
    }

    @Override
    public WindAuthenticationToken parseAndValidateToken(String accessToken) {
        WindAuthenticationToken result = jwtTokenCodec.parse(accessToken);
        String tokenId = userTokenWrapper().getTokenId(result.getSubject());
        AssertUtils.hasText(tokenId, "invalid access token user");
        AssertUtils.isTrue(Objects.equals(tokenId, result.getId()), "invalid access token");
        return result;
    }

    @Override
    public WindAuthenticationToken parseAndValidateRefreshToken(String refreshToken) {
        WindAuthenticationToken result = jwtTokenCodec.parseRefreshToken(refreshToken);
        String tokenId = refreshTokenUserWrapper().getTokenId(result.getSubject());
        AssertUtils.hasText(tokenId, "invalid refresh token user");
        AssertUtils.isTrue(Objects.equals(tokenId, result.getId()), "invalid refresh token");
        return result;
    }

    @Override
    public void revokeAllToken(String userId) {
        userTokenWrapper().removeTokenId(userId);
        refreshTokenUserWrapper().removeTokenId(userId);
    }

    private AuthenticationTokenUserMap userTokenWrapper() {
        return new AuthenticationTokenUserMapWrapper(delegate, WindConstants.EMPTY);
    }

    private AuthenticationTokenUserMap refreshTokenUserWrapper() {
        return new AuthenticationTokenUserMapWrapper(delegate, "refresh_");
    }

    @AllArgsConstructor
    private static class AuthenticationTokenUserMapWrapper implements AuthenticationTokenUserMap {

        private final AuthenticationTokenUserMap delegate;

        private final String prefix;

        @Override
        public void put(String userId, String tokenId) {
            delegate.put(prefix + userId, tokenId);
        }

        @Override
        public String getTokenId(String userId) {
            return delegate.getTokenId(prefix + userId);
        }

        @Override
        public void removeTokenId(String userId) {
            delegate.removeTokenId(prefix + userId);
        }
    }
}
