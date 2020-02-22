package com.wk.oauth.util;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

public class AdminToken {
    /**
     * 发放管理员令牌
     * @return
     */
    public static String adminToken(String... role){
        //证书文件路径
        String key_location="changgou.jks";
        //秘钥库密码
        String key_password="changgou";
        //秘钥密码
        String keypwd = "changgou";
        //秘钥别名
        String alias = "changgou";

        //加载证书  读取类路径的文件
        ClassPathResource resource = new ClassPathResource(key_location);

        //读取证书数据
        KeyStoreKeyFactory keyFactory = new KeyStoreKeyFactory(resource,key_password.toCharArray());

        //获取证书中的公钥私钥
        KeyPair keyPair = keyFactory.getKeyPair(alias, keypwd.toCharArray());

        //获取私钥 RSA算法
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        //载荷
        Map<String,Object> payload = new HashMap<>();
        payload.put("username","tom");
        //设置用户的角色/权限
        payload.put("authorities",role);

        //创建令牌，私钥加盐（RSA算法）
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateKey));

        //获取令牌数据
        String token = jwt.getEncoded();
        System.out.println("token = " + token);
        return token;
    }

    public static void main(String[] args) {
        adminToken("admin","oauth");
    }
}
