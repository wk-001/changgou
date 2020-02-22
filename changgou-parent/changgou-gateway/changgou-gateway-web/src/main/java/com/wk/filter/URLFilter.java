package com.wk.filter;

/**
 * 不需要认证就能访问的路径校验
 */
public class URLFilter {

    //不需要拦截的URL
    private static final String allurl="/user/add,/user/login";

    /**
     *校验当前访问路径是否需要验证权限
     * @param url：用户当前访问的路径
     * @return 不需要验证：true；需要验证：false
     */
    public static boolean hasAuthorize(String url){
        String[] urls = allurl.split(",");

        for (String uri : urls) {
            if(uri.equals(url)){
                return true;
            }
        }
        return false;
    }
}
