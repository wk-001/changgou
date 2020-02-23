package com.wk.pay.controller;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.WXPayUtil;
import com.wk.pay.service.WeixinPayService;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@RestController
@RequestMapping(value = "/weixin/pay")
@CrossOrigin
public class WeixinPayController {

    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /***
     * 支付回调
     * @param request
     * @return
     */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request) throws Exception {
        //获取网络输入流，通过输入流获取结果
        ServletInputStream is = request.getInputStream();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len=is.read(buffer))!=-1){
            baos.write(buffer,0,len);
        }

        //微信支付结果的字节数组
        byte[] bytes = baos.toByteArray();

        //字节数组转xml字符串
        String xmlResult = new String(bytes,"UTF-8");

        //xml字符串转map
        Map<String, String> stringMap = WXPayUtil.xmlToMap(xmlResult);
        System.out.println("stringMap = " + stringMap);

        //发送支付结果给MQ
        rabbitTemplate.convertAndSend("exchange.order","queue.order", JSON.toJSONString(stringMap));

        String result = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        return result;
    }

    /***
     * 微信支付：查询支付状态
     * @param outTradeNo   商户订单号
     * @return
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(String outTradeNo){
        Map<String,String> resultMap = weixinPayService.queryStatus(outTradeNo);
        return new Result(true,StatusCode.OK,"查询状态成功！",resultMap);
    }

    /***
     * 微信支付：创建二维码
     * @return
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String,String> paramMap){
        Map<String,String> resultMap = weixinPayService.createNative(paramMap);
        return new Result(true, StatusCode.OK,"创建二维码预付订单成功！",resultMap);
    }
}
