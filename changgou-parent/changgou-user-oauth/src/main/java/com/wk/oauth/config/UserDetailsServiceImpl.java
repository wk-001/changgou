package com.wk.oauth.config;
import com.wk.oauth.util.UserJwt;
import com.wk.user.feign.UserFeign;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/*****
 * 自定义授权认证类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;

    @Autowired
    private UserFeign userFeign;

    /****
     * 自定义授权认证
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /*----------------------授权码认证 客户端信息认证开始-------------------------*/
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication==null){
            //查询数据库 oauth_client_details
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if(clientDetails!=null){
                //秘钥
                String clientSecret = clientDetails.getClientSecret();
                //静态方式
               /* return new User(
                        username        //客户端ID
                        ,new BCryptPasswordEncoder().encode(clientSecret)       //加密的客户端秘钥
                        , AuthorityUtils.commaSeparatedStringToAuthorityList(""));  //权限*/
                //数据库查找方式
                return new User(
                        username            //客户端ID
                        ,clientSecret       //客户端秘钥，数据库的秘钥已经加密
                        ,AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        /*----------------------授权码认证 客户端信息认证结束-------------------------*/

        /*----------------------密码认证 用户账号密码信息认证开始-------------------------*/
        if (StringUtils.isEmpty(username)) {
            return null;
        }

        /*从数据库查询用户信息
        * 1：没有令牌，Feign调用之前，生成令牌（admin）
          2：Feign调用之前，令牌需要携带过去
          3：Feign调用之前，令牌需要存放到Header文件中
          4：请求->Feign调用->拦截器RequestInterceptor->Feign调用之前执行拦截
          * 创建一个拦截器，在拦截器中生成令牌，并把令牌放到请求头中*/
        Result<com.wk.user.pojo.User> userResult = userFeign.findById(username);

        if (userResult == null||userResult.getData()==null) {
            return null;
        }

        //获取用户密码
        String pwd = userResult.getData().getPassword();

        //加密明文密码
//        String pwd = new BCryptPasswordEncoder().encode("szitheima");
        //用户角色，真实情况应该在数据库查询出来
//        String permissions = "goods_list,seckill_list";
        String permissions = "user,vip";


        UserJwt userDetails = new UserJwt(
                username        //客户端ID
                ,pwd            //加密的客户端秘钥
                ,AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));      //权限
        /*----------------------密码认证 用户账号密码信息认证结束-------------------------*/

        //userDetails.setComy(songsi);
        return userDetails;
    }
}
