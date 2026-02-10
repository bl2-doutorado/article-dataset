package com.hvitops.auth.service;

import com.hvitops.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  @Value("${jwt.secret:hvitops-secret-key-for-jwt-token-validation-2024}")
  private String jwtSecret;

  @Value("${jwt.expiration:86400000}")
  private long jwtExpiration;

  public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", user.getRole().toString());
    claims.put("email", user.getEmail());
    claims.put("name", user.getName());

    return createToken(claims, user.getId().toString());
  }

  private String createToken(Map<String, Object> claims, String subject) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpiration);

    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key)
        .compact();
  }

  public String extractUserId(String token) {
    return getClaimsFromToken(token).getSubject();
  }

  public String extractRole(String token) {
    return getClaimsFromToken(token).get("role", String.class);
  }

  private void validateToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

    Jwts.parser()
        .verifyWith(key) // setSigningKey mudou para verifyWith
        .build()
        .parseSignedClaims(token); //
  }

  private Claims getClaimsFromToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
