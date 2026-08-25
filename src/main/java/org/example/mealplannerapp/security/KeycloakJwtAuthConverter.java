package org.example.mealplannerapp.security;

import lombok.RequiredArgsConstructor;
import org.example.mealplannerapp.entity.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

// TODO: Javadocs.
@Component
@RequiredArgsConstructor
public class KeycloakJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final IdentityService identityService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        User user = identityService.provisionFromJwt(jwt);
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new JwtAuthenticationToken(jwt, authorities, user.getId().toString());
    }
}
