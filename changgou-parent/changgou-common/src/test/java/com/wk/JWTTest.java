package com.wk;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT的加密解密
 */
public class JWTTest {

    /**
     * 创建令牌
     */
    @Test
    public void createToken(){
        Map<String,Object> map = new HashMap<>();
        map.put("1",2);
        map.put("3",4);
        JwtBuilder jwtBuilder = Jwts.builder()
                .setIssuer("wk")            //颁发者
                .setIssuedAt(new Date())    //颁发时间
                .setExpiration(new Date(System.currentTimeMillis()+(30*60*1000)))       //过期时间30分钟
                .setSubject("JWT的加密")             //主题信息
                .signWith(SignatureAlgorithm.HS256,"salt")     //签名算法，秘钥（盐）
                .addClaims(map);        //添加载荷
        System.out.println("jwtBuilder.compact() = " + jwtBuilder.compact());
    }

    /**
     * 解析令牌
     */
    @Test
    public void decodeToken(){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ3ayIsImlhdCI6MTU4MjExODU2NiwiZXhwIjoxNTgyMTIwMzY2LCJzdWIiOiJKV1TnmoTliqDlr4YiLCIxIjoyLCIzIjo0fQ.WFqwkch7aOYMcEXGSrJeZM5qFi63Ny-8peyHXn5ErSY";
        Claims claims = Jwts.parser()
                .setSigningKey("salt")      //秘钥（盐）
                .parseClaimsJws(token)       //要解析的令牌对象
                .getBody();//获取解析后的数据
        System.out.println("claims = " + claims.toString());
    }
}
