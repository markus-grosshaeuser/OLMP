package com.grosshaeuser.olmp.security.filter;

import com.grosshaeuser.olmp.security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private FilterChain chain;
    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(jwtService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Basic abc", "bearer token", "Bearer", "Something token"})
    void ignoresRequestsWithoutBearerAuthorization(String authorization) throws Exception {
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }

        filter.doFilter(request, response, chain);

        verifyNoInteractions(jwtService);
        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void validTokenPopulatesSecurityContextBeforeContinuingChain() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");
        request.setRemoteAddr("192.0.2.1");
        when(jwtService.parseAndValidate("valid-token")).thenReturn(claims(List.of("ROLE_MEMBER", "BORROW_MEDIA")));
        doAnswer(invocation -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.isAuthenticated()).isTrue();
            assertThat(authentication.getPrincipal()).isEqualTo(
                    new JwtAuthenticationFilter.MemberJwtPrincipal("member@example.com", 11L, 22L));
            assertThat(authentication.getCredentials()).isEqualTo("valid-token");
            assertThat(authentication.getAuthorities()).extracting("authority")
                    .containsExactly("ROLE_MEMBER", "BORROW_MEDIA");
            assertThat(authentication.getDetails()).isInstanceOf(WebAuthenticationDetails.class);
            assertThat(((WebAuthenticationDetails) authentication.getDetails()).getRemoteAddress()).isEqualTo("192.0.2.1");
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void absentAuthoritiesAuthenticateWithNoGrantedAuthorities() throws Exception {
        request.addHeader("Authorization", "Bearer token");
        when(jwtService.parseAndValidate("token")).thenReturn(claims(null));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEmpty();
        verify(chain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"accountId", "memberId"})
    void missingIdentityClaimReturnsUnauthorizedAndStopsChain(String missingClaim) throws Exception {
        request.addHeader("Authorization", "Bearer token");
        var builder = Jwts.claims().subject("member@example.com");
        builder.add(missingClaim.equals("accountId") ? "memberId" : "accountId", 1);
        when(jwtService.parseAndValidate("token")).thenReturn(builder.build());

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getErrorMessage()).isEqualTo("Invalid JWT claims");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(chain);
    }

    @Test
    void malformedIdentityClaimReturnsUnauthorized() throws Exception {
        request.addHeader("Authorization", "Bearer token");
        when(jwtService.parseAndValidate("token")).thenReturn(Jwts.claims()
                .subject("member@example.com").add("accountId", "not-a-number").add("memberId", 22).build());

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(chain);
    }

    @Test
    void invalidTokenClearsExistingAuthenticationAndStopsChain() throws Exception {
        request.addHeader("Authorization", "Bearer invalid-token");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("previous-user", null, List.of()));
        when(jwtService.parseAndValidate("invalid-token")).thenThrow(new JwtException("Invalid JWT"));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getErrorMessage()).isEqualTo("Invalid or expired JWT");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(chain);
    }

    @Test
    void downstreamExceptionsPropagateInsteadOfBecomingJwtErrors() throws Exception {
        request.addHeader("Authorization", "Bearer token");
        when(jwtService.parseAndValidate("token")).thenReturn(claims(List.of("ROLE_MEMBER")));
        ServletException failure = new ServletException("Controller failed");
        doThrow(failure).when(chain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilter(request, response, chain)).isSameAs(failure);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    private Claims claims(List<String> authorities) {
        // Integer and Long IDs both occur when parsing JSON numbers.
        var builder = Jwts.claims().subject("member@example.com").add("accountId", 11).add("memberId", 22L);
        if (authorities != null) {
            builder.add("authorities", authorities);
        }
        return builder.build();
    }
}
