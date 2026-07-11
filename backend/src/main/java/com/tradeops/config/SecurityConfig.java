package com.tradeops.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;
    @Value("${app.security.google.allowed-domain:}")
    private String googleAllowedDomain;
    @Value("${app.security.google.admin-emails:}")
    private String googleAdminEmails;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:https://accounts.google.com}")
    private String issuerUri;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults());
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyAuthority("ROLE_ops_user", "ROLE_ops_admin")
                .requestMatchers("/api/**").hasAuthority("ROLE_ops_admin")
                .anyRequest().authenticated());
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Stream.of(allowedOrigins.split(",")).map(String::trim).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));
            authorities.addAll(extractRoles(jwt));
            authorities.addAll(extractGoogleAuthorities(jwt));
            return authorities;
        });
        return converter;
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> realm) {
            Object roles = realm.get("roles");
            if (roles instanceof Collection<?> values) {
                values.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            }
        }
        Object resourceAccess = jwt.getClaims().get("resource_access");
        if (resourceAccess instanceof Map<?, ?> resources) {
            resources.values().forEach(resource -> {
                if (resource instanceof Map<?, ?> details && details.get("roles") instanceof Collection<?> values) {
                    values.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
                }
            });
        }
        return authorities;
    }

    private Collection<GrantedAuthority> extractGoogleAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String issuer = jwt.getIssuer() == null ? "" : jwt.getIssuer().toString();
        if (!issuer.equals("https://accounts.google.com") && !issuer.equals("accounts.google.com")) {
            return authorities;
        }

        String email = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        String hostedDomain = jwt.getClaimAsString("hd");
        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            return authorities;
        }
        if (!googleAllowedDomain.isBlank() && !googleAllowedDomain.equalsIgnoreCase(hostedDomain)) {
            return authorities;
        }

        authorities.add(new SimpleGrantedAuthority("ROLE_ops_user"));
        Set<String> adminEmails = new HashSet<>(Stream.of(googleAdminEmails.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .toList());
        if (adminEmails.contains(email.toLowerCase())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ops_admin"));
        }
        return authorities;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
}
