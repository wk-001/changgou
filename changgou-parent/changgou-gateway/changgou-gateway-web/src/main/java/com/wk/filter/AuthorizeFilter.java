package com.wk.filter;

import com.wk.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局过滤器
 * 用户鉴权/校验
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //令牌名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    /**
     * 全局拦截
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //获取用户令牌信息，令牌可能存在
        // 请求头文件中，获取请求头中第一个令牌信息
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        //true：令牌在头文件中 false：令牌不在头文件中
        boolean hasToken = true;

        // 如果请求头中没有令牌信息，从所有的请求参数中获取第一个指定的参数
        if (StringUtils.isEmpty(token)) {
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            hasToken = false;
        }

        // 如果参数中没有令牌信息，从cookie中获取第一个指定的参数
        if (StringUtils.isEmpty(token)) {
            HttpCookie cookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (cookie != null) {
                token = cookie.getValue();
            }
        }

        // 如果没有令牌，则拦截
        if (StringUtils.isEmpty(token)) {
            //设置没有权限的状态码 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空数据
            return response.setComplete();      //设置响应数据为空
        }

        //如果有令牌，则校验令牌是否有效
        try {
            JwtUtil.parseJWT(token);
        } catch (Exception e) {
            //令牌无效，拦截
            //设置没有权限的状态码 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空数据
            return response.setComplete();      //设置响应数据为空
        }

        //将令牌封装到头文件中，传递给下一个微服务，否则Oauth2.0无法校验令牌
        request.mutate().header(AUTHORIZE_TOKEN,token);

        //令牌有效，放行
        return chain.filter(exchange);
    }

    /**
     * 排序，越小越先执行
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
