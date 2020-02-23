package com.wk.pay.service;

import java.util.Map;

public interface WeixinPayService {

    /**
     *
     * @param outTradeNo
     * @return
     */
    Map queryStatus(String outTradeNo);

    /**
     * 创建二维码操作
     * @param paramMap
     * @return
     */
    Map createNative(Map<String,String> paramMap);
}
