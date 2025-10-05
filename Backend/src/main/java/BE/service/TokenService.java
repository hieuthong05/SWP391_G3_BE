package BE.service;

import BE.entity.User;
import BE.repository.AuthenticationRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class TokenService {

    @Autowired
    AuthenticationRepository authenticationRepository;

    private final String SECRET_KEY = "5e25ecca90a6d0d773860b14821907a13a25a29eb40f2986cd40bfb8bed67ba5";

    public SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user){
        return Jwts.builder()
                .subject(user.getPhone())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+1000*60*60*6))
                .signWith(getSignInKey())
                .compact();
    }

    public User extractToken(String token){
        String value = extractClaim(token, Claims ::getSubject);
        return authenticationRepository.findUserByPhone(value);
    }

    public  String extractPhone(String token){
        return extractClaim(token,Claims :: getSubject);
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims :: getExpiration);
    }
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails){
        final String phone = extractPhone(token);
        return  (phone.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public <T> T extractClaim(String token, Function<Claims,T> reslover){
        Claims claims = extractAllClaims(token);
        return reslover.apply(claims);
    }

    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UserDetails loadUserByPhone(String phone){
        return  authenticationRepository.findUserByPhone(phone);
    }
}
