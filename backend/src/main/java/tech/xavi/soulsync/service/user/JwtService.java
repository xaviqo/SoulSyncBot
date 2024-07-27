package tech.xavi.soulsync.service.user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tech.xavi.soulsync.entity.datafile.Account;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@Service
public class JwtService {

    private final String ISSUER = "SoulSyncBot";
    private final Key SECRET;
    private final long ACCESS_TKN_EXP_MS = 604800 * 1000L; // 7 days
    public static final String TOKEN_PREFIX = "Bearer ";

    public JwtService() { this.SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS256); }

    public boolean isTokenValid(String token, UserDetails userDetails){
        return extractUsername(token)
                .equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token)
                .before(new Date());
    }

    public String generateToken(Account account) {
        return Jwts.builder()
                .claim("role",account.getRole().getWithPrefix())
                .setIssuer(ISSUER)
                .setSubject(account.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TKN_EXP_MS ))
                .signWith(SECRET, SignatureAlgorithm.HS256)
                .compact();
    }

    public Optional<String> getTokenFromHeaders(HttpServletRequest request){
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX))
            return Optional.of(authHeader.replace(TOKEN_PREFIX, ""));
        return Optional.empty();
    }

    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        String role = extractRole(token);
        return Collections.singleton(new SimpleGrantedAuthority(role));

    }

    public String extractUsername(String token){
        return extractClaim(token,Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        return claimResolver.apply(
                extractAllClaims(token)
        );
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(SECRET)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
