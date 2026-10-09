package com.grosshaeuser.olmp.ui.external_ui_rest_api;

import com.grosshaeuser.olmp.security.JwtProperties;
import com.grosshaeuser.olmp.security.dto.MemberLoginRequest;
import com.grosshaeuser.olmp.security.dto.MemberLoginResponse;
import com.grosshaeuser.olmp.security.principal.MemberPrincipal;
import com.grosshaeuser.olmp.security.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/members/auth")
@RequiredArgsConstructor
public class MemberAuthenticationController {

    private final AuthenticationManager memberAuthenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public MemberLoginResponse login(@Valid @RequestBody MemberLoginRequest request) {
        try {
            var authentication = memberAuthenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email(),
                            request.password()
                    )
            );

            MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
            if (principal != null) {
                String token = jwtService.createAccessToken(principal);

                return new MemberLoginResponse(
                        "Bearer",
                        token,
                        jwtProperties.accessTokenTtl().toSeconds()
                );
            }

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");

        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }

}
