package com.wk.seckill.timer;

import com.wk.seckill.dao.SeckillGoodsMapper;
import com.wk.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时将秒杀商品存入到Redis缓存
 */
@Component
public class SeckillGoodsPushTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /***
     * 如果当前时间是上午10点，每两个小时算一个阶段，10~12、 12~14... 获取之后5个阶段需要秒杀的商品信息
     * 存入Redis的秒杀商品条件
     *   1、查询符合当前时间的秒杀商品
     *   2、审核通过，status=1
     *   3、库存>0
     *   4、开始时间大于当前时间，小于结束时间
     *      获取整个时间菜单
     *      确定每个时间菜单的区间值（2个小时一次秒杀）
     *      根据菜单时间的区间值获取对应的秒杀商品数据
     *      将对应的时间区间的秒杀商品数据存入到Redis
     * 定时任务每30秒执行一次
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void loadGoodsPushRedis(){
        //2个小时为一个阶段，获取当前的时间往后的5个时间段
        List<Date> dateMenus = DateUtil.getDateMenus();
        //循环查询每个时间区间的秒杀商品
        for (Date dateMenu : dateMenus) {
            //时间的字符串格式：yyyyMMddHH
            String timespace = "SeckillGoods_"+DateUtil.data2str(dateMenu, DateUtil.PATTERN_YYYYMMDDHH);
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status","1")               //审核通过，status=1
                    .andGreaterThan("stockCount",0)         //库存>0
                    .andGreaterThanOrEqualTo("startTime",dateMenu)      //开始时间大于当前时间段
                    .andLessThan("endTime",DateUtil.addDateHour(dateMenu,2));   //结束时间小于当前时间2小时后的时间段

            //通过获取Redis中存入的商品ID，排除已经存入到Redis中的秒杀商品信息
            Set keys = redisTemplate.boundHashOps(timespace).keys();    //获取该key的所有field
            if (keys != null && keys.size()>0) {
                criteria.andNotIn("id",keys);
            }

            //查询数据
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);

            for (SeckillGoods seckillGood : seckillGoods) {
                System.out.println(timespace+"---------商品ID："+seckillGood.getId());
                //将其中一个时间段内的商品信息存入Redis
                redisTemplate.boundHashOps(timespace).put(seckillGood.getId(),seckillGood);
            }

        }
    }

}
