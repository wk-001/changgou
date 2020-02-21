package com.wk.oauth.service.impl;

import com.wk.oauth.service.LoginService;
import com.wk.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * 描述
 *
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.oauth.service.impl *
 * @since 1.0
 */
@Service
public class LoginServiceImpl implements LoginService {


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /**
     * 密码模式登录
     参数传递
         1、账号		username=szitheima
         2、密码		password=szitheima
         3、授权方式		grant_type=password
     请求头传递
        4、Basic Base64(客户端ID:客户端秘钥)		Authorization=Basic xxxxxx
     * @return
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret, String grandType) {

        //1.定义url (申请令牌的url)
        //参数 : 根据spring.yml文件获取指定微服务的数据
        ServiceInstance choose = loadBalancerClient.choose("user-auth");
        //获取地址和端口
        String url =choose.getUri().toString()+"/oauth/token";

        //2.定义头信息 (有client id 和client secr)
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization","Basic "+Base64.getEncoder().encodeToString(new String(clientId+":"+clientSecret).getBytes()));

        //3. 定义请求体  有授权模式 用户的名称 和密码
        MultiValueMap<String,String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type",grandType);
        formData.add("username",username);
        formData.add("password",password);

        //4.模拟浏览器 发送POST 请求 携带 头 和请求体 到认证服务器
        HttpEntity<MultiValueMap> requestentity = new HttpEntity<>(formData, headers);

        /**
         * 参数1  指定要发送的请求的url
         * 参数2  指定要发送的请求的方法 PSOT
         * 参数3  指定请求实体 请求提交的数据信息封装(包含头和请求体数据)
         * 参数4  返回数据需要转换的类型
         */
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestentity, Map.class);

        //5.接收到返回的响应(就是:令牌的信息)
        Map body = responseEntity.getBody();

        //封装一次.

        AuthToken authToken = new AuthToken();
        //访问令牌(jwt)
        String accessToken = (String) body.get("access_token");
        //刷新令牌(jwt)
        String refreshToken = (String) body.get("refresh_token");
        //jti，作为用户的身份标识
        String jwtToken= (String) body.get("jti");


        authToken.setJti(jwtToken);
        authToken.setAccessToken(accessToken);
        authToken.setRefreshToken(refreshToken);


        //6.返回
        return authToken;
    }


    public static void main(String[] args) {
        byte[] decode = Base64.getDecoder().decode(new String("Y2hhbmdnb3UxOmNoYW5nZ291Mg==").getBytes());
        System.out.println(new String(decode));
    }
}
