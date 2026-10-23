package com.grosshaeuser.olmp.security.service;

import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.security.principal.MemberPrincipal;
import com.grosshaeuser.olmp.security.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-signing-key-at-least-32-bytes-long-for-hmac";
    private static final String ISSUER = "test-issuer";
    private final JwtService service = new JwtService(new JwtProperties(ISSUER, SECRET, Duration.ofMinutes(17)));

    @Test
    void createsSignedTokenWithIdentityAuthoritiesIssuerAndConfiguredLifetime() {
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        String token = service.createAccessToken(principal("ROLE_MEMBER", "BORROW_MEDIA"));

        // Verify independently of JwtService's parsing method.
        Claims claims = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getSubject()).isEqualTo("member@example.com");
        assertThat(claims.get("accountId", Number.class).longValue()).isEqualTo(11L);
        assertThat(claims.get("memberId", Number.class).longValue()).isEqualTo(22L);
        assertThat(claims.get("authorities")).isEqualTo(List.of("ROLE_MEMBER", "BORROW_MEDIA"));
        assertThat(claims.getIssuedAt().toInstant()).isBetween(before, Instant.now());
        assertThat(Duration.between(claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant()))
                .isEqualTo(Duration.ofMinutes(17));
    }

    @Test
    void parsesTokenCreatedByService() {
        Claims claims = service.parseAndValidate(service.createAccessToken(principal("ROLE_MEMBER")));

        assertThat(claims.getSubject()).isEqualTo("member@example.com");
        assertThat(claims.get("authorities")).isEqualTo(List.of("ROLE_MEMBER"));
    }

    @Test
    void tokenWithNoAuthoritiesHasEmptyAuthorityClaim() {
        Claims claims = service.parseAndValidate(service.createAccessToken(principal()));

        assertThat(claims.get("authorities")).isEqualTo(List.of());
    }

    @Test
    void rejectsExpiredToken() {
        String token = Jwts.builder().issuer(ISSUER).subject("member@example.com")
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();

        assertThatThrownBy(() -> service.parseAndValidate(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        String token = Jwts.builder().issuer(ISSUER).subject("member@example.com")
                .signWith(Keys.hmacShaKeyFor("another-signing-key-at-least-32-bytes-long-for-hmac"
                        .getBytes(StandardCharsets.UTF_8))).compact();

        assertThatThrownBy(() -> service.parseAndValidate(token)).isInstanceOf(SignatureException.class);
    }

    @Test
    void rejectsTokenFromDifferentIssuer() {
        String token = Jwts.builder().issuer("other-issuer").subject("member@example.com")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();

        assertThatThrownBy(() -> service.parseAndValidate(token)).isInstanceOf(IncorrectClaimException.class);
    }

    @Test
    void rejectsTokenWithoutIssuer() {
        String token = Jwts.builder().subject("member@example.com")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();

        assertThatThrownBy(() -> service.parseAndValidate(token)).isInstanceOf(MissingClaimException.class);
    }

    @Test
    void rejectsUnsignedToken() {
        String token = Jwts.builder().issuer(ISSUER).subject("member@example.com").compact();

        assertThatThrownBy(() -> service.parseAndValidate(token)).isInstanceOf(JwtException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"not-a-jwt", "a.b.c"})
    void rejectsMissingOrMalformedToken(String token) {
        assertThatThrownBy(() -> service.parseAndValidate(token))
                .isInstanceOfAny(JwtException.class, IllegalArgumentException.class);
    }

    @Test
    void rejectsSigningSecretThatIsTooShort() {
        JwtService insecureService = new JwtService(new JwtProperties(ISSUER, "short", Duration.ofMinutes(17)));

        assertThatThrownBy(() -> insecureService.createAccessToken(principal("ROLE_MEMBER")))
                .isInstanceOf(WeakKeyException.class);
    }

    private MemberPrincipal principal(String... authorities) {
        Member member = new Member();
        member.setId(22L);
        MemberAccount account = new MemberAccount();
        account.setId(11L);
        account.setMember(member);
        account.setEmail("member@example.com");
        return new MemberPrincipal(account,
                java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList());
    }
}
