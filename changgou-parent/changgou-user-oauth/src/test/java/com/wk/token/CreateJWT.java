package com.wk.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 令牌的创建和解析
 */
public class CreateJWT {

    /**
     * 创建令牌
     */
    @Test
    public void createToken(){
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
        payload.put("authorities",new String[]{"admin","oauth"});

        //创建令牌，私钥加盐（RSA算法）
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateKey));

        //获取令牌数据
        String token = jwt.getEncoded();
        System.out.println("token = " + token);
    }

    /**
     * 解析令牌
     */
    @Test
    public void encodeToken(){
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdXRob3JpdGllcyI6WyJhZG1pbiIsIm9hdXRoIl0sInVzZXJuYW1lIjoidG9tIn0.CtkBbgYT1F-H2qCARlnyf_NOqylKBIXB0bJ0JynhCvby0IcSIfz6l61trdSjw29B9sJ17_o0wa3dDq25rYxrk9oEKe2Jfz3W4iMIt_vX8c7tUUSqGtjWWBILSWeFTIK4f19qcKxQ8946aQSYL9Me4J44y1-lqRq1Pn1Sud9IsMvaVotkeUpXSvOJYqfWaKPX8R93Y5JYA13X8qXy15uVHzvWn7c9sK3btubZaIfPmVu_3PV76g-WtvQcWorZRlP6HY9qxQ224Gd58t5FBIUGHupIX1m6IPKLVTxzgqckP7vJUlRNsD_AJrA3nlZFCjo-9E4dVf0iy9Rw9xy5eisq2Q";
        //公钥，必须一次不差复制过来，包括前后缀
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlvf19DxhxmRhQSAOgOHzZQnT+ANnBLgrgRFg30H4OIUVqvsDdJcXjTAZBaxff9r1W/IKG5eJKOUnCw9Ksja7f2tdk1APXD8Gnye8esxxGiZMV2+qG1Obt4HGVyH4huky7CF6w4YqtwVDX8+6isUGfF+KBbPa1KW4EeXGooWpBFd70OuUAhH361cFz43iohf+S80TlKNzuuq0ewQ/1/Ewhz/SaWX2/SAqynlyIlzOOfih1kQF0c3yUU560tYeSOZpuOdqcajg/opnES6ezWXJNFll/irbiDMLSxxjDeb6o4mZW+iige+eJsZNQSBc1VIO7pv+JuOp2JE29rh87u9pwQIDAQAB-----END PUBLIC KEY-----";
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));
        System.out.println(jwt.getClaims());
    }
}
