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
     * 以网络输入流的形式返回支付回调xml数据
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
        Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
        System.out.println("resultMap = " + resultMap);

        //获取自定义参数attach并转成Map类型
        Map<String,String> attach = JSON.parseObject(resultMap.get("attach"), Map.class);

        //发送支付结果给MQ，如果队列名字能够从微信支付服务器哪边返回就可以动态设置Queue的名字
        rabbitTemplate.convertAndSend(attach.get("exchange"),attach.get("routingKey"), JSON.toJSONString(resultMap));
//        rabbitTemplate.convertAndSend("exchange.order","queue.order", JSON.toJSONString(resultMap));

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
     * 普通订单：
     *      exchange：exchange.order
     *      routingKey：queue.order
     * 秒杀订单：
     *      exchange：exchange.seckillorder
     *      routingKey：queue.seckillorder
     * 将exchange和routingKey的数据打包转成JSON，设置到attach，传递给微信服务器，微信服务器原样返回，实现自定义数据传递
     * @return
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String,String> paramMap){
        Map<String,String> resultMap = weixinPayService.createNative(paramMap);
        return new Result(true, StatusCode.OK,"创建二维码预付订单成功！",resultMap);
    }
}
