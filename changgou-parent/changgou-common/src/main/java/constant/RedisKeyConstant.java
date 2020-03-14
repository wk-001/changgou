package constant;

public class RedisKeyConstant {
    /**
     * 购物车
     */
    String Cart = "Cart_";

    /**
     * 秒杀订单用户排队信息
     */
    String UserQueueStatus = "UserQueueStatus_";

    /**
     * 秒杀订单
     */
    String SeckillOrder = "SeckillOrder_";

    /**
     * 商品库存队列
     */
    String SeckillGoodsCountList = "SeckillGoodsCountList_";

    /**
     * 用户排队次数，避免用户重复提交
     */
    String UserQueueCount = "UserQueueCount_";

    /**
     * 秒杀商品
     */
    String SeckillGoods_ = "SeckillGoods_";

    /**
     * 秒杀商品的队列
     */
    String SeckillOrderQueue = "SeckillOrderQueue_";
}
