package com.wk.oauth.controller;

import com.wk.oauth.service.LoginService;
import com.wk.oauth.util.AuthToken;
import com.wk.oauth.util.CookieUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 描述
 *
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.oauth.controller *
 * @since 1.0
 */
@RestController
@RequestMapping("/user")
public class UserLoginController {

    @Autowired
    private LoginService loginService;

    //客户端id
    @Value("${auth.clientId}")
    private String clientId;

    //客户端秘钥
    @Value("${auth.clientSecret}")
    private String clientSecret;

    private static final String GRAND_TYPE = "password";//授权模式 密码模式


    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    //Cookie生命周期
    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;


    /**
     * 密码模式  登录认证.
     *
     *参数传递
     * 	1、账号		username=szitheima
     * 	2、密码		password=szitheima
     * 	3、授权方式		grant_type=password
     * 请求头传递
     * 	4、Basic Base64(客户端ID:客户端秘钥)		Authorization=Basic xxxxxx
     */
    @RequestMapping("/login")
    public Result<Map> login(String username, String password) {
        //登录 之后生成令牌的数据返回
        AuthToken authToken = loginService.login(username, password, clientId, clientSecret, GRAND_TYPE);


        //设置到cookie中
        saveCookie(authToken.getAccessToken());
        return new Result<>(true, StatusCode.OK,"令牌生成成功",authToken);
    }

    private void saveCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/","Authorization",token,cookieMaxAge,false);
    }
}
