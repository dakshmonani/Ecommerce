package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
@Component
public class JwtUtils {
    private static  final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;
    @Value("${spring.ecom.app.jwtCookie}")
    private String jwtCookie;


//    //Getting jwt from header
//    public  String getJwtFromHeader(HttpServletRequest request){
//        String bearerToken =request.getHeader("Authorization");
//        logger.debug("Authorization Header {} " ,bearerToken );
//
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")){
//            return bearerToken.substring(7);
//        }
//        return null;
//    }

    //getting jwt from cookie
    public String getJwtFromCookie(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request,jwtCookie);
        if (cookie!=null){
            System.out.println("COOKIE : " +cookie.getValue());
            return cookie.getValue();
        }else {
            return null;
        }
    }
    public String getJwtFromHeader(HttpServletRequest request){
       String bearerToken = request.getHeader("Authorization");
       if (bearerToken != null && bearerToken.startsWith("Bearer ")){
           return bearerToken.substring(7);
       }
       return null;
    }


    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal){
        String jwt = generateTokenFromUserName(userPrincipal.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie,jwt)
                                                .path("/api")
                                                .maxAge(24*60*60)
                                                .httpOnly(false)
                                                .build();

        return cookie;
    }
    //for signout
    public ResponseCookie getCleanCookie(){
        ResponseCookie cookie = ResponseCookie.from(jwtCookie,null)
                .path("/api")
                .build();
        return cookie;
    }


    //Generating Token from username
    public String generateTokenFromUserName(String username){
//        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime()+jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    //Getting username from jwt token

    public String getUserNameFromJwtToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    //Generating Signing Key
    public Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret));
    }

    //validate JWT token
    public boolean validateJwtToken(String authToken){
        try{
            System.out.println("Validate");
            Jwts.parser().verifyWith((SecretKey)key()).build().parseSignedClaims(authToken);
            return true;
        }catch (MalformedJwtException malformedJwtException){
            logger.error("Invalid JWT Token : {}" , malformedJwtException.getMessage());
        }catch (ExpiredJwtException expiredJwtException){
            logger.error("JWT Token is expired : {}" , expiredJwtException.getMessage());
        }catch (UnsupportedJwtException unsupportedJwtException){
            logger.error("JWT Token is unsupported {}",unsupportedJwtException.getMessage());
        }catch (IllegalArgumentException exception){
            logger.error("JWT claims string is empty {} ",exception.getMessage());
        }
        return false;
    }

}
