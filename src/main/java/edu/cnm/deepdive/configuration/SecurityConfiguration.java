package edu.cnm.deepdive.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Provides configuration methods to customize token validation and to secure endpoints by
 * authentication status &amp; role.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


    private final Converter<Jwt, ? extends AbstractAuthenticationToken> converter;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.client-id}")
    private String clientId;

    /**
     * Initializes this instance with the provided {@link Converter}, used to convert the Bearer token
     * into a token useful for later injection into controller methods.
     *
     * @param converter Token converter.
     */
    @Autowired
    public SecurityConfiguration(
            Converter<Jwt, ? extends AbstractAuthenticationToken> converter) {
        this.converter = converter;
    }

    /**
     * Declares access-control rules on REST endpoints, based on HTTP method, authentication status,
     * and roles/authorities granted.
     *
     * @param http Security builder.
     * @throws Exception If an error occurs.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http
                .authorizeRequests((auth) ->
                        auth
                                .anyRequest()
                                .authenticated())
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(converter);
    }

    /**
     * Constructs and returns an injectable {@link JwtDecoder} that will be used automatically for
     * extended validation of the JSON web token (JWT) included as a bearer token in the request.
     *
     * @return Token decoder.
     */
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, (aud) -> aud.contains(clientId));
        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> combinedValidator =
                new DelegatingOAuth2TokenValidator<>(audienceValidator, issuerValidator);
        decoder.setJwtValidator(combinedValidator);
        return decoder;
    }
}