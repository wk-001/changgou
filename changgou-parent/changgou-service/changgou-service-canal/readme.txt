Canal的作用：相当于MySQL的一个从节点，MySQL作为主节点，执行增删改操作的时候，开启Binary log二进制日志，将增删改操作的日志写入到
日志文件。canal定期（毫秒级）读取MySQL数据更新日志文件，读取之后立刻将变更后的数据同步到canal中。可以通过搭建微服务获取canal中的数据，
同步到其他数据库中

使用流程：
    1、访问http://192.168.0.167/read_content?id=1，显示广告数据
    2、修改tb_content表的广告数据
    3、再次请求http://192.168.0.167/read_content?id=1，Redis缓存中的数据是更新后的数据