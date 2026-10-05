package com.murilo.helpdesk.security;

import com.murilo.helpdesk.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;


@Service
public class JwtService {

    private static final String EMISSOR = "helpdesk-api";
    private static final int TAMANHO_MINIMO_CHAVE = 32;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey signingKey;


    @PostConstruct
    void init() {
        byte[] bytes = jwtSecret == null ? new byte[0] : jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_CHAVE) {
            throw new IllegalStateException(
                    "jwt.secret deve ter pelo menos 32 bytes (256 bits) para assinar com HMAC-SHA256.");
        }
        if (jwtExpiration <= 0) {
            throw new IllegalStateException("jwt.expiration deve ser maior que zero (em milissegundos).");
        }
        signingKey = Keys.hmacShaKeyFor(bytes);
    }


    // O subject é o id (e não o e-mail) para o token continuar válido se o usuário trocar de e-mail.
    public String generateToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .issuer(EMISSOR)
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusMillis(jwtExpiration)))
                .signWith(signingKey)
                .compact();
    }


    // Valida assinatura, emissor e expiração; lança JwtException se o token não for aceito.
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(EMISSOR)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(Claims claims) {
        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new JwtException("Subject do token inválido.");
        }
    }

    public Instant extractExpiration(String token) {
        return parseToken(token).getExpiration().toInstant();
    }
}
