package com.rstontherun.carleaseapi.utils;


import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.rstontherun.carleaseapi.exception.BadRequestException;
import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.util.Map;

@UtilityClass
public class TokenDecoderutility{

    public static Map<String, Object> getClaimSets(String token) {
        try {
            JWT jwt = JWTParser.parse(token.startsWith("Bearer") ? token.substring("Bearer".length()+1) :token);
            return jwt.getJWTClaimsSet().getClaims();
        } catch (ParseException e) {
            throw new BadRequestException("Invalid token");
        }
    }

    public static String extractUserName(String token) {
        return (String) TokenDecoderutility.getClaimSets(token).get("sub");
    }

}
