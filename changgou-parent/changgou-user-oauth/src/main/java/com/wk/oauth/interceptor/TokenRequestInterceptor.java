package com.wk.oauth.interceptor;

import com.wk.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * 在拦截器中生成令牌，并把令牌放到请求头中
 */
@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {

    /**
     * Feign执行之前进行拦截
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //1：没有令牌，Feign调用之前，生成令牌（admin）
        //2：Feign调用之前，令牌需要携带过去
        //3：Feign调用之前，令牌需要存放到Header文件中
        //4：请求->Feign调用->拦截器RequestInterceptor->Feign调用之前执行拦截

        //生成admin令牌
        String token = AdminToken.adminToken("admin", "oauth");
        requestTemplate.header("Authorization","bearer "+token);
    }
}
