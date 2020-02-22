package entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

/**
 * 在拦截器中生成令牌，并把令牌放到请求头中，传递给下一个微服务
 */
public class FeignInterceptor implements RequestInterceptor {

    /**
     * Feign执行之前进行拦截
     * 获取用户的令牌，再封装到头文件中
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        /*记录了当前用户请求对应线程的所有数据，包含请求头和请求参数等
        * 如果开启了熔断，默认隔离策略是线程池隔离，会启动一个新线程，无法获取之前线程中的数据
        * 所以开启熔断的情况下需要将熔断策略改成信号量隔离，不会开启新的线程，不会影响数据的获取*/
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        //获取请求头中的数据
        //获取所有请求头的名字
        Enumeration<String> headerNames = attributes.getRequest().getHeaderNames();
        while(headerNames.hasMoreElements()){
            //请求头的key
            String headerKey = headerNames.nextElement();

            //获取请求头的值
            String headerValue = attributes.getRequest().getHeader(headerKey);

            //将请求头信息封装到头中，使用Feign调用的时候，会传递给下一个微服务
            requestTemplate.header(headerKey,headerValue);
        }
    }
}
