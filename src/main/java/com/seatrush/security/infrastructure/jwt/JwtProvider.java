package com.seatrush.security.infrastructure.jwt;

import com.seatrush.security.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {
    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(JwtProperties jwtProperties){

        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.secret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = jwtProperties.accessTokenExpiration();
        this.refreshTokenExpiration = jwtProperties.refreshTokenExpiration();
    }

    public String generateAccessToken(String email, String role){

        Date now = new Date();
        Date expiration = new Date(now.getTime()+accessTokenExpiration);

        return Jwts.builder()
                .subject(email)
                .claim("role",role)
                .claim("type","ACCESS")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();

    }

    public String generateRefreshToken(String email, String role){

        Date now = new Date();
        Date expiration = new Date(now.getTime()+refreshTokenExpiration);

        return Jwts.builder()
                .subject(email)
                .claim("role",role)
                .claim("type","REFRESH")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();

    }

    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        }
        catch (JwtException e){
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String getTypeFromToken(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
