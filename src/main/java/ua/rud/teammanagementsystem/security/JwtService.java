package ua.rud.teammanagementsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
    @Service
    @RequiredArgsConstructor
class JwtService{
    private final String SECRET_KEY = "m7Qp3Vt9xLk8Zc2R6nF1aW0sYhP4uD8eJ3bT5mN9kQ2vC7rX1pA6sH0dL8yUjV4";

   public SecretKey getSignInKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 60))
                .signWith(getSignInKey())
                .compact();
    }
        public String generateToken(UserDetails userDetails){
            return generateToken(new HashMap<>(), userDetails);
        }

        public Claims extracrtAllClaims(String token){
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
    public<T> T extractClaims(String token, Function<Claims, T> claimResolver){
final Claims claims = extracrtAllClaims(token);
return claimResolver.apply(claims);
    }

    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }
    public Date getTokenExpiration(String token){
        return extractClaims(token, Claims::getExpiration);
    }
    public boolean isTokenExpired(String token){
        return getTokenExpiration(token).before(new Date());
    }
    public boolean isTokenValid(String token, UserDetails userDetails){
final String username = extractUsername(token);
return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

        }