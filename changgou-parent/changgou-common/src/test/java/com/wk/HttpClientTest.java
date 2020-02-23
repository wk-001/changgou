package com.wk;

import entity.HttpClient;
import org.junit.Test;

import java.io.IOException;

/**
 * 使用HTTPclient发送HTTP/HTTPS请求
 */
public class HttpClientTest {

    @Test
    public void testHttp() throws IOException {
        //要访问的URL
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";

        //要发送的xml请求参数
        String xml = "<xml>\n" +
                "   <appid>wx2421b1c4370ec43b</appid>\n" +
                "   <mch_id>10000100</mch_id>\n" +
                "   <nonce_str>ec2316275641faa3aacf3cc599e8730f</nonce_str>\n" +
                "   <transaction_id>1008450740201411110005820873</transaction_id>\n" +
                "   <sign>FDD167FAA73459FD921B144BAF4F4CA2</sign>\n" +
                "</xml>";

        HttpClient httpClient = new HttpClient(url);

        //设置请求的xml参数
        httpClient.setXmlParam(xml);

        //设置当前请求为HTTPS
        httpClient.setHttps(true);

        //发送请求，带有xml数据的一律用post
        httpClient.post();

        //获取响应数据
        String content = httpClient.getContent();
        System.out.println("content = " + content);
    }

}
