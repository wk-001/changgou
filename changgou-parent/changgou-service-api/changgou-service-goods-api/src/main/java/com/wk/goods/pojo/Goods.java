package com.wk.goods.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * 商品信息组合对象
 */
public class Goods implements Serializable {

    //spu信息
    private Spu spu;

    //sku集合信息
    private List<Sku> skuList;

    public Goods() {
    }

    public Goods(Spu spu, List<Sku> skuList) {
        this.spu = spu;
        this.skuList = skuList;
    }

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
