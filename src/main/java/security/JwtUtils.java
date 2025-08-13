package security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey key;
    private final long jwtExpirationMs;

    public JwtUtils(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs) {

        // For HS256, require >= 32 bytes for safety.
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret too short; must be at least 32 bytes.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateJwtToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
