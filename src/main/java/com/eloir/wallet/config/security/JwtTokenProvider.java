package com.eloir.wallet.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String SECRET_KEY = "e1l2o3i4r5j6o7s8e9a1l2m3e4i5d6a7d8a9s1i2l3v4a5j6o7s8i9e1l2e3d4e5f6a7t8i9m1a2c3o4r5d6e7i8r9o1d2a3s4i5l6v7a8";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;  // 1 hora

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            log.error("Unauthorized: Token is missing or invalid");
            return false;
        }
    }

    public Claims parseClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseClaimsFromToken(token);
        return claims.getSubject();
    }
}
