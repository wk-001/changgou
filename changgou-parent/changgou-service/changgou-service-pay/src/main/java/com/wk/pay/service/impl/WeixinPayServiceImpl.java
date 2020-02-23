package com.wk.pay.service.impl;

import com.github.wxpay.sdk.WXPayUtil;
import com.wk.pay.service.WeixinPayService;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    //应用ID
    @Value("${weixin.appid}")
    private String appid;

    //商户ID
    @Value("${weixin.partner}")
    private String partner;

    //秘钥
    @Value("${weixin.partnerkey}")
    private String partnerkey;

    //支付回调地址
    @Value("${weixin.notifyurl}")
    private String notifyurl;


    /**
     * 微信支付：查询订单支付状态
     * @param outTradeNo
     * @return
     */
    @Override
    public Map queryStatus(String outTradeNo) {
        try {
            //参数
            Map<String,String> param = new HashMap<>();
            param.put("appid",appid);           //应用ID
            param.put("mch_id",partner);       //商户ID
            param.put("nonce_str", WXPayUtil.generateNonceStr());   //随机字符串
            param.put("out_trade_no",outTradeNo);   //商户订单号

            //map转成xml字符串可以携带签名
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);

            String queryUrl = "https://api.mch.weixin.qq.com/pay/orderquery";

            //提交方式
            HttpClient httpClient = new HttpClient(queryUrl);
            httpClient.setHttps(true);

            //提交参数
            httpClient.setXmlParam(signedXml);

            //执行请求
            httpClient.post();

            //获取返回数据
            String content = httpClient.getContent();

            //返回数据转成map
            Map<String, String> result = WXPayUtil.xmlToMap(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 微信支付：创建支付二维码
     * @param paramMap
     * @return
     */
    @Override
    public Map createNative(Map<String, String> paramMap) {
        try {
            //参数
            Map<String,String> param = new HashMap<>();
            param.put("appid",appid);           //应用ID
            param.put("mch_id",partner);       //商户ID
            param.put("nonce_str", WXPayUtil.generateNonceStr());   //随机字符串
            param.put("body","畅购商城商品");                    //商品描述
            param.put("out_trade_no",paramMap.get("outTradeNo"));   //商户订单号
            param.put("total_fee",paramMap.get("totalFee"));          //交易金额，单位/分
            param.put("spbill_create_ip","127.0.0.1");          //终端IP
            param.put("notify_url",notifyurl);          //交易结果回调通知地址
            param.put("trade_type","NATIVE");          //交易类型
            //map转成xml字符串可以携带签名
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);

            //微信服务器下单URL地址
            String payUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";

            //提交方式
            HttpClient httpClient = new HttpClient(payUrl);
            httpClient.setHttps(true);

            //提交参数
            httpClient.setXmlParam(signedXml);

            //执行请求
            httpClient.post();

            //获取返回数据
            String content = httpClient.getContent();

            //返回数据转成map
            Map<String, String> result = WXPayUtil.xmlToMap(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }
}
