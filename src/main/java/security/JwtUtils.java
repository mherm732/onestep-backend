package security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct; 
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String secretFromProps;          // maps to env var JWT_SECRET via application.properties

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private SecretKey key;

    @PostConstruct
    void init() {
        // If you store a raw string in JWT_SECRET:
        byte[] bytes = secretFromProps.getBytes(StandardCharsets.UTF_8);
        // If you store BASE64 in JWT_SECRET, replace line above with:
        // byte[] bytes = Decoders.BASE64.decode(secretFromProps);

        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret too short; must be at least 32 bytes for HS256.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /** OLD SIGNATURE kept for compatibility with your AuthController */
    public String generateJwtToken(Authentication authentication) {
        String username = authentication.getName();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        }
        return generateJwtToken(username);
    }

    /** Helper that actually builds the token */
    public String generateJwtToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
