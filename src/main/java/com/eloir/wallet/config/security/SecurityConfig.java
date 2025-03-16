package com.eloir.wallet.config.security;

import com.eloir.wallet.config.ConfigParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    private final ConfigParams configParams;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, ConfigParams configParams) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.configParams = configParams;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(configParams.getOpenedEndpointRoutes()).permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/wallets/**").hasRole("USER")
                                .requestMatchers("/api/operations/**").hasRole("USER")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
