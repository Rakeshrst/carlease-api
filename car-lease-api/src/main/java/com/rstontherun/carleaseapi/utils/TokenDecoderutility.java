package com.rstontherun.carleaseapi.utils;


import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.rstontherun.carleaseapi.exception.BadRequestException;
import com.rstontherun.carleaseapi.exception.UnauthorizedException;
import lombok.experimental.UtilityClass;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

@UtilityClass
public class TokenDecoderutility {

    public static String extractUserName(String token) {
        return (String) TokenDecoderutility.getClaimSets(token).get("sub");
    }

    public static Map<String, Object> getClaimSets(String token) {
        try {
            JWT jwt = JWTParser.parse(token.startsWith("Bearer") ? token.substring("Bearer".length() + 1) : token);
            return jwt.getJWTClaimsSet().getClaims();
        } catch (ParseException e) {
            throw new BadRequestException("Invalid token");
        }
    }

    public static String getAuthorizationHeader(ServerRequest request) {
        List<String> authorizationHeaders = request.headers().header("Authorization");

        if (authorizationHeaders.isEmpty()) {
            throw new UnauthorizedException("Authorization header missing");
        }

        String authorizationHeader = authorizationHeaders.get(0);

        if(authorizationHeader.isBlank() || authorizationHeader.isEmpty()){
            throw new BadRequestException("Authorization header missing");
        }

        return authorizationHeader;
    }

}
