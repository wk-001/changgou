package com.wk.filter;

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

    //用户请求参数|头文件|cookie中令牌参数名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    //用户登录地址
    private static final String USER_LOGIN_URL = "http://localhost:9001/oauth/login";

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

        //用户如果发送登录或一些不需要做权限认证的请求，直接放行
        String uri = request.getURI().toString();
        if(URLFilter.hasAuthorize(uri)){
            chain.filter(exchange);
        }

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

        // 如果没有令牌，则重定向到登录页面
        if (StringUtils.isEmpty(token)) {
            //设置没有权限的状态码 401
            //response.setStatusCode(HttpStatus.UNAUTHORIZED);

            response.setStatusCode(HttpStatus.SEE_OTHER);
            //记录用户要访问的页面
            response.getHeaders().set("Location",USER_LOGIN_URL+"?from="+request.getURI());
            return response.setComplete();      //设置响应数据为空
        }

        //如果有令牌，则校验令牌是否有效
       /* try {
            //JwtUtil.parseJWT(token);
        } catch (Exception e) {
            //令牌无效，拦截
            //设置没有权限的状态码 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空数据
            return response.setComplete();      //设置响应数据为空
        }*/

        /*使用证书、公钥私钥的校验方法，
        判断令牌是否为空，如果不为空，将令牌放到头文件中并放行
        令牌为空，则不允许访问，直接拦截*/
        if(StringUtils.isEmpty(token)){
            //令牌无效，拦截
            //设置没有权限的状态码 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空数据
            return response.setComplete();      //设置响应数据为空
        }else {
            //将令牌封装到头文件中，传递给下一个微服务，否则Oauth2.0无法校验令牌
            if (!hasToken){
                //判断当前令牌是否有"bearer "的前缀，如果没有则添加前缀，字母后面有一个空格
                if(!token.startsWith("bearer ")&&!token.startsWith("Bearer ")){
                    token = "bearer "+token;
                }
                //一个请求头只能存在一个token
                request.mutate().header(AUTHORIZE_TOKEN,token);
            }
        }

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
