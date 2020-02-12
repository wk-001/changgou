package com.wk.file.utils;

import com.wk.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 实现FastDFS文件管理操作，包括：
 *  文件上传、下载、删除、信息获取
 *  Tracker、Storage信息获取
 */
public class FastDFSUtil {

    /**
     * 加载Tracker连接信息
     */
    static {
        try {
            //查找classpath下FastDFS配置文件的路径
            String fileName = new ClassPathResource("fdfs_client.conf").getPath();
            //初始化Tracker连接信息
            ClientGlobal.init(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取StorageClient
    private static StorageClient getStorageClient() throws IOException {
        //创建一个Tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();

        //通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();

        //通过TrackerServer的连接信息，获取Storage的连接信息，创建StorageClient对象存储Storage的连接信息
        return new StorageClient(trackerServer,null);
    }

    /**
     * 文件上传
     * @param fastDFSFile ：上传文件信息的封装
     * @return
     */
    public static String[] fileUpload(FastDFSFile fastDFSFile) throws Exception {
        StorageClient storageClient = getStorageClient();

        /*通过StorageClient访问Storage，实现文件上传,并且获取文件上传后的存储信息
        * 参数：1、上传文件的字节数组；2、文件的扩展名，如jpg、png；3、元数据( 文件的大小,文件的作者,文件的创建时间戳)*/
        NameValuePair[] meta_list = new NameValuePair[]{new NameValuePair(fastDFSFile.getAuthor()), new NameValuePair(fastDFSFile.getName())};

        /**
         * uploadResult[0]：文件上传所存储Storage组的名字groupl
         * uploadResult[1]：文件存储到Storage上的文件名字M00/02/44/itheima.jpg
         */
        String[] uploadResult = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
        return uploadResult;
    }

    /**
     * 文件下载
     * @param groupName ：文件所在的组名
     * @param remoteFileName ：文件的存储路径
     */
    public static InputStream downloadFile(String groupName, String remoteFileName) throws Exception {
        StorageClient storageClient = getStorageClient();
        byte[] fileBuffer = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(fileBuffer);
    }


    /**
     * 获取文件信息
     * @param groupName ：文件所在的组名
     * @param remoteFileName ：文件的存储路径
     */
    public static FileInfo getFileInfo(String groupName, String remoteFileName) throws Exception {
        return getStorageClient().get_file_info(groupName,remoteFileName);
    }

    /**
     * 删除文件
     * @param groupName ：文件所在的组名
     * @param remoteFileName ：文件的存储路径
     */
    public static void deleteFile(String groupName, String remoteFileName) throws Exception {
        StorageClient storageClient = getStorageClient();
        storageClient.delete_file(groupName, remoteFileName);
    }

    /**
     * 获取Storage信息
     * @return
     */
    public static StorageServer getStorage() throws IOException {
        //创建一个Tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();

        //通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取Storage信息
        return trackerClient.getStoreStorage(trackerServer);
    }

    /**
     * 获取Storage的IP和端口信息
     * @return
     */
    public static ServerInfo[] getStorageIPPorts(String groupName, String remoteFileName) throws IOException {
        //创建一个Tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();

        //通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取Storage的IP和端口信息
        return trackerClient.getFetchStorages(trackerServer,groupName,remoteFileName);
    }

    //获取Tracker的信息
    public static String getTrackerInfo() throws IOException {
        //创建一个Tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();

        //通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();

        //Tracker的IP和HTTP端口
        String ip = trackerServer.getInetSocketAddress().getHostString();
        int port = ClientGlobal.getG_tracker_http_port();
        String url = "http://"+ip+":"+port;
        return url;
    }


    public static void main(String[] args) throws Exception {
       /* 测试获取文件信息
        FileInfo fileInfo = getFileInfo("group1", "M00/00/00/wKgAp15CyCOAc50MABl5dm1NZ-g101.jpg");
        System.out.println("fileInfo = " + fileInfo.getSourceIpAddr());
        System.out.println("fileInfo = " + fileInfo.getFileSize());*/

       /* 测试文件下载
       //获取文件输入流
        InputStream is = downloadFile("group1", "M00/00/00/wKgAp15CyCOAc50MABl5dm1NZ-g101.jpg");
        //将文件写入到本地磁盘
        FileOutputStream fo = new FileOutputStream("d:/1.jpg");
        //定义一个缓冲区
        byte[] buffer = new byte[1024];
        while (is.read(buffer)!=-1){
            fo.write(buffer);
        }
        fo.flush();
        fo.close();
        is.close();*/

        /* 测试文件删除
       deleteFile("group1", "M00/00/00/wKgAp15CyCOAc50MABl5dm1NZ-g101.jpg");*/

        /*StorageServer storage = getStorage();
        获取Storage下标信息
        System.out.println(storage.getStorePathIndex());
        获取IP地址和端口号
        System.out.println(storage.getInetSocketAddress());*/

       /* 根据组名和文件名获取storage的IP和端口号
       ServerInfo[] ipPorts = getStorageIPPorts("group1", "M00/00/00/wKgAp15CyCOAc50MABl5dm1NZ-g101.jpg");
        for (ServerInfo ipPort : ipPorts) {
            System.out.println(ipPort.getIpAddr());
            System.out.println(ipPort.getPort());
        }*/

        /*获取Tracker的信息
        System.out.println(getTracker());*/
    }
}
