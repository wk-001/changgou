package com.wk;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信SDK相关测试
 */
public class WeChatTest {

    @Test
    public void testDemo() throws Exception {
        //随机字符串
        String s = WXPayUtil.generateNonceStr();
        System.out.println("s = " + s);

        //Map转XML字符串
        Map<String,String> map = new HashMap<>();
        map.put("id","1");
        map.put("name","2");
        String s1 = WXPayUtil.mapToXml(map);
        System.out.println("s1 = " + s1);

        //Map转XML字符串，并生成签名
        String signedXml = WXPayUtil.generateSignedXml(map, "qianming");
        System.out.println("带签名的xml = " + signedXml);

        Map<String, String> result = WXPayUtil.xmlToMap(signedXml);
        System.out.println("xml转map = " + result);
    }
}
