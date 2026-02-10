package com.hvitops.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter
    extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

  @Value("${jwt.secret:hvitops-secret-key-for-jwt-token-validation-2024}")
  private String jwtSecret;

  public JwtAuthenticationFilter() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      try {
        String token = extractToken(exchange);
        if (token != null) {
          validateToken(token);
          Claims claims = getClaimsFromToken(token);
          exchange
              .getRequest()
              .mutate()
              .header("X-User-Id", claims.getSubject())
              .header("X-User-Role", claims.get("role", String.class))
              .build();
        }
      } catch (Exception e) {
        return onError(exchange, "Unauthorized", HttpStatus.UNAUTHORIZED);
      }
      return chain.filter(exchange);
    };
  }

  private String extractToken(ServerWebExchange exchange) {
    String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
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

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
    exchange.getResponse().setStatusCode(httpStatus);
    return exchange.getResponse().setComplete();
  }

  public static class Config {}
}
