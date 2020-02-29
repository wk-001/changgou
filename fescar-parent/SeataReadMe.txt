Seata使用流程：
1、在需要分布式事务的项目中添加Seata依赖
<!--Seata依赖-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-seata</artifactId>
    <version>2.1.0.RELEASE</version>
</dependency>

2、复制fescar-api.resources下的file.conf、registry.conf两个文件到需要分布式事务的项目中

3、修改各个微服务的yml文件，添加如下配置，配置通信指定的组名；该配置和file.con中的vgroup_mapping.my_test_tx_group = "default"对应
spring:
  cloud:
    alibaba:
      seata:
        tx-service-group: my_test_tx_group

4、启动seata-server；下载地址：https://github.com/seata/seata/releases ；解压后双击seata-server-0.8.0\bin\seata-server.bat启动服务
